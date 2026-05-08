package com.exequt.ecommerce.application.service;

import com.exequt.ecommerce.domain.gateway.PaymentGateway;
import com.exequt.ecommerce.domain.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@EnableAsync
public class MockPaymentProviderService implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentProviderService.class);

    @Override
    @Async
    public void initiatePayment(Payment payment) {
        log.info("Mock payment provider: payment {} initiated for order {} (amount: {})",
                payment.getId(), payment.getOrderId(), payment.getAmount());
    }
}
