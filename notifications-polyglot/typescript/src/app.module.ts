import { Module } from '@nestjs/common';
import { ClientsModule, Transport } from '@nestjs/microservices';
import { TypeOrmModule } from '@nestjs/typeorm';
import { NotificationController } from './notification/notification.controller';
import { NotificationService } from './notification/notification.service';
import { NotificationWorker } from './worker/notification.worker';
import { ChannelModule } from './channel/channel.module';
import { NotificationStatus } from './notification/notification-status.entity';
import { NotificationStatusService } from './notification/notification-status.service';
import { StatusController } from './notification/status.controller';
import { WebhookController } from './notification/webhook.controller';

import { ConfigModule, ConfigService } from '@nestjs/config';

@Module({
    imports: [
        ConfigModule.forRoot({
            isGlobal: true,
        }),
        TypeOrmModule.forRootAsync({
            imports: [ConfigModule],
            useFactory: (configService: ConfigService) => ({
                type: 'postgres',
                host: configService.get<string>('DB_HOST', 'localhost'),
                port: configService.get<number>('DB_PORT', 5432),
                username: configService.get<string>('DB_USERNAME', 'postgres'),
                password: configService.get<string>('DB_PASSWORD', 'postgres'),
                database: configService.get<string>('DB_DATABASE', 'notification_db'),
                entities: [NotificationStatus],
                synchronize: true,
            }),
            inject: [ConfigService],
        }),
        TypeOrmModule.forFeature([NotificationStatus]),
        ClientsModule.registerAsync([
            {
                name: 'KAFKA_SERVICE',
                imports: [ConfigModule],
                useFactory: (configService: ConfigService) => ({
                    transport: Transport.KAFKA,
                    options: {
                        client: {
                            clientId: 'notification',
                            brokers: [configService.get<string>('KAFKA_BROKERS', 'localhost:9092')],
                        },
                        consumer: {
                            groupId: 'notification-producer',
                        },
                    },
                }),
                inject: [ConfigService],
            },
        ]),
        ChannelModule,
    ],
    controllers: [
        NotificationController,
        NotificationWorker,
        StatusController,
        WebhookController,
    ],
    providers: [NotificationService, NotificationStatusService],
})
export class AppModule { }
