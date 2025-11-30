package com.example.notification.adapter;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ChannelFactory {

    private final List<ChannelAdapter> adapters;

    public ChannelFactory(List<ChannelAdapter> adapters) {
        this.adapters = adapters;
    }

    public ChannelAdapter getAdapter(String channel) {
        return adapters.stream()
                .filter(adapter -> adapter.supports(channel))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported channel: " + channel));
    }
}
