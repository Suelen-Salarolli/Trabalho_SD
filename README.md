# Orders API - Trabalho de Sistemas Distribuidos

Aplicacao distribuida para demonstrar tolerancia a falhas, observabilidade e disponibilidade em Kubernetes com Chaos Mesh.

## Arquitetura

- `orders-api`: API Spring Boot responsavel por criar e consultar pedidos.
- `postgres-service`: banco de dados relacional usado pela API.
- `redis-service`: armazenamento de chaves de idempotencia para evitar processamento duplicado.

Mecanismos implementados:

- Probes de liveness/readiness via Spring Actuator.
- Metricas Prometheus em `/actuator/prometheus`.
- Retry, timeout e circuit breaker no gateway de pagamento.
- Fallback para degradacao controlada quando o pagamento remoto falha.
- Replicas da API e HPA baseado em CPU.
- Requests/limits de CPU e memoria para todos os componentes.
- Experimentos declarativos do Chaos Mesh para rede, pod kill e stress de CPU.

## Executar localmente

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Criar um pedido:

```powershell
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/orders `
  -Headers @{ "X-Idempotency-Key" = "demo-1" } `
  -ContentType "application/json" `
  -Body '{ "id": "order-1", "customerId": "customer-1", "amount": 99.90 }'
```

Consultar pedidos:

```powershell
Invoke-RestMethod http://localhost:8080/orders
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8080/actuator/prometheus
```

## Build Docker

```powershell
docker build -t orders-api:1.0.0 .
```

Se estiver usando Minikube:

```powershell
minikube image load orders-api:1.0.0
```

## Implantacao Kubernetes

```powershell
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/redis.yaml
kubectl apply -f k8s/api.yaml
kubectl get pods
kubectl get hpa
```

A API fica disponivel pelo NodePort `30080`:

```powershell
minikube service orders-api-service --url
```

## Chaos Mesh

Instale o Chaos Mesh no cluster seguindo a documentacao oficial. Depois aplique um experimento por vez:

```powershell
kubectl apply -f k8s/chaos/network-latency.yaml
kubectl apply -f k8s/chaos/pod-kill.yaml
kubectl apply -f k8s/chaos/cpu-stress.yaml
```

Observar impacto:

```powershell
kubectl get pods -w
kubectl logs deploy/orders-api-deployment -f
kubectl top pods
kubectl get hpa orders-api-hpa -w
```

Remover experimentos:

```powershell
kubectl delete -f k8s/chaos/network-latency.yaml
kubectl delete -f k8s/chaos/pod-kill.yaml
kubectl delete -f k8s/chaos/cpu-stress.yaml
```

## Entregaveis

- Codigo-fonte, Dockerfile e manifests Kubernetes: neste repositorio.
- Manifests Chaos Mesh: `k8s/chaos/`.
- Relatorio tecnico: `docs/relatorio-resiliencia.md` e `output/pdf/relatorio-resiliencia.pdf`.
