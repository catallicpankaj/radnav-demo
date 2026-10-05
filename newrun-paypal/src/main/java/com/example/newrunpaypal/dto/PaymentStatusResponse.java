package com.example.newrunpaypal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentStatusResponse {
    private String payment_id;
    private String state; // created|approved|failed|canceled
    private String intent;
    private Amount amount;
    private String created_at;
    private String updated_at;

    @Data
    public static class Amount {
        private String total;
        private String currency;
    }
}