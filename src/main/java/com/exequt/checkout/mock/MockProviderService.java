package com.exequt.checkout.mock;

import com.exequt.checkout.payment.PaymentDtos;
import com.exequt.checkout.payment.PaymentInitiationResponse;
import com.exequt.checkout.payment.PaymentProviderService;
import java.math.BigDecimal;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MockProviderService implements PaymentProviderService {

    private static final Logger log = LoggerFactory.getLogger(MockProviderService.class);

    private final RestTemplate restTemplate;

    @Value("${app.base-url}")
    private String baseUrl;

    public MockProviderService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PaymentInitiationResponse initiatePayment(BigDecimal amount) {
        String externalPaymentId = "ext-" + UUID.randomUUID();
        log.info("Mock provider initiated payment. ExternalId={} Amount={}",
                externalPaymentId, amount);
        return new PaymentInitiationResponse(externalPaymentId, "INITIATED");
    }

    public void triggerWebhook(String externalPaymentId, PaymentDtos.WebhookResult result) {
        PaymentDtos.WebhookPayload payload = new PaymentDtos.WebhookPayload();
        payload.setExternalPaymentId(externalPaymentId);
        payload.setResult(result);

        String webhookUrl = baseUrl + "/payments/webhook";
        log.info("Mock provider firing webhook: {} -> {}", externalPaymentId, result);

        restTemplate.postForEntity(webhookUrl, payload, String.class);
        log.info("Webhook delivered successfully");
    }

}
