export class NotificationMetrics {
    total_sent: number;
    total_success: number;
    total_failed: number;
    by_status: Record<string, number>;
}
