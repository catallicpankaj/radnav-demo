package com.example.newrunpaypal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ExecutePaymentResponse {
    private String payment_id;
    private String state;
    private List<Transaction> transactions;

    @Data
    public static class Transaction {
        private Amount amount;
        private List<RelatedResource> related_resources;
    }

    @Data
    public static class Amount {
        private String total;
        private String currency;
    }

    @Data
    public static class RelatedResource {
        private Sale sale;
    }

    @Data
    public static class Sale {
        private String id;
        private String state;
        private String create_time;
        private String update_time;
    }
}