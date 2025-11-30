import { Test, TestingModule } from '@nestjs/testing';
import { WebhookController } from './webhook.controller';
import { NotificationStatusService } from './notification-status.service';

describe('WebhookController', () => {
    let controller: WebhookController;
    let service: NotificationStatusService;

    beforeEach(async () => {
        const module: TestingModule = await Test.createTestingModule({
            controllers: [WebhookController],
            providers: [
                {
                    provide: NotificationStatusService,
                    useValue: {
                        updateChannelStatus: jest.fn(),
                    },
                },
            ],
        }).compile();

        controller = module.get<WebhookController>(WebhookController);
        service = module.get<NotificationStatusService>(NotificationStatusService);
    });

    it('should be defined', () => {
        expect(controller).toBeDefined();
    });

    describe('handleCallback', () => {
        it('should update channel status and return success', async () => {
            const payload = {
                notification_id: 'test-id',
                channel: 'email',
                status: 'COMPLETED',
            };

            jest.spyOn(service, 'updateChannelStatus').mockResolvedValue(undefined);

            const result = await controller.handleCallback(payload);

            expect(service.updateChannelStatus).toHaveBeenCalledWith(
                'test-id',
                'email',
                'COMPLETED',
            );
            expect(result).toEqual({ status: 'updated' });
        });

        it('should handle different channels', async () => {
            const payload = {
                notification_id: 'test-id-2',
                channel: 'sms',
                status: 'FAILED',
            };

            jest.spyOn(service, 'updateChannelStatus').mockResolvedValue(undefined);

            const result = await controller.handleCallback(payload);

            expect(service.updateChannelStatus).toHaveBeenCalledWith(
                'test-id-2',
                'sms',
                'FAILED',
            );
            expect(result).toEqual({ status: 'updated' });
        });
    });
});
