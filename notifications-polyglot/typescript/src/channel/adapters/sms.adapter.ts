import { Injectable, Logger } from '@nestjs/common';
import { ChannelAdapter } from '../channel.interface';

@Injectable()
export class SmsAdapter implements ChannelAdapter {
    private readonly logger = new Logger(SmsAdapter.name);

    supports(channel: string): boolean {
        return channel === 'sms';
    }

    async send(recipientId: string, message: string): Promise<void> {
        // In a real app, this would call Twilio API
        this.logger.log(`[SMS] Sending to ${recipientId}: ${message}`);
        await new Promise((resolve) => setTimeout(resolve, 500)); // Simulate latency
    }
}
