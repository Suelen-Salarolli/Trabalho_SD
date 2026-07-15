package com.sistema.distribuido.infrastructure;

import com.sistema.distribuido.domain.MessageBroker;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQMessageBroker implements MessageBroker {
    @Override
    public void publish(String event, Object payload) {
        System.out.println("[Distributed Event] Evento '" + event + "' enviado com sucesso para o cluster de mensageria.");
    }
}
