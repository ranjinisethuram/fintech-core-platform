# 💳 Fintech Platform — Event-Driven Microservices

A portfolio-grade fintech backend built with **Java, Spring Boot, Kafka, PostgreSQL, and Saga orchestration**.

The platform demonstrates an event-driven microservices architecture for customer onboarding and financial transactions, including:

* Customer onboarding
* Account and wallet management
* Transactions
* Double-entry ledger
* Saga orchestration
* Fraud evaluation
* Event-driven analytics
* Notifications
* AI assistant integration
* Outbox pattern
* Idempotent event processing
* Retry, recovery and compensation

---

# 🏗️ High-Level Architecture

```text
                              ┌───────────────────┐
                              │   Python AI       │
                              │   Assistant       │
                              │ FastAPI + Groq    │
                              └─────────┬─────────┘
                                        │
                                        ▼
                              ┌───────────────────┐
                              │  AI Tool Service  │
                              │ Java / Spring Boot│
                              └─────────┬─────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         FINTECH PLATFORM                            │
│                                                                     │
│  ┌──────────────┐   ┌──────────────┐   ┌────────────────────────┐ │
│  │ Customer     │   │ Account      │   │ Wallet                 │ │
│  │ Service      │   │ Service      │   │ Service                │ │
│  └──────────────┘   └──────────────┘   └────────────────────────┘ │
│                                                                     │
│  ┌────────────────────┐       ┌──────────────────────────────────┐ │
│  │ Transaction Service│       │ Ledger Service                   │ │
│  └─────────┬──────────┘       └──────────────────────────────────┘ │
│            │                                                        │
│            │ Outbox                                                 │
│            ▼                                                        │
│       ┌───────────┐                                                 │
│       │   Kafka   │                                                 │
│       └─────┬─────┘                                                 │
│             │                                                       │
│             ▼                                                       │
│  ┌─────────────────────────┐                                       │
│  │ Orchestration Service   │                                       │
│  │                         │                                       │
│  │ Saga State              │                                       │
│  │ Recovery                │                                       │
│  │ Retry                   │                                       │
│  │ Compensation            │                                       │
│  └────────────┬────────────┘                                       │
│               │                                                     │
│        ┌──────┼───────────────┐                                    │
│        ▼      ▼               ▼                                    │
│     Fraud   Wallet          Ledger                                 │
│    Service  Service         Service                                │
│                                                                     │
└───────────────────────┬─────────────────────────────────────────────┘
                        │
                        ▼
                 ┌──────────────┐
                 │    Kafka     │
                 └──────┬───────┘
                        │
              ┌─────────┼─────────┐
              ▼         ▼         ▼
         Analytics  Notification  Other
          Service      Service   Consumers
```

---

# 🧩 Services

## Customer Service

Responsible for:

* Customer creation
* Customer profile
* Customer-facing onboarding API

Customer creation initiates the onboarding Saga.

```text
POST /customers
       │
       ▼
Customer Service
       │
       ▼
CustomerCreatedEvent
       │
       ▼
Kafka
       │
       ▼
Orchestration Service
```

---

## Account Service

Responsible for:

* Account creation
* Account validation
* Account state
* Account activation

The service primarily participates in workflows through internal commands/events.

---

## Wallet Service

Responsible for:

* Wallet creation
* Wallet balance
* Fund reservation
* Fund release
* Fund movement

Wallet operations are coordinated by the transaction Saga.

---

## Ledger Service

Responsible for double-entry accounting.

Example:

```text
Debit:  USER_WALLET
Credit: PLATFORM_BANK
```

Ledger entries provide an accounting record independent of the wallet's operational balance.

---

## Transaction Service

Provides transaction APIs such as:

```text
Deposit
Withdraw
Transfer
```

The transaction service initiates the transaction workflow but does not orchestrate the entire business process.

---

# 🔄 Saga Orchestration

The platform uses **Saga orchestration** rather than distributed transactions.

