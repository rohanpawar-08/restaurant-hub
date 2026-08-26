package com.restauranthub.media;

import com.restauranthub.media.dto.MediaUploadResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * Enterprise abstraction for uploading and managing restaurant media assets.
 * Decouples controllers and services from specific storage implementations (Local filesystem vs Cloudinary).
 */
public interface MediaStorageService {

    /**
     * Uploads an image file to storage.
     *
     * @param file            validated multipart file
     * @param folderOrPurpose target purpose or directory identifier (e.g. "FOOD", "LOGO", "HERO")
     * @return MediaUploadResult containing public URL and identifier
     */
    MediaUploadResult uploadImage(MultipartFile file, String folderOrPurpose);

    /**
     * Deletes an image from storage by its public identifier.
     *
     * @param publicId asset identifier
     */
    void deleteImage(String publicId);

    /**
     * Indicates whether storage configuration and underlying medium are active and ready.
     *
     * @return true if configured and writable, false otherwise
     */
    boolean isConfigured();

    /**
     * Identifies the storage provider name (e.g. "LOCAL", "CLOUDINARY").
     *
     * @return provider name string
     */
    String getProviderName();
}
