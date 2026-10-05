package com.example.newrunpaypal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatePaymentResponse {
    private String payment_id;
    private String approval_url;
}