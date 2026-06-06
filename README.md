# auth-service

**Version:** 2.0.1 | **Java:** 17 | **Spring Boot:** 3.2 | **DB:** PostgreSQL 14

Authentication microservice — Forgot Password OTP flow.

---

## AI in Run — Session 1 Demo (QA Scenario)

This repository is the **QA team demo codebase** for Session 1.

### Jira tickets
| Ticket | Description |
|--------|-------------|
| [SCRUM-6](https://91ranamanjeet.atlassian.net/browse/SCRUM-6) | Epic — AI in Run Session 1 Demo |
| [SCRUM-7](https://91ranamanjeet.atlassian.net/browse/SCRUM-7) | QA demo — OTP test suite (CRISPE prompt) |
| [SCRUM-10](https://91ranamanjeet.atlassian.net/browse/SCRUM-10) | Known bug — lockout counter not reset (INC-1987) |
| [SCRUM-13](https://91ranamanjeet.atlassian.net/browse/SCRUM-13) | Story — QA engineer generates test suite using AI |

### Known Bug — SCRUM-10 / INC-1987

`OtpService.verifyOtp()` does not reset `attemptCount` when lockout expires.
After the 15-minute lockout period ends, users are never locked out again on subsequent failures.

**Seeded at:** `OtpSession.isLocked()` and `OtpService.verifyOtp()` lines 98–105.

Claude's CRISPE prompt (SCRUM-7) explicitly flags this in `TC-OTP-008` as `[EXPECTED FAIL]`.

### OTP Flow

```
POST /api/v2/auth/forgot-password  { email }
  → generates 6-digit OTP
  → stores BCrypt hash (10-min expiry)
  → sends OTP email

POST /api/v2/auth/verify-otp  { email, otp }
  → validates hash + expiry + lockout
  → returns JWT on success
  → 400 on invalid/expired
  → 429 on lockout (3 failed attempts)
```

### Project Structure

```
auth-service/
├── src/main/java/com/payment/auth/
│   ├── controller/AuthController.java    ← REST endpoints
│   ├── service/OtpService.java          ← OTP logic (bug seeded lines 98-105)
│   ├── service/EmailService.java        ← Email stub
│   ├── model/OtpSession.java            ← Session model (bug in isLocked())
│   ├── repository/OtpSessionRepository  ← JPA repo interface
│   └── exception/                       ← Typed exceptions
└── src/test/java/com/payment/auth/
    └── service/OtpServiceTest.java      ← Partial tests (gaps intentional)
```
