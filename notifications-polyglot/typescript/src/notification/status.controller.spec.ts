import { Test, TestingModule } from '@nestjs/testing';
import { StatusController } from './status.controller';
import { NotificationStatusService } from './notification-status.service';
import { NotificationStatus } from './notification-status.entity';
import { NotificationMetrics } from './notification-metrics.dto';

describe('StatusController', () => {
    let controller: StatusController;
    let service: NotificationStatusService;

    beforeEach(async () => {
        const module: TestingModule = await Test.createTestingModule({
            controllers: [StatusController],
            providers: [
                {
                    provide: NotificationStatusService,
                    useValue: {
                        getStatus: jest.fn(),
                        getMetrics: jest.fn(),
                    },
                },
            ],
        }).compile();

        controller = module.get<StatusController>(StatusController);
        service = module.get<NotificationStatusService>(NotificationStatusService);
    });

    it('should be defined', () => {
        expect(controller).toBeDefined();
    });

    describe('getStatus', () => {
        it('should return notification status', async () => {
            const mockStatus: NotificationStatus = {
                notification_id: 'test-id',
                status: 'COMPLETED',
                channel_statuses: { email: 'COMPLETED', sms: 'COMPLETED' } as any,
                created_at: new Date(),
                updated_at: new Date(),
            };

            jest.spyOn(service, 'getStatus').mockResolvedValue(mockStatus);

            const result = await controller.getStatus('test-id');

            expect(service.getStatus).toHaveBeenCalledWith('test-id');
            expect(result).toEqual(mockStatus);
        });
    });

    describe('getMetrics', () => {
        it('should return notification metrics', async () => {
            const mockMetrics: NotificationMetrics = {
                total_sent: 100,
                total_success: 80,
                total_failed: 20,
                by_status: {
                    COMPLETED: 80,
                    FAILED: 20,
                },
            };

            jest.spyOn(service, 'getMetrics').mockResolvedValue(mockMetrics);

            const result = await controller.getMetrics();

            expect(service.getMetrics).toHaveBeenCalled();
            expect(result).toEqual(mockMetrics);
        });
    });
});
