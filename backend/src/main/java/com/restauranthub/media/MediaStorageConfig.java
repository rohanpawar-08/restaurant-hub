package com.restauranthub.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration selecting the active MediaStorageService provider via clean conditional bean creation.
 * Supports "local" (default, free, zero-config) and "cloudinary" (optional future cloud provider).
 */
@Configuration
public class MediaStorageConfig {

    private static final Logger log = LoggerFactory.getLogger(MediaStorageConfig.class);

    @Bean
    @ConditionalOnProperty(name = "app.media.provider", havingValue = "local", matchIfMissing = true)
    public MediaStorageService localMediaStorageService(
            @Value("${app.media.local.root:${MEDIA_LOCAL_ROOT:uploads}}") String uploadRoot
    ) {
        log.info("Media Storage active provider: LOCAL (free filesystem storage)");
        return new LocalMediaStorageService(uploadRoot);
    }

    @Bean
    @ConditionalOnProperty(name = "app.media.provider", havingValue = "cloudinary")
    public MediaStorageService cloudinaryMediaStorageService(
            @Value("${CLOUDINARY_CLOUD_NAME:${cloudinary.cloud-name:}}") String cloudName,
            @Value("${CLOUDINARY_API_KEY:${cloudinary.api-key:}}") String apiKey,
            @Value("${CLOUDINARY_API_SECRET:${cloudinary.api-secret:}}") String apiSecret
    ) {
        log.info("Media Storage active provider: CLOUDINARY");
        return new CloudinaryMediaStorageService(cloudName, apiKey, apiSecret);
    }
}
