package com.example.newrunpaypal.adapter;

import com.example.newrunpaypal.dto.*;
import com.example.newrunpaypal.model.PaymentEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Very simple mock that pretends to talk to PayPal.
 * In a real implementation this would call the PayPal SDK.
 */
@Component
public class MockPayPalAdapter {

    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        String paymentId = "PAY-" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 17).toUpperCase();
        String approvalUrl = "https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=EC-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        return new CreatePaymentResponse(paymentId, approvalUrl);
    }

    public ExecutePaymentResponse executePayment(ExecutePaymentRequest request) {
        // Mock successful execution
        ExecutePaymentResponse.Sale sale = new ExecutePaymentResponse.Sale();
        sale.setId("SALE-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        sale.setState("completed");
        sale.setCreate_time(Instant.now().toString());
        sale.setUpdate_time(Instant.now().toString());

        ExecutePaymentResponse.RelatedResource rr = new ExecutePaymentResponse.RelatedResource();
        rr.setSale(sale);

        ExecutePaymentResponse.Transaction txn = new ExecutePaymentResponse.Transaction();
        ExecutePaymentResponse.Amount amt = new ExecutePaymentResponse.Amount();
        amt.setTotal("0.00"); // placeholder; real amount would come from stored entity
        amt.setCurrency("USD");
        txn.setAmount(amt);
        txn.setRelated_resources(java.util.List.of(rr));

        return new ExecutePaymentResponse(request.getPayment_id(), "approved", java.util.List.of(txn));
    }

    public PaymentEntity buildEntityFromCreate(CreatePaymentResponse resp, CreatePaymentRequest req) {
        PaymentEntity e = new PaymentEntity();
        e.setPaymentId(resp.getPayment_id());
        e.setState("created");
        e.setIntent(req.getIntent());
        e.setTotal(req.getTransactions().get(0).getAmount().getTotal());
        e.setCurrency(req.getTransactions().get(0).getAmount().getCurrency());
        Instant now = Instant.now();
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        return e;
    }

    public void updateEntityAfterExecution(PaymentEntity entity, ExecutePaymentResponse resp) {
        entity.setState(resp.getState());
        entity.setUpdatedAt(Instant.now());
    }

    public boolean verifyWebhookSignature(String transmissionId, String transmissionTime, String certUrl, String authAlgo, String transmissionSig, String payload) {
        // Mock always true – replace with real verification later.
        return true;
    }
}