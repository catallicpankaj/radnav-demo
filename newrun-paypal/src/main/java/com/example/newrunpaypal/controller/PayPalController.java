package com.example.newrunpaypal.controller;

import com.example.newrunpaypal.dto.*;
import com.example.newrunpaypal.service.PayPalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments/paypal")
public class PayPalController {

    private final PayPalService payPalService;

    public PayPalController(PayPalService payPalService) {
        this.payPalService = payPalService;
    }

    @PostMapping("/create")
    public ResponseEntity<CreatePaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        CreatePaymentResponse response = payPalService.createPayment(request);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/execute")
    public ResponseEntity<ExecutePaymentResponse> executePayment(@Valid @RequestBody ExecutePaymentRequest request) {
        ExecutePaymentResponse response = payPalService.executePayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{paymentId}")
    public ResponseEntity<PaymentStatusResponse> getStatus(@PathVariable String paymentId) {
        PaymentStatusResponse response = payPalService.getPaymentStatus(paymentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<WebhookAckResponse> handleWebhook(@RequestBody String rawPayload,
                                                             @RequestHeader("PAYPAL-TRANSMISSION-ID") String transmissionId,
                                                             @RequestHeader("PAYPAL-TRANSMISSION-TIME") String transmissionTime,
                                                             @RequestHeader("PAYPAL-CERT-URL") String certUrl,
                                                             @RequestHeader("PAYPAL-AUTH-ALGO") String authAlgo,
                                                             @RequestHeader("PAYPAL-TRANSMISSION-SIG") String transmissionSig) {
        WebhookAckResponse ack = payPalService.handleWebhook(rawPayload, transmissionId, transmissionTime, certUrl, authAlgo, transmissionSig);
        return ResponseEntity.ok(ack);
    }

    @PostMapping("/credentials")
    public ResponseEntity<CredentialsResponse> updateCredentials(@Valid @RequestBody CredentialsRequest request) {
        CredentialsResponse resp = payPalService.updateCredentials(request);
        return ResponseEntity.ok(resp);
    }
}