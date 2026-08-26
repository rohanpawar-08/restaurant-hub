package com.restauranthub.media.exception;

/**
 * Thrown when an image fails validation or storage upload.
 */
public class MediaUploadException extends RuntimeException {

    public MediaUploadException(String message) {
        super(message);
    }

    public MediaUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
