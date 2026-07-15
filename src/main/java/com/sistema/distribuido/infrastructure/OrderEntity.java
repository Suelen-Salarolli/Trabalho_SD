package com.sistema.distribuido.infrastructure;

import com.sistema.distribuido.domain.Order;
import com.sistema.distribuido.domain.OrderStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    private String id;
    
    private String customerId;
    private double amount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public OrderEntity() {}

    public OrderEntity(String id, String customerId, double amount, OrderStatus status) {
        this.id = id;
        this.customerId = customerId;
        this.amount = amount;
        this.status = status;
    }

    public static OrderEntity fromDomain(Order order) {
        return new OrderEntity(order.id(), order.customerId(), order.amount(), order.status());
    }

    public Order toDomain() {
        return new Order(this.id, this.customerId, this.amount, this.status);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
