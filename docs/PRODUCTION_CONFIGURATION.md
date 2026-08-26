# RestaurantHub — Production & Development Configuration Architecture

This document establishes the configuration foundation, secret management safety, and environment separation guidelines for **RestaurantHub**.

---

## 1. Architectural Overview & Responsibilities

RestaurantHub follows the 12-factor configuration methodology by separating code from environment-specific configuration while maintaining a clean, zero-friction local development workflow.

```
                      +---------------------------------------+
                      |         application.properties        |
                      |  (Common safe baseline & dev defaults)|
                      +-------------------+-------------------+
                                          |
                +-------------------------+-------------------------+
                |                                                   |
                v                                                   v
+-------------------------------+               +-----------------------------------+
|   application-dev.properties  |               |    application-prod.properties    |
|   - HTTP Cookies (Secure=false)|               |   - Strict HTTPS (Secure=true)    |
|   - CORS: localhost:4200      |               |   - Strict DB Env Vars (Required) |
|   - Show SQL: true            |               |   - Fail-Closed CORS              |
|   - Debug Logging Enabled     |               |   - Forwarded Headers (framework) |
+-------------------------------+               |   - SQL Logging Disabled          |
                |                               |   - Quiet Operational Logging     |
                v                               +-----------------------------------+
+-------------------------------+
| backend/application-local.    |
| properties (External root,    |
|  gitignored, NEVER packaged)  |
+-------------------------------+
```

---

## 2. Environment Comparison Matrix

| Feature / Setting | Development (`dev` / default) | Production (`prod`) | Configuration Key / Mechanism |
| :--- | :--- | :--- | :--- |
| **Server Port** | `8080` (or `PORT`) | `${PORT:8080}` | `server.port` |
| **Active Profile** | `dev` / default | `prod` | `SPRING_PROFILES_ACTIVE=prod` |
| **Database URL** | `jdbc:mysql://localhost:3306/...` | `${DB_URL}` (strict, no default) | `spring.datasource.url` |
| **Database Credentials** | `root` / local config override | `${DB_USERNAME}`, `${DB_PASSWORD}` | `spring.datasource.username/password` |
| **Local Secrets Override**| `backend/application-local.properties` | Ignored / Absent | `spring.config.import=optional:file:./application-local.properties` |
| **JPA DDL Auto** | `validate` | `validate` | `spring.jpa.hibernate.ddl-auto` |
| **Flyway Migrations** | `enabled=true` (V1–V7) | `enabled=true` (V1–V7) | `spring.flyway.enabled` |
| **Baseline on Migrate** | Disabled | Disabled | N/A |
| **SQL Logging** | Enabled (`show-sql=true`) | Disabled (`show-sql=false`) | `spring.jpa.show-sql` |
| **Session Cookie Secure** | `false` (works over HTTP) | `true` (HTTPS only) | `server.servlet.session.cookie.secure` |
| **Session Cookie HttpOnly**| `true` | `true` | `server.servlet.session.cookie.http-only`|
| **Session Cookie SameSite**| `Lax` | `Lax` | `server.servlet.session.cookie.same-site`|
| **Session Timeout** | `30m` | `30m` (configurable via env) | `server.servlet.session.timeout` |
| **Forwarded Headers** | Not configured | `framework` (`ForwardedHeaderFilter`) | `server.forward-headers-strategy` |
| **CORS Origins** | `http://localhost:4200` | `${CORS_ALLOWED_ORIGINS:}` (fail-closed) | `app.cors.allowed-origins` |
| **Media Storage Provider** | `local` | `local` or `cloudinary` | `app.media.provider` |
| **Media Local Root** | `uploads` | `${MEDIA_LOCAL_ROOT:uploads}` | `app.media.local.root` |
| **Multipart Upload Limit**| `6MB` file / `10MB` request | `6MB` file / `10MB` request | `spring.servlet.multipart.max-file-size` |
| **Error Response Body** | Safe JSON (no stack traces) | Safe JSON (no stack traces) | `server.error.include-stacktrace=never` |
| **Logging Level** | `DEBUG` (`com.restauranthub`) | `INFO` (`com.restauranthub`), `WARN` (`root`) | `logging.level.*` |

