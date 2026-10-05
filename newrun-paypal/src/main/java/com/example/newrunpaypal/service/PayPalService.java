package com.example.newrunpaypal.service;

import com.example.newrunpaypal.dto.*;

public interface PayPalService {
    CreatePaymentResponse createPayment(CreatePaymentRequest request);
    ExecutePaymentResponse executePayment(ExecutePaymentRequest request);
    PaymentStatusResponse getPaymentStatus(String paymentId);
    WebhookAckResponse handleWebhook(String rawPayload, String transmissionId, String transmissionTime, String certUrl, String authAlgo, String transmissionSig);
    CredentialsResponse updateCredentials(CredentialsRequest request);
}