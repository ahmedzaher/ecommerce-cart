package com.exequt.ecommerce.domain.gateway;

import com.exequt.ecommerce.domain.model.Payment;

public interface PaymentGateway {
    void initiatePayment(Payment payment);
}
