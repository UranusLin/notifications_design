import { NestFactory } from '@nestjs/core';
import {
    FastifyAdapter,
    NestFastifyApplication,
} from '@nestjs/platform-fastify';
import { AppModule } from './app.module';
import { MicroserviceOptions, Transport } from '@nestjs/microservices';
import { winstonConfig } from './common/logger.config';
import { GlobalExceptionFilter } from './common/global-exception.filter';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';

async function bootstrap() {
    const app = await NestFactory.create<NestFastifyApplication>(
        AppModule,
        new FastifyAdapter(),
        {
            logger: winstonConfig,
        },
    );

    app.useGlobalFilters(new GlobalExceptionFilter());

    const config = new DocumentBuilder()
        .setTitle('Notification Service')
        .setDescription('The notification service API description')
        .setVersion('1.0')
        .build();
    const document = SwaggerModule.createDocument(app, config);
    SwaggerModule.setup('api', app, document);

    const kafkaBrokers = process.env.KAFKA_BROKERS?.split(',') || ['localhost:9092'];

    app.connectMicroservice<MicroserviceOptions>({
        transport: Transport.KAFKA,
        options: {
            client: {
                brokers: kafkaBrokers,
            },
            consumer: {
                groupId: 'notification-workers-ts',
            },
        },
    });

    await app.startAllMicroservices();
    await app.listen(3000, '0.0.0.0');
    console.log(`Application is running on: ${await app.getUrl()}`);
}
bootstrap();
