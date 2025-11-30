package com.example.notification.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class NotificationRequest {
    private List<String> channels;
    private List<String> recipientIds;
    private String message;
    private Map<String, Object> metadata;
}
