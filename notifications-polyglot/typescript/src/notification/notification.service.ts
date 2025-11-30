import { Inject, Injectable, Logger } from '@nestjs/common';
import { ClientKafka } from '@nestjs/microservices';
import { v4 as uuidv4 } from 'uuid';
import { NotificationRequest } from './notification.dto';
import { NotificationStatusService } from './notification-status.service';

@Injectable()
export class NotificationService {
    private readonly logger = new Logger(NotificationService.name);

    constructor(
        @Inject('KAFKA_SERVICE') private readonly client: ClientKafka,
        private readonly statusService: NotificationStatusService,
    ) { }

    async onModuleInit() {
        this.client.subscribeToResponseOf('notifications');
        await this.client.connect();
    }

    async enqueueNotification(request: NotificationRequest): Promise<string> {
        const notificationId = uuidv4();
        this.logger.log(`Enqueuing notification: ${notificationId}`);

        await this.statusService.createInitialStatus(notificationId, request.channels);

        this.client.emit('notifications', {
            key: notificationId,
            value: request,
        });

        return notificationId;
    }
}
