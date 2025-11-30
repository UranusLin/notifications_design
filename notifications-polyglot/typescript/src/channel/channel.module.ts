import { Module } from '@nestjs/common';
import { EmailAdapter } from './adapters/email.adapter';
import { SmsAdapter } from './adapters/sms.adapter';
import { PushAdapter } from './adapters/push.adapter';
import { ChannelFactory } from './channel.factory';

@Module({
    providers: [EmailAdapter, SmsAdapter, PushAdapter, ChannelFactory],
    exports: [ChannelFactory],
})
export class ChannelModule { }
