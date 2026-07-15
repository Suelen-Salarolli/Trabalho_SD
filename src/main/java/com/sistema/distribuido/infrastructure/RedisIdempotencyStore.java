package com.sistema.distribuido.infrastructure;

import com.sistema.distribuido.domain.IdempotencyKeyStore;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisIdempotencyStore implements IdempotencyKeyStore {

    private final JedisPool jedisPool;
    private final int ttlSeconds;

    public RedisIdempotencyStore(
            @Value("${spring.data.redis.host:localhost}") String host,
            @Value("${spring.data.redis.port:6379}") int port,
            @Value("${spring.data.redis.password:}") String password,
            @Value("${app.idempotency.ttl-seconds:86400}") int ttlSeconds) {
        
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(10);
        
        if (password == null || password.isBlank()) {
            this.jedisPool = new JedisPool(config, host, port);
        } else {
            this.jedisPool = new JedisPool(config, host, port, 2000, password);
        }
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public boolean isProcessed(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        }
    }

    @Override
    public void markAsProcessed(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, ttlSeconds, "processed");
        }
    }
}