---

## 3. Secret Packaging Safety: External Developer Configuration

To eliminate the critical risk of packaging developer credentials into production JAR archives:
- The private developer configuration file **MUST** reside outside `src/main/resources` in the `backend/` working directory root:
  `C:\Users\Admin\restaurant-hub\backend\application-local.properties`
- `application.properties` loads this via:
  ```properties
  spring.config.import=optional:file:./application-local.properties
  ```
- The classpath import (`optional:classpath:application-local.properties`) is permanently removed.
- Git ignores `application-local.properties` in `.gitignore` and `backend/.gitignore`.
- Maven builds (`mvnw package`) will never copy `application-local.properties` into `target/classes` or the output JAR.

---

## 4. Session Cookie Security & SameSite Deep Dive

Spring Boot manages server-side `HttpSession` identifiers using the `JSESSIONID` cookie.

### Key Cookie Attributes
1. **`HttpOnly=true`**:
   - Blocks client-side JavaScript (`document.cookie`) from accessing the session token.
   - Prevents session theft via Cross-Site Scripting (XSS) attacks.
2. **`Secure=true` (Production)**:
   - Instructs the browser to only transmit the cookie over encrypted HTTPS TLS connections.
   - Prevents eavesdropping and Man-In-The-Middle (MitM) token sniffing over unencrypted channels.
   - In **development**, `Secure=false` is used so developers can authenticate over `http://localhost`.
3. **`SameSite=Lax`**:
   - Ensures the session cookie is withheld on cross-site subresource requests (e.g. cross-origin `<img>`, `<iframe>`, cross-site AJAX `POST`), while allowing top-level link navigations from external sites.
   - Complements Spring Security's Double Submit Cookie CSRF architecture (`XSRF-TOKEN` + `X-XSRF-TOKEN` header).
4. **`Timeout=30m`**:
   - Inactive user sessions automatically expire on the server after 30 minutes.

---

## 5. Reverse Proxy & Forwarded Headers Trust Boundary

In production environments, Spring Boot is deployed behind a reverse proxy or load balancer (e.g. Nginx, Cloudflare, AWS ALB) that terminates SSL/TLS.

### Configuration
```properties
server.forward-headers-strategy=framework
```

### Mechanism & Trust Boundary Requirements
- The reverse proxy forwards client metadata using standard headers (`X-Forwarded-Proto: https`, `X-Forwarded-For`, `X-Forwarded-Host`, `X-Forwarded-Port`).
- Spring Boot's `ForwardedHeaderFilter` updates the `HttpServletRequest` (`request.isSecure()`, remote address, scheme).
- **Critical Security Requirement**:
  - `server.forward-headers-strategy=framework` assumes deployment behind a **trusted reverse proxy**.
  - Production Spring Boot instances must **never** be directly exposed to the public internet without the reverse proxy in front.
  - The upstream reverse proxy must be configured to strip/sanitize any incoming client-supplied `X-Forwarded-*` headers before setting verified proxy headers to prevent header spoofing attacks.

---

## 6. CORS Strategy vs Same-Origin Architecture

### Recommended Architecture (Same-Origin via Reverse Proxy)
In production, the strongly recommended setup routes both the Angular frontend and the Spring Boot API through a single domain / reverse proxy:
- `https://restaurant.example.com/` → Angular Static Build
- `https://restaurant.example.com/api/` → Spring Boot API
- `https://restaurant.example.com/media/` → Uploaded Media Assets

In this same-origin setup:
- **No CORS configuration is required.**
- `CORS_ALLOWED_ORIGINS` is left empty.
- `WebMvcConfig.java` enforces a **fail-closed** policy (rejects cross-origin requests).

### CORS vs Cross-Site Cookie Limitations
- **CORS** allows cross-origin JavaScript requests (`fetch`/`XMLHttpRequest`) to read responses from another origin.
- **However, CORS does NOT resolve cross-site cookie blocking** enforced by modern browser privacy features (e.g. Safari ITP, Chrome third-party cookie restrictions).
- For genuinely cross-site deployments (e.g. `app.domain-a.com` calling `api.domain-b.com`), browsers treat `JSESSIONID` as a third-party cookie and block it unless `SameSite=None; Secure=true` is used alongside specific client cookie policies.
- Because **same-origin** is our recommended production architecture, `SameSite=Lax` is retained as the standard, ensuring robust security without cross-site friction.

