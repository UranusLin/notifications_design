import { Injectable, Logger } from '@nestjs/common';
import { ChannelAdapter } from '../channel.interface';

@Injectable()
export class PushAdapter implements ChannelAdapter {
    private readonly logger = new Logger(PushAdapter.name);

    supports(channel: string): boolean {
        return channel === 'push';
    }

    async send(recipientId: string, message: string): Promise<void> {
        // In a real app, this would call FCM/APNs
        this.logger.log(`[Push] Sending to ${recipientId}: ${message}`);
        await new Promise((resolve) => setTimeout(resolve, 100)); // Simulate latency
    }
}
