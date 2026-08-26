package com.restauranthub.media;

import com.restauranthub.media.dto.MediaUploadResult;
import com.restauranthub.media.exception.MediaUploadException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Free local filesystem implementation of MediaStorageService.
 * Stores uploaded assets in segregated local directories and generates clean public /media/... URLs.
 * Requires zero external cloud dependencies or paid accounts.
 */
public class LocalMediaStorageService implements MediaStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalMediaStorageService.class);

    private final Path rootPath;

    public LocalMediaStorageService(String uploadRoot) {
        this.rootPath = Paths.get(uploadRoot != null ? uploadRoot.trim() : "uploads")
                .toAbsolutePath()
                .normalize();

        initDirectories();
    }

    /**
     * Initializes base upload directory and purpose subdirectories (food, logo, hero).
     */
    private void initDirectories() {
        try {
            Files.createDirectories(rootPath);
            for (MediaPurpose purpose : MediaPurpose.values()) {
                Path subDir = rootPath.resolve(purpose.getSubdirectory()).normalize();
                Files.createDirectories(subDir);
            }
            log.info("Local Media Storage initialized at root: {}", rootPath);
        } catch (IOException e) {
            log.error("Failed to initialize local media storage directories at [{}]: {}", rootPath, e.getMessage());
            throw new MediaUploadException("Failed to initialize local media storage directories.", e);
        }
    }

    @Override
    public boolean isConfigured() {
        return Files.exists(rootPath) && Files.isWritable(rootPath);
    }

    @Override
    public String getProviderName() {
        return "LOCAL";
    }

    @Override
    public MediaUploadResult uploadImage(MultipartFile file, String folderOrPurpose) {
        if (file == null || file.isEmpty()) {
            throw new MediaUploadException("Please select an image file to upload.");
        }

        MediaPurpose purpose = MediaPurpose.fromParameter(folderOrPurpose);
        Path targetDir = rootPath.resolve(purpose.getSubdirectory()).normalize();

        // Path-traversal defense
        if (!targetDir.startsWith(rootPath)) {
            throw new MediaUploadException("Invalid media target directory.");
        }

        try {
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            String extension = extractSafeExtension(file);
            String storedFilename = UUID.randomUUID().toString() + extension;
            Path destinationFile = targetDir.resolve(storedFilename).normalize();

            // Strict boundary verification
            if (!destinationFile.startsWith(targetDir) || !destinationFile.startsWith(rootPath)) {
                throw new MediaUploadException("Illegal file path detected.");
            }

            try (InputStream is = file.getInputStream()) {
                Files.copy(is, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            String publicUrl = "/media/" + purpose.getSubdirectory() + "/" + storedFilename;
            String publicId = purpose.getSubdirectory() + "/" + storedFilename;

            log.info("Local media upload succeeded: publicId={}, size={} bytes", publicId, file.getSize());
            return new MediaUploadResult(publicUrl, publicId);

        } catch (IOException e) {
            log.error("Failed to store media file on local disk: {}", e.getMessage());
            throw new MediaUploadException("Failed to save media file to local disk.", e);
        }
    }

    @Override
    public void deleteImage(String publicId) {
        if (!StringUtils.hasText(publicId)) {
            return;
        }

        try {
            Path targetFile = rootPath.resolve(publicId.trim()).normalize();
            if (targetFile.startsWith(rootPath)) {
                Files.deleteIfExists(targetFile);
                log.info("Local media deleted: {}", publicId);
            }
        } catch (IOException e) {
            log.warn("Failed to delete local media [{}]: {}", publicId, e.getMessage());
        }
    }

    public Path getRootPath() {
        return rootPath;
    }

    private String extractSafeExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.hasText(originalFilename)) {
            String lower = originalFilename.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".png")) return ".png";
            if (lower.endsWith(".webp")) return ".webp";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return ".jpg";
        }

        String contentType = file.getContentType();
        if (contentType != null) {
            String lowerType = contentType.toLowerCase(Locale.ROOT);
            if (lowerType.contains("png")) return ".png";
            if (lowerType.contains("webp")) return ".webp";
            if (lowerType.contains("jpeg") || lowerType.contains("jpg")) return ".jpg";
        }

        return ".jpg";
    }
}
