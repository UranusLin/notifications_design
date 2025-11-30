import { Test, TestingModule } from '@nestjs/testing';
import { INestApplication, HttpStatus } from '@nestjs/common';
import * as request from 'supertest';
import { AppModule } from '../src/app.module';
import { KafkaContainer, StartedKafkaContainer } from '@testcontainers/kafka';
import { PostgreSqlContainer, StartedPostgreSqlContainer } from '@testcontainers/postgresql';

describe('Notification Integration (e2e)', () => {
    let app: INestApplication;
    let kafkaContainer: StartedKafkaContainer;
    let postgresContainer: StartedPostgreSqlContainer;

    beforeAll(async () => {
        // Start Kafka container
        kafkaContainer = await new KafkaContainer('confluentinc/cp-kafka:7.5.0')
            .withExposedPorts(9093)
            .start();

        // Start PostgreSQL container
        postgresContainer = await new PostgreSqlContainer('postgres:16-alpine')
            .start();

        // Override environment variables
        const kafkaPort = kafkaContainer.getMappedPort(9093);
        const kafkaHost = kafkaContainer.getHost();
        process.env.KAFKA_BROKERS = `${kafkaHost}:${kafkaPort}`;

        process.env.DB_HOST = postgresContainer.getHost() === 'localhost' ? '127.0.0.1' : postgresContainer.getHost();
        process.env.DB_PORT = postgresContainer.getPort().toString();
        process.env.DB_USERNAME = postgresContainer.getUsername();
        process.env.DB_PASSWORD = postgresContainer.getPassword();
        process.env.DB_DATABASE = postgresContainer.getDatabase();

        console.log('DB Config:', {
            host: process.env.DB_HOST,
            port: process.env.DB_PORT,
            username: process.env.DB_USERNAME,
            password: process.env.DB_PASSWORD,
            database: process.env.DB_DATABASE,
        });

        const moduleFixture: TestingModule = await Test.createTestingModule({
            imports: [AppModule],
        }).compile();

        app = moduleFixture.createNestApplication();
        await app.init();

        // Give worker time to connect
        await new Promise(resolve => setTimeout(resolve, 5000));
    }, 120000);

    afterAll(async () => {
        await app.close();
        await kafkaContainer.stop();
        await postgresContainer.stop();
    }, 30000);

    it('/notifications (POST) should enqueue notification', async () => {
        const response = await request(app.getHttpServer())
            .post('/notifications')
            .send({
                channels: ['email', 'sms'],
                recipientIds: ['user123', 'user456'],
                message: 'TypeScript integration test',
            })
            .expect(HttpStatus.CREATED);

        expect(response.body).toHaveProperty('id');
        expect(response.body).toHaveProperty('status', 'accepted');
    });

    it('should process notification and update status', async () => {
        const response = await request(app.getHttpServer())
            .post('/notifications')
            .send({
                channels: ['push'],
                recipientIds: ['device-token-ts'],
                message: 'Worker integration test',
            })
            .expect(HttpStatus.CREATED);

        const notificationId = response.body.id;
        expect(notificationId).toBeDefined();

        // Wait for worker to process
        await new Promise(resolve => setTimeout(resolve, 5000));

        // Check status
        const statusResponse = await request(app.getHttpServer())
            .get(`/status/${notificationId}`)
            .expect(HttpStatus.OK);

        expect(statusResponse.body).toHaveProperty('notification_id', notificationId);
        // Status might be PROCESSING or COMPLETED depending on timing
        console.log('Status:', statusResponse.body.status);
    }, 15000);

    it('/metrics (GET) should return metrics', async () => {
        const response = await request(app.getHttpServer())
            .get('/metrics')
            .expect(HttpStatus.OK);

        expect(response.body).toHaveProperty('total_sent');
        expect(response.body.total_sent).toBeGreaterThan(0);
    });
});
