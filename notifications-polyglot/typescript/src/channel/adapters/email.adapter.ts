import { Injectable, Logger } from '@nestjs/common';
import { ChannelAdapter } from '../channel.interface';

@Injectable()
export class EmailAdapter implements ChannelAdapter {
    private readonly logger = new Logger(EmailAdapter.name);

    supports(channel: string): boolean {
        return channel === 'email';
    }

    async send(recipientId: string, message: string): Promise<void> {
        // In a real app, this would call SendGrid/SES API
        this.logger.log(`[Email] Sending to ${recipientId}: ${message}`);
        await new Promise((resolve) => setTimeout(resolve, 200)); // Simulate latency
    }
}
