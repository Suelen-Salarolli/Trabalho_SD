package com.sistema.distribuido.domain;

public class CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final MessageBroker messageBroker;
    private final PaymentGateway paymentGateway;
    private final IdempotencyKeyStore idempotencyStore;

    public CreateOrderUseCase(OrderRepository orderRepository, MessageBroker messageBroker,
                              PaymentGateway paymentGateway, IdempotencyKeyStore idempotencyStore) {
        this.orderRepository = orderRepository;
        this.messageBroker = messageBroker;
        this.paymentGateway = paymentGateway;
        this.idempotencyStore = idempotencyStore;
    }

    public Order execute(String idempotencyKey, OrderInput input) {
        if (idempotencyStore.isProcessed(idempotencyKey)) {
            return orderRepository.findById(input.id())
                    .orElseThrow(() -> new IllegalStateException("Transacao duplicada ou em processamento."));
        }

        Order order = new Order(input.id(), input.customerId(), input.amount(), OrderStatus.PENDING);
        orderRepository.save(order);

        try {
            PaymentResult result = paymentGateway.charge(order.id(), order.amount());
            order = order.withStatus(result.success() ? OrderStatus.PAID : OrderStatus.FAILED);
        } catch (Exception e) {
            order = order.withStatus(OrderStatus.FAILED);
            System.err.println("[Resiliencia] Erro ao chamar gateway de pagamento: " + e.getMessage());
        }

        orderRepository.save(order);
        idempotencyStore.markAsProcessed(idempotencyKey);
        messageBroker.publish("order.created", order);

        return order;
    }
}
