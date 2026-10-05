package com.example.newrunpaypal.repository;

import com.example.newrunpaypal.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {
}