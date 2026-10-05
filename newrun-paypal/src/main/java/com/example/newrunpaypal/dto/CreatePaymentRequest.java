package com.example.newrunpaypal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CreatePaymentRequest {
    @NotBlank
    private String intent; // sale|authorize|order
    @NotNull
    private Payer payer;
    @NotEmpty
    private List<Transaction> transactions;
    @NotNull
    private RedirectUrls redirect_urls;

    @Data
    public static class Payer {
        @NotBlank
        private String payment_method; // should be "paypal"
    }

    @Data
    public static class Transaction {
        @NotNull
        private Amount amount;
        private String description;
        private ItemList item_list;
    }

    @Data
    public static class Amount {
        @NotBlank
        private String total;
        @NotBlank
        private String currency;
    }

    @Data
    public static class ItemList {
        @NotEmpty
        private List<Item> items;
    }

    @Data
    public static class Item {
        @NotBlank
        private String name;
        @NotBlank
        private String sku;
        @NotBlank
        private String price;
        @NotBlank
        private String currency;
        @NotNull
        private Integer quantity;
    }

    @Data
    public static class RedirectUrls {
        @NotBlank
        private String return_url;
        @NotBlank
        private String cancel_url;
    }
}