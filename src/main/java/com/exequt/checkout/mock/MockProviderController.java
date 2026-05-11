package com.exequt.checkout.mock;

import com.exequt.checkout.payment.PaymentDtos;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mock-provider")
public class MockProviderController {
    private final MockProviderService mockProviderService;

    public MockProviderController(MockProviderService mockProviderService) {
        this.mockProviderService = mockProviderService;
    }

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, String>> trigger(
            @Valid @RequestBody PaymentDtos.TriggerRequest request) {

        mockProviderService.triggerWebhook(request.getExternalPaymentId(), request.getResult());

        return ResponseEntity.ok(Map.of(
                "message", "Webhook will be delivered",
                "externalPaymentId", request.getExternalPaymentId(),
                "result", request.getResult().name()
        ));
    }
}
