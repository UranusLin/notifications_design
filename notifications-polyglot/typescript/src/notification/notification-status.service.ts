import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { NotificationStatus } from './notification-status.entity';
import { NotificationMetrics } from './notification-metrics.dto';

@Injectable()
export class NotificationStatusService {
    constructor(
        @InjectRepository(NotificationStatus)
        private statusRepository: Repository<NotificationStatus>,
    ) { }

    async createInitialStatus(notificationId: string, channels: string[]): Promise<void> {
        const channelStatuses: Record<string, string> = {};
        channels.forEach((channel) => {
            channelStatuses[channel] = 'PENDING';
        });

        const status = this.statusRepository.create({
            notification_id: notificationId,
            status: 'ENQUEUED',
            channel_statuses: channelStatuses,
        });

        await this.statusRepository.save(status);
    }

    async getStatus(notificationId: string): Promise<NotificationStatus> {
        return this.statusRepository.findOneBy({ notification_id: notificationId });
    }

    async updateChannelStatus(
        notificationId: string,
        channel: string,
        newStatus: string,
    ): Promise<void> {
        const status = await this.statusRepository.findOneBy({ notification_id: notificationId });
        if (!status) {
            return;
        }

        status.channel_statuses[channel] = newStatus;

        // Aggregate overall status
        let allCompleted = true;
        let anyFailed = false;

        Object.values(status.channel_statuses).forEach((s) => {
            if (s !== 'COMPLETED') {
                allCompleted = false;
            }
            if (s === 'FAILED') {
                anyFailed = true;
            }
        });

        if (allCompleted) {
            status.status = 'COMPLETED';
        } else if (anyFailed) {
            status.status = 'PARTIAL_FAILURE';
        } else {
            status.status = 'PROCESSING';
        }

        await this.statusRepository.save(status);
    }

    async getMetrics(): Promise<NotificationMetrics> {
        const results = await this.statusRepository
            .createQueryBuilder('status')
            .select('status.status', 'status')
            .addSelect('COUNT(*)', 'count')
            .groupBy('status.status')
            .getRawMany();

        const counts: Record<string, number> = {};
        let total = 0;
        let success = 0;
        let failed = 0;

        results.forEach((r) => {
            const count = parseInt(r.count, 10);
            counts[r.status] = count;
            total += count;
            if (r.status === 'COMPLETED') {
                success += count;
            } else if (r.status === 'FAILED') {
                failed += count;
            }
        });

        return {
            total_sent: total,
            total_success: success,
            total_failed: failed,
            by_status: counts,
        };
    }
}
