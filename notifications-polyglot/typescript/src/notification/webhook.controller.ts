import { Controller, Post, Body } from '@nestjs/common';
import { NotificationStatusService } from './notification-status.service';

@Controller('webhook')
export class WebhookController {
    constructor(private readonly statusService: NotificationStatusService) { }

    @Post('callback')
    async handleCallback(@Body() payload: any): Promise<{ status: string }> {
        await this.statusService.updateChannelStatus(
            payload.notification_id,
            payload.channel,
            payload.status,
        );
        return { status: 'updated' };
    }
}
