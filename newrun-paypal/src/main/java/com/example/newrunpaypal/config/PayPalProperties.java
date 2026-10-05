package com.example.newrunpaypal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "paypal")
public class PayPalProperties {
    private String clientId;
    private String clientSecret;
    private String webhookId;

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    public String getWebhookId() { return webhookId; }
    public void setWebhookId(String webhookId) { this.webhookId = webhookId; }
}