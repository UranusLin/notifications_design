import { Controller, Get, Param } from '@nestjs/common';
import { NotificationStatusService } from './notification-status.service';
import { NotificationStatus } from './notification-status.entity';
import { NotificationMetrics } from './notification-metrics.dto';

@Controller()
export class StatusController {
    constructor(private readonly statusService: NotificationStatusService) { }

    @Get('status/:id')
    async getStatus(@Param('id') id: string): Promise<NotificationStatus> {
        return this.statusService.getStatus(id);
    }

    @Get('metrics')
    async getMetrics(): Promise<NotificationMetrics> {
        return this.statusService.getMetrics();
    }
}
