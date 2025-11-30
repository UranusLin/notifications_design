import { Entity, Column, PrimaryColumn, CreateDateColumn, UpdateDateColumn } from 'typeorm';

@Entity('notification_status')
export class NotificationStatus {
    @PrimaryColumn()
    notification_id: string;

    @Column()
    status: string; // ENQUEUED, PROCESSING, COMPLETED, FAILED, PARTIAL_FAILURE

    @Column('jsonb')
    channel_statuses: Record<string, string>;

    @CreateDateColumn()
    created_at: Date;

    @UpdateDateColumn()
    updated_at: Date;
}
