package com.example.newrunpaypal.service;

import com.example.newrunpaypal.adapter.MockPayPalAdapter;
import com.example.newrunpaypal.dto.*;
import com.example.newrunpaypal.model.PaymentEntity;
import com.example.newrunpaypal.repository.PaymentRepository;
import com.example.newrunpaypal.config.PayPalProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;

@Service
public class PayPalServiceImpl implements PayPalService {

    private final MockPayPalAdapter adapter;
    private final PaymentRepository repository;
    private final PayPalProperties properties;

    public PayPalServiceImpl(MockPayPalAdapter adapter, PaymentRepository repository, PayPalProperties properties) {
        this.adapter = adapter;
        this.repository = repository;
        this.properties = properties;
    }

    @Override
    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        // In a real service we would validate intent, amounts, etc.
        CreatePaymentResponse resp = adapter.createPayment(request);
        PaymentEntity entity = adapter.buildEntityFromCreate(resp, request);
        repository.save(entity);
        return resp;
    }

    @Override
    @Transactional
    public ExecutePaymentResponse executePayment(ExecutePaymentRequest request) {
        PaymentEntity entity = repository.findById(request.getPayment_id())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        ExecutePaymentResponse resp = adapter.executePayment(request);
        adapter.updateEntityAfterExecution(entity, resp);
        repository.save(entity);
        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentStatusResponse getPaymentStatus(String paymentId) {
        PaymentEntity entity = repository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        PaymentStatusResponse.Amount amt = new PaymentStatusResponse.Amount();
        amt.setTotal(entity.getTotal());
        amt.setCurrency(entity.getCurrency());
        return new PaymentStatusResponse(
                entity.getPaymentId(),
                entity.getState(),
                entity.getIntent(),
                amt,
                entity.getCreatedAt().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
                entity.getUpdatedAt().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        );
    }

    @Override
    @Transactional
    public WebhookAckResponse handleWebhook(String rawPayload, String transmissionId, String transmissionTime, String certUrl, String authAlgo, String transmissionSig) {
        boolean valid = adapter.verifyWebhookSignature(transmissionId, transmissionTime, certUrl, authAlgo, transmissionSig, rawPayload);
        if (!valid) {
            throw new IllegalArgumentException("Invalid webhook signature");
        }
        // For demo we just acknowledge. Real logic would parse payload and update payment state.
        return new WebhookAckResponse("received");
    }

    @Override
    public CredentialsResponse updateCredentials(CredentialsRequest request) {
        // In a real service credentials would be stored securely (e.g., Vault).
        properties.setClientId(request.getClient_id());
        properties.setClientSecret(request.getClient_secret());
        return new CredentialsResponse("PayPal credentials updated successfully.");
    }
}