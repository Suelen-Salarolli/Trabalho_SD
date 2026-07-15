package com.sistema.distribuido.controller;

import com.sistema.distribuido.domain.CreateOrderUseCase;
import com.sistema.distribuido.domain.Order;
import com.sistema.distribuido.domain.OrderInput;
import com.sistema.distribuido.domain.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final OrderRepository orderRepository;

    public OrderController(CreateOrderUseCase createOrderUseCase, OrderRepository orderRepository) {
        this.createOrderUseCase = createOrderUseCase;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public ResponseEntity<?> listOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findOrder(@PathVariable String id) {
        return orderRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Order not found.")));
    }

    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody OrderInput input) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "The header 'X-Idempotency-Key' is required."));
        }

        try {
            Order order = createOrderUseCase.execute(idempotencyKey, input);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
}