```text
Transaction Service
       │
       ▼
TransactionInitiatedEvent
       │
       ▼
Kafka
       │
       ▼
Orchestration Service
       │
       ├── Evaluate Fraud
       │
       ├── Reserve Funds
       │
       ├── Execute Transaction
       │
       ├── Create Ledger Entries
       │
       └── Complete Transaction
```

The orchestration service owns:

* Saga state
* Current step
* Next step
* Retry state
* Recovery
* Compensation
* Saga timeout handling

Domain services own the actual business operations.

---

# 🔁 Saga Recovery

Transient failures do not immediately fail a Saga.

```text
Step
 │
 ├── Success ───────────────► Next Step
 │
 └── Failure
       │
       ▼
   Retryable?
    /     \
   Yes     No
   │        │
   ▼        ▼
 Retry     Compensation
   │        │
   ▼        ▼
Recovery  Previous Steps
```

Recovery is performed asynchronously through scheduled jobs.

Typical components include:

```text
SagaTimeoutJob
RetrySchedulerJob
SagaRecoveryService
```

---

# ↩️ Compensation

When a transaction cannot complete after a state-changing step, previously completed steps can be compensated.

Example:

```text
Reserve Funds
      │
      ▼
Fraud / Processing Failure
      │
      ▼
Compensation
      │
      ▼
Release Reserved Funds
```

Compensation is tracked independently from the forward execution path so that retries and partial failures can be handled safely.

---

# 📨 Event-Driven Communication

Kafka is used as the asynchronous communication backbone.

Typical events include:

```text
CustomerCreatedEvent
AccountCreatedEvent
WalletCreatedEvent
LedgerAccountCreatedEvent
TransactionInitiatedEvent
TransactionCompletedEvent
TransactionFailedEvent
FraudEvaluationCompletedEvent
```

Commands are used to request operations:

```text
CreateAccountCommand
CreateWalletCommand
CreateLedgerCommand
EvaluateFraudCommand
ReserveFundsCommand
ReleaseFundsCommand
```

---

# 📦 Outbox Pattern

Services use the Outbox pattern to reliably publish domain events.

```text
┌────────────────────────────┐
│       Service Database     │
│                            │
│ Business Data              │
│ Outbox Events              │
└─────────────┬──────────────┘
              │
              │ Poll
              ▼
       Outbox Publisher
              │
              ▼
            Kafka
```

The business state change and outbox record are persisted in the same database transaction.

This reduces the risk of:

```text
Database updated
      +
Kafka publish failed
```

---

# 🛡️ Idempotent Event Processing

Kafka consumers are designed to tolerate duplicate delivery.

A processed-message mechanism tracks event IDs:

```text
Event
 │
 ▼
Check eventId
 │
 ├── Already processed ──► Ignore
 │
 └── New
      │
      ▼
 Process event
      │
      ▼
 Save processed event
```

This is particularly important for:

* Saga events
* Analytics events
* Financial operations

---

# 🚨 Fraud Service

Fraud evaluation is implemented as a separate service.

```text
Transaction
     │
     ▼
Orchestration Service
     │
     ▼
EvaluateFraudCommand
     │
     ▼
Fraud Service
     │
     ├── Rule Engine
     │
     ├── Transaction History
     │
     └── Risk Features
     │
     ▼
FraudEvaluationResult
     │
     ▼
Orchestration Service
```

The initial fraud engine can use deterministic rules such as:

```text
High Transaction Amount
High Transaction Velocity
Unusual Transaction Amount
New Beneficiary
```

Example:

```text
Risk Score
    │
    ├── LOW
    ├── MEDIUM
    ├── HIGH
    └── CRITICAL
```

The fraud service owns fraud interpretation and risk evaluation.

The Saga owns workflow decisions based on the result.

---

# 📊 Analytics Service

Analytics is implemented as an asynchronous Kafka consumer.

It does not participate in transaction execution.

```text
TransactionCompletedEvent
            │
            ▼
          Kafka
            │
            ▼
    Analytics Service
            │
            ▼
       PostgreSQL
```

