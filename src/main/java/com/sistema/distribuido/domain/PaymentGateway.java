package com.sistema.distribuido.domain;

public interface PaymentGateway {
    PaymentResult charge(String orderId, double amount);
}
