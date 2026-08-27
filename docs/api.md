# RestaurantHub — REST API Specification

This document provides a human-readable reference for the RestaurantHub REST API, its security architecture, request/response models, and endpoint catalog.

---

## 1. Architecture & Protocol Baseline

- **Base URL (Local Development)**: `http://localhost:8080/api/v1`
- **Base URL (Production Same-Origin)**: `https://<restaurant-domain>/api/v1`
- **Media Assets Base Path**: `/media/**`
- **Data Exchange Format**: `application/json` (UTF-8)
- **Multipart Form Uploads**: `multipart/form-data` (Images up to 5 MB)

---

## 2. Authentication & Session Security

RestaurantHub implements **stateful session-based authentication** using Spring Security and standard HTTP servlet sessions.

```
+----------------+                      +--------------------+
| Angular Client |                      | Spring Boot Server |
+-------+--------+                      +---------+----------+
        |                                         |
        | 1. POST /api/v1/auth/login              |
        |---------------------------------------->| (Validates BCrypt hash)
        |                                         | (Creates HttpSession)
        | 2. 200 OK + Set-Cookie: JSESSIONID      |
        |<----------------------------------------| (HttpOnly, SameSite=Lax)
        |                                         |
        | 3. GET /api/v1/auth/csrf                |
        |---------------------------------------->| (Generates CsrfToken)
        | 4. 200 OK + Set-Cookie: XSRF-TOKEN      |
        |<----------------------------------------| (HttpOnly=false)
        |                                         |
        | 5. Mutating Request (POST/PUT/DELETE)   |
        |    Cookie: JSESSIONID                   |
        |    Header: X-XSRF-TOKEN                 |
        |---------------------------------------->| (Validates Session & Token)
        | 6. 200 OK / 201 Created                 |
        |<----------------------------------------|
```

### Key Security Decisions
1. **No Bearer JWTs**: Authentication uses standard HTTP session cookies (`JSESSIONID`) protected by `HttpOnly=true` to prevent XSS credential theft.
2. **Double Submit Cookie CSRF**:
   - `GET /api/v1/auth/csrf` sets the client-readable `XSRF-TOKEN` cookie and returns header details.
   - Angular HttpClient automatically attaches the `X-XSRF-TOKEN` header on all mutating requests (`POST`, `PUT`, `PATCH`, `DELETE`).
3. **Role-Based Access Control**:
   - **`PUBLIC`**: Unauthenticated access to menu, categories, branding settings, health check, registration, and login.
   - **`CUSTOMER`**: Authenticated customer account; can place orders and inspect owned order history.
   - **`ADMIN`**: Elevated administrative role; can manage menu categories, dishes, restaurant settings, order fulfillment status, and upload media.

---

## 3. Interactive OpenAPI & Swagger UI

- **Swagger UI (Development)**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI 3.0 JSON Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Production Exposure Policy**: OpenAPI and Swagger UI endpoints are **disabled by default** in production (`springdoc.api-docs.enabled=false`, `springdoc.swagger-ui.enabled=false`) to minimize attack surface. They can be enabled if explicitly required via `APP_API_DOCS_ENABLED=true`.

> **Note on Swagger UI Interactivity**: Swagger UI transmits `JSESSIONID` cookies automatically on same-origin requests, but does not natively inject custom `X-XSRF-TOKEN` headers on mutating requests without manual input.

---

## 4. Standardized Error Response Format

All API errors return a consistent RFC 7807-compatible JSON payload:

```json
{
  "timestamp": "2026-08-26T19:50:00.000",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more request fields",
  "path": "/api/v1/categories",
  "fieldErrors": {
    "name": "Category name is required",
    "displayOrder": "Display order must be positive"
  }
}
```

