package com.example.newrunpaypal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExecutePaymentRequest {
    @NotBlank
    private String payment_id;
    @NotBlank
    private String payer_id;
}