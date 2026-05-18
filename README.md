# Payment Gateway

A Spring Boot payment gateway implementation for the Checkout.com engineering challenge.

The application validates payment requests, forwards valid payments to a simulated acquiring bank, and stores payment results for later retrieval.

Supported payment outcomes:

- Authorized
- Declined
- Rejected

---

## Tech Stack

- Java 17
- Spring Boot
- Gradle
- JUnit 5
- Mockito
- Docker
- OpenAPI / Swagger UI

---

## Requirements

- JDK 17
- Docker

---

## Running the Application

### 1. Start the bank simulator

```bash
docker compose up
```

The bank simulator runs on:

```text
http://localhost:8080
```

---

### 2. Start the Spring Boot application

Mac/Linux:

```bash
./gradlew bootRun
```

Windows:

```powershell
.\gradlew.bat bootRun
```

The application runs on:

```text
http://localhost:8090
```

---

## API Documentation

Swagger UI:

```text
http://localhost:8090/swagger-ui/index.html
```

---

## API Endpoints

### Process Payment

```http
POST /api/payments
```

Example request:

```json
{
  "card_number": "4111111111111111",
  "expiry_month": 12,
  "expiry_year": 2027,
  "currency": "USD",
  "amount": 1050,
  "cvv": "123"
}
```

---

### Retrieve Payment

```http
GET /api/payments/{id}
```

---


## Running Tests

Run all tests:

```bash
./gradlew test
```

---

## Notes

- Full card numbers are never returned in API responses
- Only the last four digits are exposed
- CVV values are never persisted
- Payments are stored in-memory using `ConcurrentHashMap`
- The bank simulator configuration under `imposters/` was left unchanged