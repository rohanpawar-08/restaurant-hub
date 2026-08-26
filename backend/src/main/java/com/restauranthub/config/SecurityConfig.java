package com.restauranthub.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Centralized Spring Security Configuration with Official SPA CSRF Hardening.
 *
 * Key Architectural Decisions:
 * 1. Session-Based Authentication: Uses standard HTTP Servlet sessions stored on the server.
 *    The browser is issued an HttpOnly `JSESSIONID` session cookie.
 * 2. Hardened SPA CSRF Protection: Uses Spring Security's official Single-Page Application (SPA)
 *    CSRF architecture. Anti-CSRF tokens are issued via an `XSRF-TOKEN` cookie (HttpOnly=false)
 *    and submitted by Angular in the `X-XSRF-TOKEN` header on all mutating requests (POST/PUT/DELETE).
 * 3. JSESSIONID vs XSRF-TOKEN:
 *    - `JSESSIONID`: Identity/Session token -> HttpOnly (hidden from JavaScript)
 *    - `XSRF-TOKEN`: Anti-forgery defense -> Readable by Angular HttpClient to verify origin intent
 * 4. Custom JSON AuthenticationEntryPoint: Returns clean RFC 7807-compatible JSON error responses
 *    for unauthenticated requests instead of HTML forms.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public SecurityConfig() {
    }

    /**
     * Password encoder using the BCrypt hashing algorithm with automatic salting.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the Spring Security AuthenticationManager bean.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * SecurityContextRepository that persists security context in the HTTP session.
     */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * Configures the main HTTP Security Filter Chain with SPA CSRF protection.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        tokenRepository.setHeaderName("X-XSRF-TOKEN");

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(tokenRepository)
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                )
                .addFilterAfter(new CsrfCookieFilter(), org.springframework.security.web.csrf.CsrfFilter.class)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint())
                )
                .authorizeHttpRequests(auth -> auth
                        // Public Health Check
                        .requestMatchers(HttpMethod.GET, "/api/v1/health/**").permitAll()
                        // Public Read-Only Restaurant Settings & Branding
                        .requestMatchers(HttpMethod.GET, "/api/v1/settings/**", "/api/v1/settings").permitAll()
                        // Public Read-Only Menu & Categories
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/foods/**").permitAll()
                        // Public CSRF Token Endpoint
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/csrf").permitAll()
                        // Public Authentication Endpoints (require CSRF)
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        // Preflight OPTIONS requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Authenticated User Session Endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").authenticated()
                        // Authenticated Customer Orders Endpoints
                        .requestMatchers("/api/v1/orders/**").authenticated()
                        // Admin-Only Resource Management & Endpoints
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/categories/**", "/api/v1/foods/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**", "/api/v1/foods/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/categories/**", "/api/v1/foods/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**", "/api/v1/foods/**").hasRole("ADMIN")
                        // Any other request requires authentication
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    /**
     * Custom entry point that returns HTTP 401 Unauthorized JSON error response.
     */
    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String json = String.format(
                    "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Full authentication is required to access this resource\",\"path\":\"%s\"}",
                    java.time.LocalDateTime.now(),
                    request.getRequestURI()
            );
            response.getWriter().write(json);
        };
    }

    /**
     * Spring Security Official SPA CSRF Token Request Handler.
     * Resolves the raw X-XSRF-TOKEN header sent by Angular while maintaining BREACH protection.
     */
    static final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {
        private final CsrfTokenRequestHandler delegate = new XorCsrfTokenRequestAttributeHandler();

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
            this.delegate.handle(request, response, csrfToken);
        }

        @Override
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
            String headerValue = request.getHeader("X-XSRF-TOKEN");
            if (!StringUtils.hasText(headerValue)) {
                headerValue = request.getHeader("X-CSRF-TOKEN");
            }
            if (!StringUtils.hasText(headerValue)) {
                headerValue = request.getHeader(csrfToken.getHeaderName());
            }
            return (StringUtils.hasText(headerValue)) ? headerValue : this.delegate.resolveCsrfTokenValue(request, csrfToken);
        }
    }

    /**
     * Filter that forces the deferred CSRF token to be rendered so that the XSRF-TOKEN cookie is set.
     */
    static final class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                // Invoking getToken() forces the deferred token to populate the cookie
                csrfToken.getToken();
            }
            filterChain.doFilter(request, response);
        }
    }
}
