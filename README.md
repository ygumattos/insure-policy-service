# Insure Policy Service 🛡️🚀

Serviço para **recebimento, validação e ciclo de vida** de solicitações de apólice, com integração de **fraude (WireMock)**, **persistência (Postgres + Flyway)** e **mensageria (Kafka / Redpanda)**.  
Arquitetura **Clean/Hexagonal**, foco em **SOLID, testes e observabilidade**.
---

## Sumário

- [Stack](#stack)
- [Arquitetura (resumo)](#arquitetura-resumo)
- [Subindo tudo com Docker](#subindo-tudo-com-docker)
- [Perfis e propriedades](#perfis-e-propriedades)
- [Endpoints principais](#endpoints-principais)
- [Mensageria (Kafka/Redpanda)](#mensageria-kafkaredpanda)
- [Como testar](#como-testar)

---

## Stack

- **JDK 17**, **Kotlin**
- **Spring Boot 3**, **Spring Web**, **Spring Data JPA**
- **PostgreSQL 15** + **Flyway**
- **Kafka** via **Redpanda** + **Redpanda Console**
- **WireMock** (mock do serviço de fraude)
- **Gradle** (build), **JaCoCo** (coverage)
- **Docker / Docker Compose**
- **IntelliJ IDEA** (recomendado)
- **Insomnia** (ou Postman) para testar a API

---

## Arquitetura (resumo)

- **Clean Architecture / Hexagonal**  
  Ports & Adapters:
    - `application` (use cases)
    - `domain` (entidades, regras)
    - `adapters` (HTTP, Kafka, DB/JPA, WireMock)

Use cases principais:
- `ManagePolicyLifecycleUseCase` (criação, consulta, cancelamento)
- `EvaluatePolicyUseCase` (regras por classificação/categoria)
- `PolicyStatusUseCase` (transições RECEIVED → VALIDATED → PENDING → APPROVED/REJECTED/CANCELLED)

Mensageria:
- Producer: eventos de **status-changed**
- Consumers: **payment-events** e **subscription-events** (atualizam flags e podem aprovar a apólice)

---

## Subindo tudo com Docker

Pré-requisitos:
- Docker e Docker Compose
- Portas livres: **8080** (API), **8089** (WireMock), **5432** (Postgres), **19092/9092** (Kafka), **8087** (Redpanda Console)

1) **Build do projeto**
```bash
./gradlew clean build -x test
```
2) **Subir infraestrutura + app**
```bash
docker compose up -d
```
3) **Acessos**
API: http://localhost:8080
WireMock: http://localhost:8089/__admin
Redpanda Console: http://localhost:8087/overview
Postgres: localhost:5432 (db insurance_db, user user, pass password)
Flyway aplica as migrations automaticamente na subida da app.

## Perfis e propriedades
- application-docker.yml: usado no Compose (SPRING_PROFILES_ACTIVE=docker).
- application-local.yml: desenvolvimento local sem Compose.
- application-test.yml: testes (JUnit).
______________________________________________________________________

## Endpoints principais

Base path: `/api/v1`
### Criar Policy
`POST /policies`
```json
{
  "customerId": "21000000-0000-0000-0000-000000000000",
  "productId": "P-1",
  "category": "RESIDENCIAL",
  "salesChannel": "WEB",
  "paymentMethod": "CARD",
  "totalMonthlyPremiumAmount": 100.00,
  "insuredAmount": 50000.00,
  "coverages": { "BASIC": 1000.00 },
  "assistances": ["ASSIST-1"]
}
```
**Classificação de fraude (WireMock) por prefixo de customerId:**
21^ → REGULAR
22^ → HIGH_RISK
23^ → PREFERENTIAL
24^ → NO_INFORMATION
44^ → NOT_FOUND (404)
55^ → SERVER_ERROR (500)

### Buscar por ID
GET `/policies/{id}`

### Cancelar Policy
DELETE `/policies/{id}`

________________________________________
## Mensageria (Kafka/Redpanda)

Tópicos:
- Producer:
  - status-changed-events
- Consumers:
  - payment-events → paymentConfirmation 
    - ```json
      { "id": "UUID", "paymentConfirmation": true }
      ```
  - subscription-events → subscriptionAutorization
    - ```json
      { "id": "UUID", "subscriptionAutorization": true }
      ```

Console: http://localhost:8087/overview
_________________________________________

## Como testar

- Criar Policy:
```curl
curl --request POST \
  --url http://localhost:8080/api/v1/policies/create \
  --header 'Content-Type: application/json' \
  --header 'User-Agent: insomnia/11.6.1' \
  --data '{
  "customerId": "23c56d77-348c-4bf0-908f-22d402ee715c",
  "productId": "1b2da7cc-b367-4196-8a78-9cfeec21f587",
  "category": "Teste",
  "salesChannel": "MOBILE",
  "paymentMethod": "CREDIT_CARD",
  "totalMonthlyPremiumAmount": 75.25,
  "insuredAmount": 375000.00,
  "coverages": {
    "Roubo": 100000.25,
    "Perda Total": 100000.25,
    "Colisão com Terceiros": 75000.00
  },
  "assistances": [
    "Guincho até 250km",
    "Troca de Óleo",
    "Chaveiro 24h"
  ]
}'
```
- Buscar policy
```curl
curl --request GET \
  --url http://localhost:8080/api/v1/policies/7779b743-19f7-4a73-b0c1-057519def2e4 \
  --header 'User-Agent: insomnia/11.6.2'
```
- Cancelar policy 
```curl
curl --request DELETE \
  --url http://localhost:8080/api/v1/policies/7779b743-19f7-4a73-b0c1-057519def2e4 \
  --header 'User-Agent: insomnia/11.6.2'
```
