# RestaurantHub — Release & Packaging Guide

This guide details the repeatable procedures for building, testing, auditing, and packaging production-grade release artifacts for RestaurantHub.

---

## 1. Release Architecture & Artifacts

| Component | Technology | Build Command | Output Artifact Location |
| :--- | :--- | :--- | :--- |
| **Backend** | Spring Boot 4.1.1, Java 21 | `.\mvnw.cmd clean package` *(in `backend/`)* | `backend/target/backend-0.0.1-SNAPSHOT.jar` |
| **Frontend** | Angular 22, TypeScript | `npm run build` *(in project root)* | `dist/restaurant-hub/` |

---

## 2. Prerequisites

- **Java JDK**: 21 LTS
- **Node.js**: 20.x or 22.x LTS with npm
- **Database (Local Dev/Validation)**: MySQL 8.0+
- **OS Support**: Windows (PowerShell), macOS, Linux

---

## 3. Step-by-Step Build & Release Procedure

### Step 3.1 — Execute Full Test Suites

Execute all backend integration tests and frontend component tests before generating release artifacts:

```powershell
# 1. Backend Integration Tests (206 tests)
cd c:\Users\Admin\restaurant-hub\backend
.\mvnw.cmd test

# 2. Frontend Unit Tests (160 tests)
cd c:\Users\Admin\restaurant-hub
npm test -- --watch=false
```

### Step 3.2 — Build Production Frontend Bundle

Generate optimized, minified production assets with zero hardcoded hosts:

```powershell
cd c:\Users\Admin\restaurant-hub
npm run build
```

**Artifact Verified**: Output files reside in `dist/restaurant-hub/`.

### Step 3.3 — Package Standalone Executable Spring Boot JAR

Compile, test, and package the self-contained Spring Boot fat JAR:

```powershell
cd c:\Users\Admin\restaurant-hub\backend
.\mvnw.cmd clean package
```

**Artifact Verified**: The executable JAR is created at `backend\target\backend-0.0.1-SNAPSHOT.jar`.

---

## 4. Release Artifact Verification & Auditing

### 4.1 — Verify Packaged JAR Integrity & Secret Exclusion

Ensure Flyway migrations and application profiles are present while local developer secrets are strictly excluded:

```powershell
cd c:\Users\Admin\restaurant-hub\backend

# 1. Confirm Flyway migrations V1 through V7 are bundled:
jar tf target/backend-0.0.1-SNAPSHOT.jar | Select-String "BOOT-INF/classes/db/migration"

# 2. Confirm application profiles are packaged:
jar tf target/backend-0.0.1-SNAPSHOT.jar | Select-String "BOOT-INF/classes/application"

# 3. CRITICAL AUDIT: Confirm private credentials file is ABSENT:
jar tf target/backend-0.0.1-SNAPSHOT.jar | Select-String "application-local.properties"
# (Must return NO results)

# 4. CRITICAL AUDIT: Confirm runtime uploads directory is ABSENT:
jar tf target/backend-0.0.1-SNAPSHOT.jar | Select-String "uploads"
# (Must return NO results)
```

### 4.2 — Verify Frontend Bundle for Host Hardcoding

Ensure Angular bundles use relative `/api` and `/media` paths without references to `localhost`:

```powershell
cd c:\Users\Admin\restaurant-hub
Get-ChildItem -Path dist\restaurant-hub -Recurse -File | Select-String "localhost:8080"
# (Must return NO results)
```

---

## 5. Local Execution & Production Dry Run

### 5.1 — Local Development Startup from Packaged JAR

Test the packaged JAR using local development settings:

```powershell
cd c:\Users\Admin\restaurant-hub\backend
java -jar target/backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

In a separate terminal, verify the service is operational:
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/health"
# Output: {"status":"UP","application":"RestaurantHub API"}

Invoke-RestMethod -Uri "http://localhost:8080/v3/api-docs"
# Output: Returns complete OpenAPI JSON specification
```

### 5.2 — Strict Production Profile Dry Run

Verify that the `prod` profile enforces mandatory environment variables and fails fast if database credentials are missing:

```powershell
cd c:\Users\Admin\restaurant-hub\backend
java "-Dspring.profiles.active=prod" -jar target/backend-0.0.1-SNAPSHOT.jar
# Result: Application must fail immediately at startup with missing ${DB_URL}
```

---

## 6. Versioning Recommendations

| Milestone | Suggested Version | Rationale |
| :--- | :--- | :--- |
| **Current Foundation** | `0.1.0-alpha` | Core ordering, admin dashboard, Flyway V1-V7, OpenAPI documentation, profile separation. |
| **Pre-Production Milestone** | `0.9.0-rc` | Staging dry runs, reverse proxy integration, production media storage verification. |
| **Production Launch** | `1.0.0` | First verified public production release. |
