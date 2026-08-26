package com.restauranthub.media;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class MediaStorageProviderSelectionTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(MediaStorageConfig.class);

    @Test
    @DisplayName("Default configuration should resolve MediaStorageService to LocalMediaStorageService")
    void defaultConfigurationShouldResolveLocalProvider() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(MediaStorageService.class);
            MediaStorageService service = context.getBean(MediaStorageService.class);
            assertThat(service).isInstanceOf(LocalMediaStorageService.class);
            assertThat(service.getProviderName()).isEqualTo("LOCAL");
            assertThat(service.isConfigured()).isTrue();
        });
    }

    @Test
    @DisplayName("app.media.provider=local should resolve MediaStorageService to LocalMediaStorageService")
    void explicitLocalPropertyShouldResolveLocalProvider() {
        contextRunner
                .withPropertyValues("app.media.provider=local")
                .run(context -> {
                    assertThat(context).hasSingleBean(MediaStorageService.class);
                    MediaStorageService service = context.getBean(MediaStorageService.class);
                    assertThat(service).isInstanceOf(LocalMediaStorageService.class);
                    assertThat(service.getProviderName()).isEqualTo("LOCAL");
                });
    }

    @Test
    @DisplayName("app.media.provider=cloudinary should resolve MediaStorageService to CloudinaryMediaStorageService")
    void cloudinaryPropertyShouldResolveCloudinaryProvider() {
        contextRunner
                .withPropertyValues("app.media.provider=cloudinary")
                .run(context -> {
                    assertThat(context).hasSingleBean(MediaStorageService.class);
                    MediaStorageService service = context.getBean(MediaStorageService.class);
                    assertThat(service).isInstanceOf(CloudinaryMediaStorageService.class);
                    assertThat(service.getProviderName()).isEqualTo("CLOUDINARY");
                    // Without credentials, it should be unconfigured but should NOT fail application startup
                    assertThat(service.isConfigured()).isFalse();
                });
    }

    @Test
    @DisplayName("app.media.provider=cloudinary with credentials should report configured")
    void cloudinaryPropertyWithCredentialsShouldBeConfigured() {
        contextRunner
                .withPropertyValues(
                        "app.media.provider=cloudinary",
                        "CLOUDINARY_CLOUD_NAME=demo_cloud",
                        "CLOUDINARY_API_KEY=123456789",
                        "CLOUDINARY_API_SECRET=abcdefsecret"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(MediaStorageService.class);
                    MediaStorageService service = context.getBean(MediaStorageService.class);
                    assertThat(service).isInstanceOf(CloudinaryMediaStorageService.class);
                    assertThat(service.getProviderName()).isEqualTo("CLOUDINARY");
                    assertThat(service.isConfigured()).isTrue();
                });
    }
}
