export class NotificationRequest {
    channels: string[];
    recipientIds: string[];
    message: string;
    metadata?: Record<string, any>;
}
