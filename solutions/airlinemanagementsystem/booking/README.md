# Booking Service - Airline Management System

The **Booking Service** is the central orchestration engine of the Airline Management System. It manages high-throughput booking requests, passenger data, luggage logistics, and coordinates distributed transactions between the **Flight Service** (Inventory) and **Notification Service**.

It is designed for **Resilience** and **High Availability** using an **Asynchronous Event-Driven Architecture**.

---

## Tech Stack

We chose specific technologies to handle the concurrency and reliability requirements of an airline system.

| Component | Technology | Why we used it? |
| :--- | :--- | :--- |
| **Framework** | Spring Boot 3.2 | Rapid development, dependency injection, and robust ecosystem for microservices. |
| **Messaging** | Apache Kafka | **Load Leveling:** To buffer thousands of incoming booking requests so the database doesn't crash during traffic spikes. |
| **Database** | MySQL 8.0 | **ACID Compliance:** Essential for financial transactions and booking records where data integrity is non-negotiable. |
| **Distributed Lock** | Redis (via Flight Service) | **Idempotency:** To ensure a specific seat (e.g., "12A") cannot be booked by two people simultaneously. |
| **Communication** | OpenFeign | **Declarative REST Client:** simplifies synchronous calls to the Flight Service for locking seats. |
| **Documentation** | OpenAPI (Swagger) | Auto-generated API documentation for frontend teams. |
| **Build Tool** | Maven | Standard dependency management and build lifecycle. |

---

## System Design & Architecture

The Booking Service addresses the **"Thundering Herd"** problem, where thousands of users might try to book popular flights at the exact same moment. Instead of a traditional synchronous (blocking) architecture, we implemented a **Queue-Based Load Leveling** architecture.

### High-Level Data Flow
1.  **Ingestion (API Layer):** The user submits a booking request. The Controller validates the input and pushes the event to Kafka (`booking-request-topic`). It returns `202 Accepted` immediately (Non-blocking).
2.  **Processing (Consumer Layer):** A Kafka Consumer processes messages one by one (or in batches). It acts as a "Saga Orchestrator".
3.  **Distributed Locking:** The Consumer calls the **Flight Service** to lock the requested seats in Redis.
4.  **Persistence:** If the lock is acquired, the service saves the Booking, Passengers, and Luggage to MySQL in a single atomic transaction.
5.  **Compensation:** If the seat is taken or an error occurs, the system triggers a "Failure Event" (sends a failure email) and ensures no partial data is saved.

---

## Design Patterns Implemented

| Pattern | Where it is used | Reason for adoption |
| :--- | :--- | :--- |
| **Queue-Based Load Leveling** | Kafka Producer/Consumer | To decouple the ingestion rate from the processing rate. This prevents the database from being overwhelmed during traffic surges. |
| **Saga Pattern (Orchestration)** | `BookingService` | To manage distributed transactions across microservices. If one step fails (e.g., Seat Lock), we execute compensating actions (Rollback & Notify). |
| **Distributed Locking** | Integration with Redis | To prevent **Race Conditions** and ensure data consistency in a distributed environment (Double Booking Prevention). |
| **Facade Pattern** | `BookingController` | Provides a simple interface for the client (`POST /booking`) while hiding the complex background complexity (Kafka, Redis, SQL). |
| **Domain-Driven Design (DDD)** | `Luggage` Module | Modeled complex business logic (Luggage Tracking, Audit History) using rich entities rather than anemic CRUD data structures. |

---

## Key Features

### 1. Asynchronous Booking Engine
* **Non-Blocking API:** Returns `202 Accepted` in <50ms, improving user experience.
* **Idempotency:** Guarantees that submitting the same request twice does not result in double charges or bookings.

### 2. Baggage Reconciliation System (BRS)
A real-world tracking module for luggage logistics.
* **Tag Generation:** Auto-generates unique IATA-style barcodes (e.g., `TAG-A1B2C3`).
* **Audit Trail:** Tracks every scan event (`CHECKED_IN` -> `LOADED` -> `ON_CAROUSEL`).
* **Staff API:** dedicated endpoints for ground crew to scan bags at different checkpoints.

### 3. Resilience & Notifications
* **Circuit Breaking:** Handles downstream failures gracefully (e.g., if Flight Service is down).
* **Automated Emails:** Integrates with Gmail SMTP to send booking confirmations and failure reasons.

---

## API Reference

### Booking Operations
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/bookings` | Submit a new booking request (Async). |
| `GET` | `/api/v1/bookings/status/{userId}` | Check the status of the latest booking (`PENDING`, `CONFIRMED`, `FAILED`). |

### Luggage Operations (Ground Staff)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/luggage/{tag}/scan` | Update bag status (e.g., `LOADED`) & record location history. |
| `GET` | `/api/v1/luggage/{tag}` | Get current status and full history of a bag. |

---

## Setup & Installation

### Prerequisites
* Java 17+
* Docker & Docker Compose (for Kafka, MySQL, Redis)
* Flight Service running on port `8081`

### 1. Infrastructure Setup
Ensure your distributed infrastructure is running:
```bash
docker-compose up -d
```

### 2. Configuration
Update src/main/resources/application.properties with your credentials:

### Properties
```
# Kafka
spring.kafka.bootstrap-servers=localhost:9092

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/booking_service_db

# Email (Optional for testing)
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### 3. Run the Service
```Bash
mvn spring-boot:run
```
The service will start on Port **8082**.

```Swagger UI: http://localhost:8082/swagger-ui.html```

```Health Check: http://localhost:8082/actuator/health```