package com.restauranthub.media;

import com.restauranthub.media.dto.MediaUploadResult;
import com.restauranthub.media.exception.MediaStorageNotConfiguredException;
import com.restauranthub.media.exception.MediaUploadException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Cloudinary implementation of MediaStorageService.
 * Uses direct HTTP REST API calls to Cloudinary without requiring proprietary SDK dependencies.
 *
 * Automatically detects whether CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET
 * environment variables or properties are populated.
 */
public class CloudinaryMediaStorageService implements MediaStorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryMediaStorageService.class);

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final HttpClient httpClient;

    public CloudinaryMediaStorageService(String cloudName, String apiKey, String apiSecret) {
        this.cloudName = cloudName != null ? cloudName.trim() : "";
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.apiSecret = apiSecret != null ? apiSecret.trim() : "";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        if (isConfigured()) {
            log.info("Cloudinary Media Storage is configured for cloud: {}", this.cloudName);
        } else {
            log.info("Cloudinary Media Storage is NOT configured (credentials not supplied). URL-based media input remains enabled.");
        }
    }

    @Override
    public boolean isConfigured() {
        return StringUtils.hasText(cloudName) && StringUtils.hasText(apiKey) && StringUtils.hasText(apiSecret);
    }

    @Override
    public String getProviderName() {
        return "CLOUDINARY";
    }

    @Override
    public MediaUploadResult uploadImage(MultipartFile file, String folder) {
        if (!isConfigured()) {
            throw new MediaStorageNotConfiguredException();
        }

        try {
            long timestamp = Instant.now().getEpochSecond();
            String folderName = StringUtils.hasText(folder) ? folder.trim() : "restauranthub";

            // Prepare signature: folder=...&timestamp=...<api_secret>
            String toSign = "folder=" + folderName + "&timestamp=" + timestamp + apiSecret;
            String signature = sha1Hex(toSign);

            String boundary = "----RestaurantHub" + UUID.randomUUID().toString().replace("-", "");
            byte[] body = buildMultipartBody(boundary, file, apiKey, timestamp, folderName, signature);

            String uploadUrl = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String responseBody = response.body();
                String secureUrl = extractJsonField(responseBody, "secure_url");
                String publicId = extractJsonField(responseBody, "public_id");

                if (secureUrl == null) {
                    secureUrl = extractJsonField(responseBody, "url");
                }

                if (secureUrl != null && publicId != null) {
                    return new MediaUploadResult(secureUrl, publicId);
                }
            }

            log.error("Cloudinary upload failed with status [{}]: {}", response.statusCode(), response.body());
            throw new MediaUploadException("Image upload to remote storage provider failed. Please try again or use direct URL.");
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("Image upload network error: {}", e.getMessage());
            throw new MediaUploadException("Failed to transmit image to remote storage provider: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(String publicId) {
        if (!isConfigured() || !StringUtils.hasText(publicId)) {
            return;
        }

        try {
            long timestamp = Instant.now().getEpochSecond();
            String toSign = "public_id=" + publicId + "&timestamp=" + timestamp + apiSecret;
            String signature = sha1Hex(toSign);

            String boundary = "----RestaurantHub" + UUID.randomUUID().toString().replace("-", "");
            StringBuilder body = new StringBuilder();
            appendFormField(body, boundary, "public_id", publicId);
            appendFormField(body, boundary, "api_key", apiKey);
            appendFormField(body, boundary, "timestamp", String.valueOf(timestamp));
            appendFormField(body, boundary, "signature", signature);
            body.append("--").append(boundary).append("--\r\n");

            String destroyUrl = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/destroy";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(destroyUrl))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                    .build();

            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            log.warn("Cloudinary delete call failed for [{}]: {}", publicId, e.getMessage());
        }
    }

    private byte[] buildMultipartBody(
            String boundary,
            MultipartFile file,
            String apiKey,
            long timestamp,
            String folder,
            String signature
    ) throws IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

        appendFormField(baos, boundary, "api_key", apiKey);
        appendFormField(baos, boundary, "timestamp", String.valueOf(timestamp));
        appendFormField(baos, boundary, "folder", folder);
        appendFormField(baos, boundary, "signature", signature);

        // Append file
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
        String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";

        StringBuilder fileHeader = new StringBuilder();
        fileHeader.append("--").append(boundary).append("\r\n");
        fileHeader.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(filename).append("\"\r\n");
        fileHeader.append("Content-Type: ").append(contentType).append("\r\n\r\n");

        baos.write(fileHeader.toString().getBytes(StandardCharsets.UTF_8));
        baos.write(file.getBytes());
        baos.write("\r\n".getBytes(StandardCharsets.UTF_8));

        String endBoundary = "--" + boundary + "--\r\n";
        baos.write(endBoundary.getBytes(StandardCharsets.UTF_8));

        return baos.toByteArray();
    }

    private void appendFormField(java.io.ByteArrayOutputStream baos, String boundary, String name, String value) throws IOException {
        StringBuilder field = new StringBuilder();
        field.append("--").append(boundary).append("\r\n");
        field.append("Content-Disposition: form-data; name=\"").append(name).append("\"\r\n\r\n");
        field.append(value).append("\r\n");
        baos.write(field.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void appendFormField(StringBuilder sb, String boundary, String name, String value) {
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"").append(name).append("\"\r\n\r\n");
        sb.append(value).append("\r\n");
    }

    private String sha1Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-1 algorithm not available", e);
        }
    }

    private String extractJsonField(String json, String field) {
        if (json == null || field == null) return null;
        String pattern = "\"" + field + "\":\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return null;
        int start = idx + pattern.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end).replace("\\/", "/");
    }
}
