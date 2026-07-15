package com.sistema.distribuido.domain;

public interface IdempotencyKeyStore {
    boolean isProcessed(String key);
    void markAsProcessed(String key);
}
