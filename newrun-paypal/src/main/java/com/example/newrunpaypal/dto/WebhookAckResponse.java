package com.example.newrunpaypal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WebhookAckResponse {
    private String status;
}