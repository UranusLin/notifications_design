import { Body, Controller, Post, Res, HttpStatus } from '@nestjs/common';
import { FastifyReply } from 'fastify';
import { NotificationService } from './notification.service';
import { NotificationRequest } from './notification.dto';
import { ApiResponse } from '../common/api-response';

@Controller('notifications')
export class NotificationController {
    constructor(private readonly notificationService: NotificationService) { }

    @Post()
    async sendNotification(@Body() request: NotificationRequest, @Res() res: FastifyReply) {
        const id = await this.notificationService.enqueueNotification(request);
        res.status(HttpStatus.CREATED).send({
            id: id,
            status: 'accepted',
        });
    }
}
