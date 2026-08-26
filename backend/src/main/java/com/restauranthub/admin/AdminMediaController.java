package com.restauranthub.admin;

import com.restauranthub.media.MediaStorageService;
import com.restauranthub.media.dto.MediaUploadResult;
import com.restauranthub.media.exception.MediaStorageNotConfiguredException;
import com.restauranthub.media.exception.MediaUploadException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Administrative REST Controller for secure image uploads.
 * Requires ROLE_ADMIN and valid CSRF token.
 */
@RestController
@RequestMapping("/api/v1/admin/media")
public class AdminMediaController {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg",
            ".jpeg",
            ".png",
            ".webp"
    );

    private final MediaStorageService mediaStorageService;

    public AdminMediaController(MediaStorageService mediaStorageService) {
        this.mediaStorageService = mediaStorageService;
    }

    /**
     * Returns the operational status of the remote media storage provider.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getMediaStatus() {
        return ResponseEntity.ok(Map.of(
                "available", mediaStorageService.isConfigured(),
                "provider", mediaStorageService.isConfigured() ? "Cloudinary" : "None"
        ));
    }

    /**
     * Secure image upload endpoint.
     * Validates file size, MIME type, file extension, and magic bytes before uploading.
     */
    @PostMapping("/images")
    public ResponseEntity<MediaUploadResult> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "restauranthub") String folder
    ) {
        validateImageFile(file);

        if (!mediaStorageService.isConfigured()) {
            throw new MediaStorageNotConfiguredException();
        }

        MediaUploadResult result = mediaStorageService.uploadImage(file, folder);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new MediaUploadException("Please select an image file to upload.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new MediaUploadException("Image file size exceeds maximum permitted limit of 5 MB.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new MediaUploadException("Image file must have a valid filename.");
        }

        String lowerFilename = originalFilename.toLowerCase();
        boolean hasAllowedExtension = ALLOWED_EXTENSIONS.stream().anyMatch(lowerFilename::endsWith);
        if (!hasAllowedExtension) {
            throw new MediaUploadException("Unsupported file extension. Allowed formats are: JPG, PNG, WEBP.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new MediaUploadException("Unsupported image content type. Allowed types: JPEG, PNG, WEBP.");
        }

        // Magic byte verification to prevent polyglot/executable disguise
        try {
            byte[] header = file.getInputStream().readNBytes(12);
            if (!isValidImageHeader(header, contentType)) {
                throw new MediaUploadException("Corrupt or invalid image format detected.");
            }
        } catch (IOException e) {
            throw new MediaUploadException("Unable to read uploaded image stream.", e);
        }
    }

    private boolean isValidImageHeader(byte[] header, String contentType) {
        if (header == null || header.length < 4) {
            return false;
        }

        // JPEG: FF D8 FF
        if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
            return true;
        }

        // PNG: 89 50 4E 47
        if (header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
            return true;
        }

        // WEBP: RIFF....WEBP (header[0..3]="RIFF", header[8..11]="WEBP")
        if (header.length >= 12 &&
                header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F' &&
                header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return true;
        }

        return false;
    }
}
