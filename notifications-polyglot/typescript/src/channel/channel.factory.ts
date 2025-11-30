import { Injectable } from '@nestjs/common';
import { ChannelAdapter } from './channel.interface';
import { EmailAdapter } from './adapters/email.adapter';
import { SmsAdapter } from './adapters/sms.adapter';
import { PushAdapter } from './adapters/push.adapter';

@Injectable()
export class ChannelFactory {
    private adapters: ChannelAdapter[];

    constructor(
        emailAdapter: EmailAdapter,
        smsAdapter: SmsAdapter,
        pushAdapter: PushAdapter,
    ) {
        this.adapters = [emailAdapter, smsAdapter, pushAdapter];
    }

    getAdapter(channel: string): ChannelAdapter {
        const adapter = this.adapters.find((a) => a.supports(channel));
        if (!adapter) {
            throw new Error(`Unsupported channel: ${channel}`);
        }
        return adapter;
    }
}
