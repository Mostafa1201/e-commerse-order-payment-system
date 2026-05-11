package com.exequt.checkout.payment;

import java.math.BigDecimal;

public interface PaymentProviderService {
    PaymentInitiationResponse initiatePayment(BigDecimal amount);
}
