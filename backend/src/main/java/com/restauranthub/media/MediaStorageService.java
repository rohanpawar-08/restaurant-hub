package com.restauranthub.media;

import com.restauranthub.media.dto.MediaUploadResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * Enterprise abstraction for uploading and managing restaurant media assets.
 * Decouples controllers and services from specific cloud storage vendors (e.g. Cloudinary, S3).
 */
public interface MediaStorageService {

    /**
     * Uploads an image file to remote storage.
     *
     * @param file   validated multipart file
     * @param folder destination folder/prefix (e.g. "restauranthub/branding", "restauranthub/food")
     * @return MediaUploadResult containing hosted URL and identifier
     */
    MediaUploadResult uploadImage(MultipartFile file, String folder);

    /**
     * Deletes an image from storage by its public identifier.
     *
     * @param publicId asset identifier
     */
    void deleteImage(String publicId);

    /**
     * Indicates whether cloud storage credentials and configuration are active and available.
     *
     * @return true if configured, false otherwise
     */
    boolean isConfigured();
}