---

## 7. Database & Flyway Schema Policy

1. **Schema Authority**:
   - Flyway is the **sole authority** managing the MySQL database schema (`spring.flyway.enabled=true`, migrations located in `classpath:db/migration`).
   - Baseline-on-migrate is intentionally disabled.
2. **JPA Validation**:
   - `spring.jpa.hibernate.ddl-auto=validate` is strictly enforced in all environments.
   - Hibernate validates entity mappings at startup against the Flyway schema and **never** alters tables at runtime (`update`, `create`, `create-drop` are prohibited).
3. **Secret Safety**:
   - Production profile (`prod`) does **not** provide fallback database credentials. If `DB_URL`, `DB_USERNAME`, or `DB_PASSWORD` are missing, the application fails fast at startup.

---

## 8. Media Storage & Persistence Guidelines

RestaurantHub supports two media providers configured via `MEDIA_PROVIDER`:

### Local Storage (`MEDIA_PROVIDER=local`)
- Default provider for single-server persistent deployments and development.
- In production, set `MEDIA_LOCAL_ROOT` to a persistent directory outside the application build tree (e.g., `/var/data/restaurant-hub/uploads`).
- **Critical Operational Note**:
  - The local media directory requires **persistent disk mounts** and an independent **backup policy** (e.g., daily volume snapshots or cron rsync).
  - Never store uploads inside `src/main/resources`, `target/`, or Angular build directories, as container redeployments will wipe them.

### Cloud Storage (`MEDIA_PROVIDER=cloudinary`)
- For multi-instance or serverless deployments requiring distributed CDN storage.
- Requires `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, and `CLOUDINARY_API_SECRET`.

---

## 9. Multipart Limits & Error Protection

1. **Upload Limits**:
   - `spring.servlet.multipart.max-file-size=6MB`
   - `spring.servlet.multipart.max-request-size=10MB`
   - Aligns with the 5 MB application-level image validation in `AdminMediaController` with safe overhead for HTTP multipart framing.
2. **Error Protection**:
   - `server.error.include-stacktrace=never`
   - `server.error.include-message=never`
   - `GlobalExceptionHandler.java` converts unhandled exceptions to generic HTTP 500 JSON responses without leaking internal class names, SQL queries, or filesystem paths.

---

## 10. Logging Policy

- **Development**: `com.restauranthub` logs at `DEBUG` level for rapid local iteration.
- **Production**:
  - Root logger set to `WARN`.
  - Application logger set to `INFO`.
  - Hibernate SQL query and parameter logging disabled (`spring.jpa.show-sql=false`).
  - Sensitive tokens (`JSESSIONID`, `XSRF-TOKEN`, `DB_PASSWORD`, API secrets) are never logged.

---

## 11. Frontend URL Architecture & Proxy

1. **Relative Path Routing**:
   - Angular services make all requests using relative paths: `/api/v1/...` and `/media/...`.
   - The production Angular bundle contains **no hardcoded backend URLs** (`http://localhost:8080`).
2. **Development Proxy (`proxy.conf.json`)**:
   - Used exclusively during `npm start` / `ng serve` to forward `/api` and `/media` to `http://localhost:8080`.
   - Does **not** affect or bundle into the production build.

---

## 12. How to Run

### Development Mode (Localhost)
1. **Database**: Ensure local MySQL is running on `localhost:3306`.
2. **Local Credentials**: Duplicate `application-local.example.properties` as `backend/application-local.properties` (gitignored) and enter your local MySQL credentials.
3. **Run Backend (with dev profile)**:
   ```powershell
   cd backend
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
   ```
4. **Run Frontend**:
   ```bash
   npm start
   ```

### Production Mode
1. Provide required environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `PORT`, etc., see `.env.example`).
2. Launch Spring Boot with the `prod` profile:
   ```bash
   java -Dspring.profiles.active=prod -jar backend.jar
   ```
