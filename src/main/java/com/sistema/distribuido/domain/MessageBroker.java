package com.sistema.distribuido.domain;

public interface MessageBroker {
    void publish(String event, Object payload);
}