Initial reporting models can include:

```text
transaction_daily_summary
fraud_daily_summary
processed_analytics_event
```

Example APIs:

```text
GET /api/v1/analytics/transactions/daily
GET /api/v1/analytics/fraud/daily
```

This creates a separate read/analytics model instead of querying transactional databases directly.

---

# 🔔 Notification Service

Notifications are asynchronous side effects.

```text
TransactionCompletedEvent
          │
          ▼
        Kafka
          │
          ▼
Notification Service
          │
          ▼
Email / SMS / Push
```

Notification processing does not block the transaction Saga.

---

# 🤖 AI Assistant Integration

The backend exposes a controlled interface for the Python AI assistant.

```text
                    Python AI Assistant
                         │
                     Groq LLM
                         │
                    Tool Calling
                         │
                         ▼
               ┌───────────────────┐
               │  AI Tool Service  │
               │   Spring Boot     │
               └─────────┬─────────┘
                         │
             ┌───────────┼────────────┐
             ▼           ▼            ▼
          Account      Wallet     Transaction
          Service      Service       Service
             │           │            │
             └───────────┼────────────┘
                         │
                         ▼
                    Fraud / Analytics
```

The AI Tool Service acts as an **adapter**, not a replacement for the domain services.

---

# 🔐 AI Security Boundary

The LLM does not directly access the fintech database or internal services.

```text
LLM
 │
 │ tool request
 ▼
AI Tool Service
 │
 │ authorization
 │ customer/account validation
 ▼
Domain Service
```

The AI layer initially provides read-only operations such as:

```text
get_account_balance
get_transaction_history
get_transaction_status
get_transaction_risk
get_financial_insights
```

Financial operations can later be exposed through explicit approval and authorization workflows.

---

# 💰 Transaction Flow

A simplified transfer flow:

```text
Client
  │
  ▼
Transaction Service
  │
  ▼
TransactionInitiatedEvent
  │
  ▼
Outbox
  │
  ▼
Kafka
  │
  ▼
Orchestration Service
  │
  ▼
Evaluate Fraud
  │
  ▼
Fraud Service
  │
  ▼
Fraud Result
  │
  ▼
Reserve Funds
  │
  ▼
Wallet Service
  │
  ▼
Execute Transfer
  │
  ├──────────────► Source Wallet
  │
  └──────────────► Destination Wallet
  │
  ▼
Ledger Service
  │
  ▼
Double Entry
  │
  ▼
TransactionCompletedEvent
  │
  ├──────────────► Analytics
  │
  └──────────────► Notification
```

---

# 🧾 Ledger Model

The platform uses double-entry accounting.

For example, a simplified deposit:

```text
             Deposit
                │
        ┌───────┴────────┐
        ▼                ▼
 PLATFORM_BANK      USER_WALLET
     Credit             Debit
```

Ledger accounts can include:

```text
PLATFORM_BANK
USER_WALLET
PLATFORM_FEES
PAYMENT_CLEARING
WITHDRAWAL_CLEARING
REFUND_ACCOUNT
```

The exact ledger entries depend on the transaction type and accounting flow.

---

# 🗂️ Project Structure

```text
fintech-platform/
│
├── customer-service/
├── account-service/
├── wallet-service/
├── ledger-service/
├── transaction-service/
├── orchestration-service/
├── fraud-service/
├── notification-service/
├── analytics-service/
├── ai-tool-service/
│
├── common-lib/
├── common-logging/
├── common-security/
├── account-contract/
│
├── pom.xml
└── README.md
```

---

# 🧰 Technology Stack

| Technology       | Purpose                              |
| ---------------- | ------------------------------------ |
| Java             | Backend development                  |
| Spring Boot      | Microservices framework              |
| Spring Data JPA  | Persistence                          |
| PostgreSQL       | Transactional data                   |
| Apache Kafka     | Event streaming                      |
| Maven            | Build / dependency management        |
| Feign / HTTP     | Synchronous service communication    |
| Keycloak         | Authentication and authorization     |
| JWT              | Service/user security                |
| Outbox Pattern   | Reliable event publishing            |
| Saga             | Distributed transaction coordination |
| OpenTelemetry*   | Distributed tracing                  |
| Python + FastAPI | AI assistant                         |
| Groq             | LLM                                  |
| Docker*          | Local infrastructure                 |

