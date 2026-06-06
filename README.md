# payment-service

**Version:** 2.4.1 | **Java:** 17 | **Spring Boot:** 3.2 | **DB:** PostgreSQL 14

Payment processing microservice for the AI in Run demo programme.

---

## AI in Run — Session 1 Demo

This repository is the **Dev team demo codebase** for Session 1: Foundations + Prompt Engineering.

The branch `feature/payment-processor-v2` contains PR #847 — the code reviewed live during the session using Claude's CRISPE prompt framework.

### Jira tickets
| Ticket | Description |
|--------|-------------|
| [SCRUM-6](https://91ranamanjeet.atlassian.net/browse/SCRUM-6) | Epic — AI in Run Session 1 Demo |
| [SCRUM-5](https://91ranamanjeet.atlassian.net/browse/SCRUM-5) | AMU scenario — Payment API 503 incident |
| [SCRUM-7](https://91ranamanjeet.atlassian.net/browse/SCRUM-7) | QA scenario — OTP test suite generation |
| [SCRUM-8](https://91ranamanjeet.atlassian.net/browse/SCRUM-8) | Dev scenario — PR #847 security code review |

### What the demo shows
1. Connect Claude to this GitHub repo via the GitHub connector
2. Reference PR #847 — Claude reads the full diff automatically, zero copy-paste
3. Run the CRISPE code review prompt from SCRUM-8
4. Watch Claude find CRITICAL bugs including the PCI-DSS card logging violation
5. Run the reflection loop — surfaces the idempotency double-payment gap

### Intentional bugs in PaymentProcessor.java (PR #847)
These are deliberately seeded for the demo. Claude finds them during the review.

| Severity | Line | Bug |
|----------|------|-----|
| CRITICAL | 47 | `getCardNumber()` logged in plain text — PCI-DSS violation |
| CRITICAL | 61 | No idempotency check — double-payment risk on retry |
| CRITICAL | 89 | `paymentDetails` null check missing — NPE in open transaction |
| HIGH | 102 | DB connection not closed in catch — pool exhaustion risk |
| HIGH | 115 | `AuditWriteException` swallowed — compliance audit trail dropped |
| MEDIUM | 34 | SRP violation — email + audit in same class as payment logic |
| LOW | 78 | Magic number `3` — should be named constant |

---

## Project Structure

```
payment-service/
├── src/main/java/com/payment/
│   ├── processor/
│   │   └── PaymentProcessor.java      ← Main class with bugs (PR #847)
│   ├── model/
│   │   ├── PaymentDetails.java         ← PCI-DSS sensitive — see getMaskedReference()
│   │   └── PaymentResult.java
│   ├── exception/
│   │   ├── PaymentGatewayException.java
│   │   └── AuditWriteException.java   ← ALWAYS re-throw this
│   ├── audit/
│   │   └── AuditLogger.java           ← PCI-DSS compliance writer
│   ├── gateway/
│   │   └── PaymentGateway.java        ← External gateway stub
│   └── notification/
│       └── NotificationService.java   ← Email stub
├── src/test/java/com/payment/
│   └── processor/
│       └── PaymentProcessorTest.java  ← Partial coverage (gaps intentional for demo)
├── PR_DESCRIPTION.md                  ← PR context Claude reads via GitHub connector
└── pom.xml
```

---

## Running Locally

```bash
# Requires Java 17 + PostgreSQL 14
mvn clean install
mvn spring-boot:run
```

Configure `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/payment_db
spring.datasource.username=payment_user
spring.datasource.password=your_password
spring.datasource.hikari.maximum-pool-size=100
```
