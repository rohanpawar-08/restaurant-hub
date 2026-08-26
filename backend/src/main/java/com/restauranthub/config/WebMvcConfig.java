package com.restauranthub.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Centralized Spring MVC Configuration for Cross-Origin Resource Sharing (CORS)
 * and public read-only serving of locally uploaded media assets under /media/**.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final List<String> allowedOrigins;
    private final String mediaLocalRoot;

    public WebMvcConfig(
            @Value("${app.cors.allowed-origins:http://localhost:4200}") String[] allowedOrigins,
            @Value("${app.media.local.root:${MEDIA_LOCAL_ROOT:uploads}}") String mediaLocalRoot
    ) {
        this.allowedOrigins = allowedOrigins != null
                ? Arrays.stream(allowedOrigins)
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .toList()
                : List.of();
        this.mediaLocalRoot = mediaLocalRoot != null ? mediaLocalRoot.trim() : "uploads";
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (!allowedOrigins.isEmpty()) {
            registry.addMapping("/api/**")
                    .allowedOrigins(allowedOrigins.toArray(String[]::new))
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get(mediaLocalRoot).toAbsolutePath().normalize();
        String uploadPath = uploadDir.toUri().toString();
        if (!uploadPath.endsWith("/")) {
            uploadPath += "/";
        }

        registry.addResourceHandler("/media/**")
                .addResourceLocations(uploadPath)
                .setCachePeriod(3600);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        if (!allowedOrigins.isEmpty()) {
            configuration.setAllowedOrigins(allowedOrigins);
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
            configuration.setAllowedHeaders(Arrays.asList("*"));
            configuration.setAllowCredentials(true);
            configuration.setMaxAge(3600L);
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

