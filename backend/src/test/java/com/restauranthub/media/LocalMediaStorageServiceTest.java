package com.restauranthub.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.restauranthub.media.dto.MediaUploadResult;
import com.restauranthub.media.exception.MediaUploadException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class LocalMediaStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalMediaStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new LocalMediaStorageService(tempDir.toString());
    }

    @Test
    @DisplayName("Should initialize base directories and report LOCAL provider as configured")
    void shouldInitializeDirectoriesAndReportConfigured() {
        assertThat(storageService.isConfigured()).isTrue();
        assertThat(storageService.getProviderName()).isEqualTo("LOCAL");
        assertThat(Files.exists(tempDir.resolve("food"))).isTrue();
        assertThat(Files.exists(tempDir.resolve("logo"))).isTrue();
        assertThat(Files.exists(tempDir.resolve("hero"))).isTrue();
    }

    @Test
    @DisplayName("Should store food image with UUID filename and return clean /media URL")
    void shouldStoreFoodImageAndReturnCleanMediaUrl() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "crispy-dosa.jpg",
                "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10}
        );

        MediaUploadResult result = storageService.uploadImage(file, "FOOD");

        assertThat(result).isNotNull();
        assertThat(result.url()).startsWith("/media/food/").endsWith(".jpg");
        assertThat(result.publicId()).startsWith("food/").endsWith(".jpg");
        assertThat(result.url()).doesNotContain("temp", "C:", "\\");

        // Verify physical file exists on disk
        Path storedFile = tempDir.resolve(result.publicId());
        assertThat(Files.exists(storedFile)).isTrue();
        assertThat(Files.size(storedFile)).isEqualTo(6);
    }

    @Test
    @DisplayName("Should store logo image in logo subdirectory with PNG extension")
    void shouldStoreLogoImageInLogoSubdirectory() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "brand-logo.png",
                "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}
        );

        MediaUploadResult result = storageService.uploadImage(file, "LOGO");

        assertThat(result.url()).startsWith("/media/logo/").endsWith(".png");
        assertThat(result.publicId()).startsWith("logo/").endsWith(".png");
        assertThat(Files.exists(tempDir.resolve(result.publicId()))).isTrue();
    }

    @Test
    @DisplayName("Should store hero image in hero subdirectory with WEBP extension")
    void shouldStoreHeroImageInHeroSubdirectory() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "restaurant-front.webp",
                "image/webp",
                new byte[]{'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'}
        );

        MediaUploadResult result = storageService.uploadImage(file, "HERO");

        assertThat(result.url()).startsWith("/media/hero/").endsWith(".webp");
        assertThat(result.publicId()).startsWith("hero/").endsWith(".webp");
        assertThat(Files.exists(tempDir.resolve(result.publicId()))).isTrue();
    }

    @Test
    @DisplayName("Should reject empty multipart upload")
    void shouldRejectEmptyUpload() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> storageService.uploadImage(emptyFile, "FOOD"))
                .isInstanceOf(MediaUploadException.class)
                .hasMessageContaining("Please select an image file to upload");
    }

    @Test
    @DisplayName("Should safely delete stored image by publicId")
    void shouldSafelyDeleteStoredImage() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0}
        );

        MediaUploadResult result = storageService.uploadImage(file, "FOOD");
        Path storedFile = tempDir.resolve(result.publicId());
        assertThat(Files.exists(storedFile)).isTrue();

        storageService.deleteImage(result.publicId());
        assertThat(Files.exists(storedFile)).isFalse();
    }

    @Test
    @DisplayName("Should ignore path traversal attempts in deleteImage")
    void shouldIgnorePathTraversalInDelete() {
        // Attempting to delete outside root should fail silently or do nothing without throwing
        storageService.deleteImage("../../windows/system32/cmd.exe");
        storageService.deleteImage(null);
    }
}
