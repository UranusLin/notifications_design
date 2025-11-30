import { NotificationRequest } from '../notification/notification.dto';

export interface ChannelAdapter {
    send(recipientId: string, message: string): Promise<void>;
    supports(channel: string): boolean;
}