### Common HTTP Status Codes
| Status Code | Meaning | Typical Scenario |
| :--- | :--- | :--- |
| **`200 OK`** | Request succeeded | Successful GET, PUT, PATCH |
| **`201 Created`** | Resource created | Successful entity creation or registration |
| **`204 No Content`** | Request succeeded with no body | Successful DELETE or logout |
| **`400 Bad Request`** | Validation failure / Malformed JSON | Missing required field, store closed, invalid PIN |
| **`401 Unauthorized`** | Missing authentication | Unauthenticated access to customer or admin endpoint |
| **`403 Forbidden`** | Insufficient permissions / CSRF failure | Customer accessing admin endpoint or missing CSRF token |
| **`404 Not Found`** | Resource not found | Invalid category ID, food ID, or order ID |
| **`409 Conflict`** | Unique constraint violation | Duplicate email, phone, category slug, or active FK links |
| **`500 Internal Error`** | Server failure | Unhandled runtime exception (stack trace never leaked) |

---

## 5. Endpoint Catalog

### Health Check
- `GET /api/v1/health`: Returns service availability status (`{"status":"UP","application":"RestaurantHub API"}`). (Public)

### Authentication (`/api/v1/auth`)
- `POST /api/v1/auth/register`: Register new customer account. (Public, CSRF required)
- `POST /api/v1/auth/login`: Authenticate credentials and establish session. (Public, CSRF required)
- `GET /api/v1/auth/me`: Retrieve current logged-in customer profile. (Customer / Admin)
- `GET /api/v1/auth/csrf`: Initialize anti-CSRF token and cookie. (Public)
- `POST /api/v1/auth/logout`: Invalidate session and clear authentication cookies. (Authenticated)

### Restaurant Settings (`/api/v1/settings`)
- `GET /api/v1/settings`: Public restaurant profile, branding colors, operational hours, delivery fees, and order acceptance status. (Public)

### Categories (`/api/v1/categories`)
- `GET /api/v1/categories`: List menu categories (supports `?activeOnly=true`). (Public)
- `GET /api/v1/categories/{id}`: Get category details by ID. (Public)
- `POST /api/v1/categories`: Create new category. (Admin, CSRF required)
- `PUT /api/v1/categories/{id}`: Update category details. (Admin, CSRF required)
- `DELETE /api/v1/categories/{id}`: Delete category (blocked if dishes are linked). (Admin, CSRF required)

### Foods (`/api/v1/foods`)
- `GET /api/v1/foods`: List dishes (supports `?categoryId=...`, `?popular=true`, `?activeOnly=true`). (Public)
- `GET /api/v1/foods/{id}`: Get dish details by ID. (Public)
- `POST /api/v1/foods`: Create new dish. (Admin, CSRF required)
- `PUT /api/v1/foods/{id}`: Update dish details. (Admin, CSRF required)
- `DELETE /api/v1/foods/{id}`: Delete dish. (Admin, CSRF required)

### Customer Orders (`/api/v1/orders`)
- `POST /api/v1/orders`: Submit new food order with delivery address and line items. (Customer, CSRF required)
- `GET /api/v1/orders`: List order history for current customer. (Customer)
- `GET /api/v1/orders/{id}`: Retrieve receipt and details for specific customer-owned order. (Customer)

### Admin Operations (`/api/v1/admin`)
- `GET /api/v1/admin/dashboard/summary`: Retrieve live operational metrics (revenue, orders, menu items). (Admin)
- `GET /api/v1/admin/orders`: List all restaurant orders (supports `?status=...`). (Admin)
- `GET /api/v1/admin/orders/{id}`: Retrieve detailed order management record. (Admin)
- `PATCH /api/v1/admin/orders/{id}/status`: Advance order status (`CONFIRMED`, `PREPARING`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`). (Admin, CSRF required)
- `GET /api/v1/admin/settings`: Retrieve editable restaurant configuration. (Admin)
- `PUT /api/v1/admin/settings`: Update branding, delivery thresholds, GSTIN, FSSAI, and hours. (Admin, CSRF required)
- `GET /api/v1/admin/media/status`: Check active media storage provider and upload limits. (Admin)
- `POST /api/v1/admin/media/images`: Upload image file (`multipart/form-data`, <= 5 MB, JPEG/PNG/WEBP). (Admin, CSRF required)
