package com.sistema.distribuido.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateOrderUseCaseTest {

    @Test
    void createsPaidOrderWhenPaymentSucceeds() {
        InMemoryOrderRepository repository = new InMemoryOrderRepository();
        InMemoryIdempotencyStore idempotencyStore = new InMemoryIdempotencyStore();
        RecordingMessageBroker broker = new RecordingMessageBroker();
        CreateOrderUseCase useCase = new CreateOrderUseCase(
                repository,
                broker,
                (orderId, amount) -> new PaymentResult(true, "tx-1"),
                idempotencyStore);

        Order order = useCase.execute("key-1", new OrderInput("order-1", "customer-1", 100.0));

        assertEquals(OrderStatus.PAID, order.status());
        assertEquals(OrderStatus.PAID, repository.findById("order-1").orElseThrow().status());
        assertEquals(1, broker.events.size());
    }

    @Test
    void createsFailedOrderWhenPaymentFails() {
        InMemoryOrderRepository repository = new InMemoryOrderRepository();
        CreateOrderUseCase useCase = new CreateOrderUseCase(
                repository,
                new RecordingMessageBroker(),
                (orderId, amount) -> new PaymentResult(false, "fallback-unavailable"),
                new InMemoryIdempotencyStore());

        Order order = useCase.execute("key-2", new OrderInput("order-2", "customer-2", 250.0));

        assertEquals(OrderStatus.FAILED, order.status());
        assertEquals(OrderStatus.FAILED, repository.findById("order-2").orElseThrow().status());
    }

    @Test
    void returnsStoredOrderForRepeatedIdempotencyKey() {
        InMemoryOrderRepository repository = new InMemoryOrderRepository();
        InMemoryIdempotencyStore idempotencyStore = new InMemoryIdempotencyStore();
        RecordingMessageBroker broker = new RecordingMessageBroker();
        CreateOrderUseCase useCase = new CreateOrderUseCase(
                repository,
                broker,
                (orderId, amount) -> new PaymentResult(true, "tx-3"),
                idempotencyStore);

        Order first = useCase.execute("key-3", new OrderInput("order-3", "customer-3", 50.0));
        Order second = useCase.execute("key-3", new OrderInput("order-3", "customer-3", 50.0));

        assertEquals(first, second);
        assertEquals(1, broker.events.size());
    }

    private static class InMemoryOrderRepository implements OrderRepository {
        private final Map<String, Order> orders = new LinkedHashMap<>();

        @Override
        public void save(Order order) {
            orders.put(order.id(), order);
        }

        @Override
        public Optional<Order> findById(String id) {
            return Optional.ofNullable(orders.get(id));
        }

        @Override
        public List<Order> findAll() {
            return new ArrayList<>(orders.values());
        }
    }

    private static class InMemoryIdempotencyStore implements IdempotencyKeyStore {
        private final Set<String> keys = ConcurrentHashMap.newKeySet();

        @Override
        public boolean isProcessed(String key) {
            return keys.contains(key);
        }

        @Override
        public void markAsProcessed(String key) {
            keys.add(key);
        }
    }

    private static class RecordingMessageBroker implements MessageBroker {
        private final List<String> events = new ArrayList<>();

        @Override
        public void publish(String event, Object payload) {
            events.add(event);
        }
    }
}
