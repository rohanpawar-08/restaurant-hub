# RestaurantHub — Release Quality Checklist

Use this checklist to perform pre-release verification before packaging and releasing any build artifact.

---

## 1. Automated Test Verification
- [x] **Backend Integration Tests**: All 206 backend tests pass (`cd backend; .\mvnw.cmd test`).
- [x] **Frontend Unit Tests**: All 160 Angular tests pass (`npm test -- --watch=false`).
- [x] **Database Schema Validation**: All entity mappings match Flyway V1–V7 migrations with `ddl-auto=validate`.

---

## 2. Release Artifact Generation & Build Health
- [x] **Frontend Production Build**: `npm run build` succeeds without compilation errors.
- [x] **Frontend Asset Audit**: `dist/restaurant-hub` contains 0 hardcoded `localhost:8080` references.
- [x] **Backend Package**: `.\mvnw.cmd clean package` generates executable `backend-0.0.1-SNAPSHOT.jar`.
- [x] **JAR Bytecode Check**: Application classes, Flyway migration files, and base profiles are present in JAR.

---

## 3. Secret Safety & Artifact Auditing
- [x] **Credential Exclusion**: `backend/application-local.properties` is NOT packaged inside the Spring Boot JAR.
- [x] **Runtime Directory Exclusion**: Runtime `uploads/` directory is NOT packaged inside the Spring Boot JAR.
- [x] **Git Ignored Verification**: `.env`, `.env.local`, and `application-local.properties` are strictly listed in `.gitignore`.
- [x] **Zero Hardcoded Secrets**: No database passwords or third-party API keys exist in git-tracked code.

---

## 4. API Documentation & OpenAPI Standards
- [x] **OpenAPI Specification**: Interactive Swagger UI is accessible at `/swagger-ui.html` during development.
- [x] **OpenAPI JSON**: Full schema served at `/v3/api-docs`.
- [x] **Accurate Security Documentation**: OpenAPI metadata accurately documents `HttpSession` + CSRF cookie security (NOT JWT).
- [x] **Production Toggle**: Swagger UI and OpenAPI docs are disabled by default in the `prod` profile (`APP_API_DOCS_ENABLED=false`).
- [x] **Human-Readable Specification**: `docs/API.md` is up to date with all endpoint routes and DTO definitions.

---

## 5. Profile & Production Readiness
- [x] **Profile Separation**: `application-dev.properties` (for local dev) and `application-prod.properties` (for deployment) are configured.
- [x] **Mandatory Production Properties**: Production profile requires `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` from environment.
- [x] **Fail-Fast Verification**: Starting `prod` profile without `DB_URL` fails fast immediately at startup.
- [x] **HTTPS Session Cookies**: `server.servlet.session.cookie.secure=true` is enabled in `prod`.
- [x] **Fail-Closed CORS**: CORS origins in production fail closed (empty default) unless explicitly supplied in `CORS_ALLOWED_ORIGINS`.
- [x] **Safe Error Responses**: Stack traces and internal server exception messages are suppressed across all profiles.

---

## 6. Local Production Dry Run
- [x] **Dev JAR Execution**: Packaged JAR boots cleanly and `GET /api/v1/health` returns `200 OK` (`{"status":"UP"}`).
- [x] **Clean Process Termination**: Test execution terminated cleanly without hanging port bindings.
