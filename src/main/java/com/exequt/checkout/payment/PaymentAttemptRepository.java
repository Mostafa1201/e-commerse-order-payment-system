package com.exequt.checkout.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, UUID> {
    Optional<PaymentAttempt> findByExternalPaymentId(String externalPaymentId);

    boolean existsByOrderIdAndStatusIn(UUID orderId, List<PaymentAttemptStatus> statuses);
}
