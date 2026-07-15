package com.sistema.distribuido.domain;

public record Order(String id, String customerId, double amount, OrderStatus status) {
    public Order withStatus(OrderStatus newStatus) {
        return new Order(this.id, this.customerId, this.amount, newStatus);
    }
}