`*` Components can be enabled depending on the deployment profile.

---

# 🔒 Security Architecture

Authentication and authorization are separated from business logic.

```text
Client
  │
  ▼
JWT / Identity Provider
  │
  ▼
API / Service
  │
  ▼
Authorization
  │
  ▼
Business Operation
```

Keycloak can provide:

* User authentication
* Client authentication
* Roles
* Scopes
* JWT tokens

Service-to-service communication can use internal authentication where required.

---

# 📈 Observability

The architecture is designed to support distributed tracing across:

```text
HTTP Request
     │
     ▼
Service
     │
     ▼
Kafka
     │
     ▼
Consumer
     │
     ▼
Downstream Service
```

Correlation/trace information can be propagated across synchronous and asynchronous boundaries.

---

# 🧠 Architectural Principles

### Domain ownership

Each service owns its domain state and business logic.

### Event-driven integration

Kafka is used where asynchronous communication and loose coupling are beneficial.

### Saga orchestration

Distributed business workflows are coordinated centrally without distributed database transactions.

### Reliable messaging

The Outbox pattern and idempotent consumers improve message reliability.

### Separation of concerns

```text
Domain Services
      │
      ├── Business Logic
      │
Saga Orchestrator
      │
      └── Workflow
      │
Kafka
      │
      └── Integration
      │
AI Tool Service
      │
      └── AI Adapter
      │
Analytics
      │
      └── Read Models
```

---

# 🚀 Getting Started

## Prerequisites

* Java 21+
* Maven
* PostgreSQL
* Apache Kafka
* Keycloak
* Python 3.x (for AI assistant)

---

## Build

```bash
mvn clean install
```

---

## Run infrastructure

The recommended local environment includes:

```text
PostgreSQL
Kafka
Keycloak
```

Infrastructure configuration can be provided through Docker Compose.

---

## Configuration

Each service can configure:

```text
Database URL
Kafka Bootstrap Servers
Security / Keycloak
Internal Service URLs
JWT configuration
```

Environment-specific configuration should be supplied through environment variables or external configuration.

---

# 🧪 Testing Strategy

The platform can be tested at multiple levels:

### Unit tests

Business rules and service logic.

### Integration tests

Database, Kafka and service boundaries.

### Contract tests

Shared API contracts between services.

### End-to-end tests

Examples:

```text
Customer Creation
       ↓
Account Creation
       ↓
Wallet Creation
       ↓
Ledger Creation
       ↓
Account Activation
```

and:

```text
Transfer
   ↓
Fraud
   ↓
Reserve
   ↓
Transfer
   ↓
Ledger
   ↓
Complete
```

---

# 🔮 Future Enhancements

Potential extensions include:

* ML-based fraud scoring
* ONNX-based fraud model inference
* MCP integration for AI tools
* AI-assisted financial insights
* Budgeting
* Personalized spending analysis
* Advanced reporting
* Event sourcing
* CQRS read models
* Kubernetes deployment
* Cloud deployment on AWS
* OpenTelemetry tracing
* Centralized observability
* API Gateway
* Distributed configuration

---

# 🎯 Project Goals

This project is designed to demonstrate practical experience with:

* Microservices architecture
* Event-driven architecture
* Saga orchestration
* Distributed transaction management
* Kafka
* Outbox pattern
* Idempotency
* Retry and recovery
* Compensation
* Double-entry accounting
* Fraud detection
* Analytics
* Secure service-to-service communication
* AI/LLM integration
* Cloud-ready architecture

The AI assistant is intentionally implemented as a **separate interaction layer over the fintech platform**, allowing AI capabilities to evolve independently while keeping the financial domain services deterministic, secure and independently deployable.
