# Finance Data Processing & Access Control Backend (FDPACB)

A production-quality **Spring Boot 3.4** REST API backend for a Finance Dashboard system, featuring JWT authentication, role-based access control (RBAC), financial record management, and dashboard analytics.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.4 / Spring Web MVC |
| Security | Spring Security 6 + JWT (JJWT 0.12) |
| Rate Limiting | Bucket4j Token Bucket |
| Database | H2 (file-based, zero external setup) |
| ORM | Spring Data JPA + Hibernate |
| Validation | Jakarta Bean Validation |
| API Docs | SpringDoc OpenAPI 2 / Swagger UI |
| Build | Gradle 8 |
| Java | Java 17 |
| Tests | JUnit 5 + Spring MockMvc |

---

## Quick Start

**Prerequisites:** Java 17+

```bash
# Clone or unzip, then run:
./gradlew bootRun          # Windows: gradlew.bat bootRun
```

The application starts on **http://localhost:8080**

> **Note:** On first startup, the database is auto-created at `./data/fdpacb.mv.db` and seeded with sample data.

---

## Default Credentials

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `analyst` | `analyst123` | ANALYST |
| `viewer` | `viewer123` | VIEWER |

---

## Developer Tools

| URL | Description |
|---|---|
| http://localhost:8080/swagger-ui.html | Interactive API documentation (Swagger UI) |
| http://localhost:8080/h2-console | H2 database console (JDBC URL: `jdbc:h2:file:./data/fdpacb`) |

---

## API Overview

### Authentication (Public)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Login and receive a JWT token |

**Login example:**
```json
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```
**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "username": "admin",
  "role": "ADMIN",
  "expiresInMs": 86400000
}
```

Use the token in subsequent requests:
```
Authorization: Bearer <token>
```

---

### User Management (ADMIN only)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/users` | List all users |
| `GET` | `/api/users/{id}` | Get user by ID |
| `PUT` | `/api/users/{id}` | Update user role or active status |
| `DELETE` | `/api/users/{id}` | Deactivate a user (soft) |

---

### Financial Records

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/records` | ANALYST, ADMIN | Create a record |
| `GET` | `/api/records` | ALL | List records (paginated + filtered) |
| `GET` | `/api/records/{id}` | ALL | Get record by ID |
| `PUT` | `/api/records/{id}` | ADMIN | Update a record |
| `DELETE` | `/api/records/{id}` | ADMIN | Soft-delete a record |

**Filter parameters for `GET /api/records`:**

| Parameter | Type | Example |
|---|---|---|
| `type` | `INCOME` or `EXPENSE` | `?type=INCOME` |
| `category` | See Category enum | `?category=SALARY` |
| `from` | `yyyy-MM-dd` | `?from=2025-01-01` |
| `to` | `yyyy-MM-dd` | `?to=2025-12-31` |
| `search` | string | `?search=salary` |
| `page` | integer (0-based) | `?page=0` |
| `size` | integer | `?size=10` |

**Create record example:**
```json
POST /api/records
{
  "amount": 85000.00,
  "type": "INCOME",
  "category": "SALARY",
  "date": "2025-04-01",
  "notes": "April salary"
}
```

---

### Dashboard Analytics

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/dashboard/summary` | ALL | Full summary (income, expense, net, categories, trend, recent) |
| `GET` | `/api/dashboard/category-totals` | ANALYST, ADMIN | Spend per category |
| `GET` | `/api/dashboard/monthly-trends` | ANALYST, ADMIN | Last 6 months income/expense trend |
| `GET` | `/api/dashboard/recent-activity` | ALL | Last 10 transactions |

**Sample summary response:**
```json
{
  "totalIncome": 265000.00,
  "totalExpense": 40300.00,
  "netBalance": 224700.00,
  "categoryTotals": {
    "SALARY": 241000.00,
    "INVESTMENT": 23000.00,
    "FOOD": 6300.00
  },
  "monthlyTrend": [...],
  "recentActivity": [...]
}
```

---

## Access Control Matrix

