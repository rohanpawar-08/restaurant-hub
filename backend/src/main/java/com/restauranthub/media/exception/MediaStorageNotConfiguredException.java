package com.restauranthub.media.exception;

/**
 * Thrown when an upload is requested but the media storage provider (e.g. Cloudinary) is not configured.
 */
public class MediaStorageNotConfiguredException extends RuntimeException {

    public MediaStorageNotConfiguredException() {
        super("Image upload is not configured yet. You can enter an image URL/path.");
    }

    public MediaStorageNotConfiguredException(String message) {
        super(message);
    }
}
