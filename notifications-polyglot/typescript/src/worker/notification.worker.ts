import { Controller, Logger } from '@nestjs/common';
import { Ctx, EventPattern, KafkaContext, Payload } from '@nestjs/microservices';
import { NotificationRequest } from '../notification/notification.dto';
import { ChannelFactory } from '../channel/channel.factory';
import { NotificationStatusService } from '../notification/notification-status.service';

@Controller()
export class NotificationWorker {
    private readonly logger = new Logger(NotificationWorker.name);

    constructor(
        private readonly channelFactory: ChannelFactory,
        private readonly statusService: NotificationStatusService,
    ) { }

    @EventPattern('notifications')
    async handleNotification(@Payload() message: any, @Ctx() context: KafkaContext) {
        const request: NotificationRequest = message;
        const originalMessage = context.getMessage();
        const notificationId = originalMessage.key.toString();

        this.logger.log(
            `Processing notification ${notificationId} for channels: ${request.channels.join(', ')}`,
        );

        for (const channel of request.channels) {
            await this.statusService.updateChannelStatus(notificationId, channel, 'PROCESSING');
            try {
                const adapter = this.channelFactory.getAdapter(channel);
                let success = true;
                for (const recipientId of request.recipientIds) {
                    try {
                        await adapter.send(recipientId, request.message);
                    } catch (e) {
                        success = false;
                        this.logger.error(`Failed to send to ${recipientId} via ${channel}: ${e}`);
                    }
                }

                if (success) {
                    await this.statusService.updateChannelStatus(notificationId, channel, 'COMPLETED');
                } else {
                    await this.statusService.updateChannelStatus(notificationId, channel, 'FAILED');
                }
            } catch (error) {
                this.logger.error(
                    `Failed to process channel ${channel}: ${(error as any).message}`,
                );
                await this.statusService.updateChannelStatus(notificationId, channel, 'FAILED');
            }
        }

        this.logger.log(`Notification ${notificationId} processed successfully`);
    }
}
