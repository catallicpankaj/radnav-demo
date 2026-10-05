package com.example.newrunpaypal.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Data
public class PaymentEntity {
    @Id
    private String paymentId;
    private String state;
    private String intent;
    private String total;
    private String currency;
    private Instant createdAt;
    private Instant updatedAt;
}