| Endpoint | VIEWER | ANALYST | ADMIN |
|---|:---:|:---:|:---:|
| `POST /api/auth/register` | ✅ | ✅ | ✅ |
| `POST /api/auth/login` | ✅ | ✅ | ✅ |
| `GET /api/records` | ✅ | ✅ | ✅ |
| `GET /api/records/{id}` | ✅ | ✅ | ✅ |
| `POST /api/records` | ❌ | ✅ | ✅ |
| `PUT /api/records/{id}` | ❌ | ❌ | ✅ |
| `DELETE /api/records/{id}` | ❌ | ❌ | ✅ |
| `GET /api/dashboard/summary` | ✅ | ✅ | ✅ |
| `GET /api/dashboard/category-totals` | ❌ | ✅ | ✅ |
| `GET /api/dashboard/monthly-trends` | ❌ | ✅ | ✅ |
| `GET /api/dashboard/recent-activity` | ✅ | ✅ | ✅ |
| `GET/PUT/DELETE /api/users/**` | ❌ | ❌ | ✅ |

Access control is enforced via Spring Security's `@PreAuthorize` method-level annotations.

---

## Enums Reference

**RecordType:** `INCOME`, `EXPENSE`

**Category:** `SALARY`, `INVESTMENT`, `FOOD`, `TRANSPORT`, `UTILITIES`, `ENTERTAINMENT`, `HEALTHCARE`, `OTHER`

**Role:** `VIEWER`, `ANALYST`, `ADMIN`

---

## Error Response Format

All errors return a consistent JSON structure:

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "amount": "Amount is required",
    "type": "Type is required (INCOME or EXPENSE)"
  },
  "timestamp": "2025-04-03T10:00:00"
}
```

| Status | Meaning |
|---|---|
| `400` | Validation failed (field-level errors in `errors` map) |
| `401` | Invalid credentials or expired token |
| `403` | Authenticated but insufficient role |
| `404` | Resource not found |
| `409` | Conflict (e.g., duplicate username/email) |
| `429` | Too Many Requests (Rate limit exceeded) |
| `500` | Unexpected server error |

---

## Running Tests

```bash
./gradlew test
```

Tests use an in-memory H2 database (`@ActiveProfiles("test")`) and are isolated from dev data.

---

## Project Structure

```
src/main/java/com/FDPACB/FDPACB/
├── config/          # SecurityConfig, JpaConfig, OpenApiConfig, RateLimitingInterceptor, WebMvcConfig, DataSeeder
├── controller/      # AuthController, UserController, FinancialRecordController, DashboardController
├── dto/             # auth/, user/, record/, dashboard/ DTOs
├── entity/          # User, FinancialRecord
├── enums/           # Role, RecordType, Category
├── exception/       # ResourceNotFoundException, ConflictException, GlobalExceptionHandler
├── repository/      # UserRepository, FinancialRecordRepository
├── security/        # JwtUtil, JwtAuthenticationFilter, CustomUserDetailsService
└── service/         # AuthService, UserService, FinancialRecordService, DashboardService
```

---

## Assumptions & Design Decisions

1. **H2 file-based database** — chosen for zero-setup evaluation. Data persists across restarts in `./data/fdpacb.mv.db`. A simple environment variable change to the JDBC URL swaps this for PostgreSQL or MySQL in production.

2. **Self-registration with role selection** — allowed for assessment convenience. In production, self-registration would be restricted to VIEWER with admin promotion flow.

3. **Soft delete** — financial records are flagged `deleted=true` rather than physically removed. This enables audit history and prevents accidental data loss.

4. **Stateless JWT auth** — no sessions or server-side token storage. Token expiry is 24 hours. For production, consider refresh tokens and a token revocation blacklist.

5. **Rate Limiting** — Every client is limited to 50 API requests per minute using the Bucket4j token-bucket algorithm, protecting the API from abuse.

6. **BCrypt password hashing** — all passwords stored as BCrypt hashes (strength 10).

7. **Pagination & Searching** — defaults to `page=0, size=10` for all list endpoints. Native SQL queries efficiently support combining pagination with multiple AND filters including a free-text `search` parameter.

8. **Spring Boot 3.4** (not 4.0) — Spring Boot 4.0 is not yet released as a stable GA version. Spring Boot 3.4 with Spring Security 6 and Jakarta EE 10 is the current production-ready LTS version.
