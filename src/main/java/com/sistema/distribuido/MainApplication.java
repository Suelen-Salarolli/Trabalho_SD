package com.sistema.distribuido;

import com.sistema.distribuido.domain.CreateOrderUseCase;
import com.sistema.distribuido.domain.IdempotencyKeyStore;
import com.sistema.distribuido.domain.MessageBroker;
import com.sistema.distribuido.domain.OrderRepository;
import com.sistema.distribuido.domain.PaymentGateway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @Bean
    public CreateOrderUseCase createOrderUseCase(
            OrderRepository orderRepository,
            MessageBroker messageBroker,
            PaymentGateway paymentGateway,
            IdempotencyKeyStore idempotencyStore) {
        return new CreateOrderUseCase(orderRepository, messageBroker, paymentGateway, idempotencyStore);
    }

    @Bean
    @org.springframework.context.annotation.Profile("!local")
    public IdempotencyKeyStore redisIdempotencyStore(
            @org.springframework.beans.factory.annotation.Value("${spring.data.redis.host:localhost}") String host,
            @org.springframework.beans.factory.annotation.Value("${spring.data.redis.port:6379}") int port,
            @org.springframework.beans.factory.annotation.Value("${spring.data.redis.password:}") String password,
            @org.springframework.beans.factory.annotation.Value("${app.idempotency.ttl-seconds:86400}") int ttlSeconds) {
        return new com.sistema.distribuido.infrastructure.RedisIdempotencyStore(host, port, password, ttlSeconds);
    }

    @Bean
    @org.springframework.context.annotation.Profile("local")
    public IdempotencyKeyStore inMemoryIdempotencyStore() {
        return new IdempotencyKeyStore() {
            private final java.util.Set<String> store = java.util.concurrent.ConcurrentHashMap.newKeySet();
            @Override
            public boolean isProcessed(String key) {
                return store.contains(key);
            }
            @Override
            public void markAsProcessed(String key) {
                store.add(key);
            }
        };
    }

    @Bean
    public org.springframework.web.client.RestTemplate restTemplate() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(1000); // 1 segundo
        factory.setReadTimeout(1500);    // 1.5 segundos
        return new org.springframework.web.client.RestTemplate(factory);
    }
}
