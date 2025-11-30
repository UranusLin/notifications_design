import { Test, TestingModule } from '@nestjs/testing';
import { NotificationController } from './notification.controller';
import { NotificationService } from './notification.service';
import { FastifyReply } from 'fastify';

describe('NotificationController', () => {
    let controller: NotificationController;
    let service: NotificationService;

    beforeEach(async () => {
        const module: TestingModule = await Test.createTestingModule({
            controllers: [NotificationController],
            providers: [
                {
                    provide: NotificationService,
                    useValue: {
                        enqueueNotification: jest.fn().mockResolvedValue('test-uuid'),
                    },
                },
            ],
        }).compile();

        controller = module.get<NotificationController>(NotificationController);
        service = module.get<NotificationService>(NotificationService);
    });

    it('should be defined', () => {
        expect(controller).toBeDefined();
    });

    it('should enqueue notification', async () => {
        const req = {
            channels: ['email'],
            recipientIds: ['user1'],
            message: 'test',
        };

        const res = {
            status: jest.fn().mockReturnThis(),
            send: jest.fn(),
        } as unknown as FastifyReply;

        await controller.sendNotification(req, res);

        expect(service.enqueueNotification).toHaveBeenCalledWith(req);
        expect(res.status).toHaveBeenCalledWith(201);
        expect(res.send).toHaveBeenCalledWith({
            id: 'test-uuid',
            status: 'accepted',
        });
    });
});
