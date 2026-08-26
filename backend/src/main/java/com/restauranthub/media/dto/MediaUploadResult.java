package com.restauranthub.media.dto;

/**
 * Result payload containing the hosted public URL and asset identifier.
 */
public record MediaUploadResult(
        String url,
        String publicId
) {
}
