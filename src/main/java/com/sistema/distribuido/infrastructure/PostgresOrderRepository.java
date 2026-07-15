package com.sistema.distribuido.infrastructure;

import com.sistema.distribuido.domain.Order;
import com.sistema.distribuido.domain.OrderRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class PostgresOrderRepository implements OrderRepository {

    private final SpringDataOrderRepository jpaRepository;

    public PostgresOrderRepository(SpringDataOrderRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Order order) {
        System.out.println("[Database] Salvando no Postgres real -> Pedido ID: " + order.id() + " | Status: " + order.status());
        jpaRepository.save(OrderEntity.fromDomain(order));
    }

    @Override
    public Optional<Order> findById(String id) {
        return jpaRepository.findById(id).map(OrderEntity::toDomain);
    }

    @Override
    public java.util.List<Order> findAll() {
        return jpaRepository.findAll().stream()
                .map(OrderEntity::toDomain)
                .toList();
    }
}
