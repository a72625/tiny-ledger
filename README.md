# Tiny Ledger 🏦

A high-performance, in-memory financial ledger system built with **Kotlin** and **Spring Boot 4.x**. 
This project demonstrates a robust architecture for handling concurrent money movements with atomic integrity and 
memory-efficient pagination.

```bash
# To run the application
./gradlew bootRun
```


## Key Features
* Ability to record money movements (ie: deposits and withdrawals)
* View current balance
* View transaction history

## 🚀 Implementation details

* **Atomic Transactions:** Uses `ConcurrentHashMap` and `compute` blocks to ensure balance integrity and prevent race conditions without heavy locking.
* **Memory Efficiency:** Implements lazy pagination using Kotlin `Sequences` to handle accounts with millions of transactions with constant memory overhead.
* **LIFO Journaling:** Optimized for storing transactions in a `ConcurrentLinkedDeque` for $O(1)$ newest-first retrieval.

---

## 🛠 Tech Stack

| Component | Technology                           |
| :--- |:-------------------------------------|
| **Language** | Kotlin 2.2.x                         |
| **Framework** | Spring Boot 4.0.x (Servlet Stack)    |
| **Documentation** | OpenAPI 3.0 / Swagger UI             |
| **Validation** | Jakarta Validation (Bean Validation) |
| **Testing** | JUnit 5, Mockito, MockMvc            |
| **Build Tool** | Gradle (Kotlin DSL)                  |

---

## 📖 API Reference

The API is versioned under `/v1` and follows RESTful principles.

Swagger endpoint with full documentation:
`/swagger-ui/index.html`

### Accounts
* `POST /v1/accounts` - Opens a new ledger account.
* `GET /v1/accounts/{id}/balances` - Retrieves the current balance.

### Transactions
* `POST /v1/accounts/{id}/transactions` - Records a money movement (Positive = Deposit, Negative = Withdrawal).
* `GET /v1/accounts/{id}/transactions` - Retrieves a paged list of transaction history with rolling balance.




---

## 🧪 Testing

The project maintains high coverage across all layers:

* **Domain Unit Tests:** Validates business rules (precision limits, non-zero checks).
* **Repository Tests:** Validates concurrency safety and pagination math.
* **Controller Tests:** Validates HTTP status codes and API contract integrity using `MockMvc`.

```bash
# Run all tests
./gradlew clean test
```