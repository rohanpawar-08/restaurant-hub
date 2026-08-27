# RestaurantHub

**RestaurantHub** is a full-stack, production-ready restaurant ordering and administration platform tailored for Indian restaurants. Built with modern enterprise architectural patterns, secure session-based authentication, hardened SPA CSRF protection, Flyway database migrations, and responsive Angular design.

---

## 🚀 Technology Stack

### Backend
- **Framework**: Spring Boot 4.1.1 (Spring Framework 7)
- **Language**: Java 21 LTS
- **Database**: MySQL 8.0+
- **Database Migrations**: Flyway (V1 through V7)
- **ORM / Persistence**: Spring Data JPA & Hibernate 7 (`ddl-auto=validate`)
- **Security**: Spring Security, BCrypt password hashing, `HttpSession` session cookies, Double-Submit SPA CSRF protection
- **API Documentation**: SpringDoc OpenAPI 3.0 / Swagger UI (`springdoc-openapi-starter-webmvc-ui:3.1.0`)
- **Media Storage**: Extensible storage abstraction (`LOCAL` filesystem storage, optional `CLOUDINARY` cloud provider)

### Frontend
- **Framework**: Angular 22 (Standalone components, reactive signals)
- **Build Tool**: Angular CLI & Vite
- **Testing**: Vitest (160 automated tests)
- **Routing**: Customer portal, Admin management dashboard, relative `/api` and `/media` reverse-proxy routing

---

## 📖 Documentation Index

Comprehensive engineering guides are available in the [`docs/`](docs/) directory:

- 📑 [**API Specification (`docs/API.md`)**](docs/API.md) — Human-readable endpoint reference, session & CSRF workflow, error response format, and status code matrix.
- 📦 [**Release & Packaging Guide (`docs/RELEASE.md`)**](docs/RELEASE.md) — Step-by-step instructions for creating and auditing standalone JAR and frontend distribution bundles.
- ✅ [**Release Checklist (`docs/RELEASE_CHECKLIST.md`)**](docs/RELEASE_CHECKLIST.md) — Pre-release verification gates covering test status, secrets, migrations, and dry runs.
- ⚙️ [**Production Configuration (`docs/PRODUCTION_CONFIGURATION.md`)**](docs/PRODUCTION_CONFIGURATION.md) — Detailed reference on development vs. production profiles, environment variables, and security hardening.

---

## 🛠️ Quick Start (Local Development)

### 1. Prerequisites
- **Java 21 JDK** installed and configured in `PATH`
- **Node.js 20+** and `npm`
- **MySQL 8.0+** running locally on port `3306`

### 2. Configure Local Database Credentials
Create a local credential file at `backend/application-local.properties` (gitignored):
```properties
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

### 3. Start Backend Service
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```
- API Base URL: `http://localhost:8080/api/v1`
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON Spec: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### 4. Start Frontend Application
In a separate terminal:
```powershell
npm install
npm start
```
- Customer App: `http://localhost:4200/`
- Admin Dashboard: `http://localhost:4200/admin`

---

## 🧪 Testing

```powershell
# Run backend integration tests (206 tests)
cd backend
.\mvnw.cmd test

# Run frontend test suite (160 tests)
npm test -- --watch=false
```

---

## 📦 Building Release Artifacts

```powershell
# Package standalone executable Spring Boot JAR
cd backend
.\mvnw.cmd clean package
# Output: backend/target/backend-0.0.1-SNAPSHOT.jar

# Build production Angular bundle
npm run build
# Output: dist/restaurant-hub/
```
