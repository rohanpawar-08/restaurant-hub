package com.restauranthub.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Centralized Spring MVC Configuration for Cross-Origin Resource Sharing (CORS)
 * and public read-only serving of locally uploaded media assets under /media/**.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;
    private final String mediaLocalRoot;

    public WebMvcConfig(
            @Value("${app.cors.allowed-origins:http://localhost:4200}") String[] allowedOrigins,
            @Value("${app.media.local.root:${MEDIA_LOCAL_ROOT:uploads}}") String mediaLocalRoot
    ) {
        this.allowedOrigins = allowedOrigins;
        this.mediaLocalRoot = mediaLocalRoot != null ? mediaLocalRoot.trim() : "uploads";
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
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

    @org.springframework.context.annotation.Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(java.util.Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(java.util.Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
