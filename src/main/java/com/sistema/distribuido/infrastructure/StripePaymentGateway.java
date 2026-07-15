package com.sistema.distribuido.infrastructure;

import com.sistema.distribuido.domain.PaymentGateway;
import com.sistema.distribuido.domain.PaymentResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class StripePaymentGateway implements PaymentGateway {

    private final RestTemplate restTemplate;
    private final String paymentServiceUrl;
    private final boolean remotePaymentEnabled;

    public StripePaymentGateway(
            RestTemplate restTemplate,
            @Value("${app.payment-service.url}") String paymentServiceUrl,
            @Value("${app.payment-service.enabled:false}") boolean remotePaymentEnabled) {
        this.restTemplate = restTemplate;
        this.paymentServiceUrl = paymentServiceUrl;
        this.remotePaymentEnabled = remotePaymentEnabled;
    }

    @Override
    @Retry(name = "paymentService", fallbackMethod = "fallbackCharge")
    @CircuitBreaker(name = "paymentService", fallbackMethod = "fallbackCharge")
    public PaymentResult charge(String orderId, double amount) {
        if (!remotePaymentEnabled) {
            System.out.println("[Payment] Pagamento simulado aprovado para o pedido " + orderId + ".");
            return new PaymentResult(true, "tx_simulated_" + orderId);
        }

        System.out.println("[Payment] Chamando servico externo de pagamento...");
        ResponseEntity<PaymentResult> response = restTemplate.postForEntity(
                paymentServiceUrl + "/payments",
                new PaymentRequest(orderId, amount),
                PaymentResult.class);

        PaymentResult result = response.getBody();
        if (result == null) {
            throw new IllegalStateException("Payment service returned an empty response.");
        }
        return result;
    }

    public PaymentResult fallbackCharge(String orderId, double amount, Throwable cause) {
        System.err.println("[Resiliencia] Pagamento indisponivel para pedido " + orderId
                + ". Fallback acionado: " + cause.getMessage());
        return new PaymentResult(false, "fallback-unavailable");
    }

    private record PaymentRequest(String orderId, double amount) {
    }
}
