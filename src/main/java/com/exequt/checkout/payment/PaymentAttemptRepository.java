package com.exequt.checkout.payment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, UUID> {

    Optional<PaymentAttempt> findByExternalPaymentId(String externalPaymentId);

    boolean existsByOrderIdAndStatusIn(UUID orderId, List<PaymentAttemptStatus> statuses);
}
