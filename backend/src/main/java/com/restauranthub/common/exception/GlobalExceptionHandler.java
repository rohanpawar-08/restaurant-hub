package com.restauranthub.common.exception;

import com.restauranthub.category.exception.CategoryNotFoundException;
import com.restauranthub.category.exception.DuplicateCategorySlugException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception handling interceptor for all REST Controllers.
 * Catches domain and framework exceptions and maps them into consistent HTTP error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles CategoryNotFoundException when a requested category does not exist.
     * Maps to HTTP 404 Not Found.
     */
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFoundException(
            CategoryNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Category not found at [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles FoodNotFoundException when a requested food item does not exist.
     * Maps to HTTP 404 Not Found.
     */
    @ExceptionHandler(com.restauranthub.food.exception.FoodNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFoodNotFoundException(
            com.restauranthub.food.exception.FoodNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Food item not found at [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles OrderNotFoundException when a requested order does not exist or is not owned by the user.
     * Maps to HTTP 404 Not Found.
     */
    @ExceptionHandler(com.restauranthub.order.exception.OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFoundException(
            com.restauranthub.order.exception.OrderNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Order not found at [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles FoodUnavailableException when an item in the order is marked unavailable.
     * Maps to HTTP 400 Bad Request.
     */
    @ExceptionHandler(com.restauranthub.order.exception.FoodUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleFoodUnavailableException(
            com.restauranthub.order.exception.FoodUnavailableException ex,
            HttpServletRequest request
    ) {
        log.warn("Food unavailable at [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles UnsupportedPaymentMethodException when an order is submitted with an unsupported payment method.
     * Maps to HTTP 400 Bad Request.
     */
    @ExceptionHandler(com.restauranthub.order.exception.UnsupportedPaymentMethodException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedPaymentMethodException(
            com.restauranthub.order.exception.UnsupportedPaymentMethodException ex,
            HttpServletRequest request
    ) {
        log.warn("Unsupported payment method at [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles OrdersClosedException when an order is attempted while ordering is disabled.
     * Maps to HTTP 400 Bad Request.
     */
    @ExceptionHandler(com.restauranthub.order.exception.OrdersClosedException.class)
    public ResponseEntity<ErrorResponse> handleOrdersClosedException(
            com.restauranthub.order.exception.OrdersClosedException ex,
            HttpServletRequest request
    ) {
        log.warn("Order attempt blocked because restaurant is not accepting orders at [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles MediaStorageNotConfiguredException when upload provider is not configured.
     * Maps to HTTP 503 Service Unavailable.
     */
    @ExceptionHandler(com.restauranthub.media.exception.MediaStorageNotConfiguredException.class)
    public ResponseEntity<ErrorResponse> handleMediaStorageNotConfiguredException(
            com.restauranthub.media.exception.MediaStorageNotConfiguredException ex,
            HttpServletRequest request
    ) {
        log.warn("Media storage not configured at [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    /**
     * Handles MediaUploadException for invalid or failed uploads.
     * Maps to HTTP 400 Bad Request.
     */
    @ExceptionHandler(com.restauranthub.media.exception.MediaUploadException.class)
    public ResponseEntity<ErrorResponse> handleMediaUploadException(
            com.restauranthub.media.exception.MediaUploadException ex,
            HttpServletRequest request
    ) {
        log.warn("Media upload failure at [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles InactiveCategoryException when attempting to assign food to an inactive category.
     * Maps to HTTP 400 Bad Request.
     */
    @ExceptionHandler(com.restauranthub.food.exception.InactiveCategoryException.class)
    public ResponseEntity<ErrorResponse> handleInactiveCategoryException(
            com.restauranthub.food.exception.InactiveCategoryException ex,
            HttpServletRequest request
    ) {
        log.warn("Inactive category error at [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles DuplicateCategorySlugException when a unique slug constraint is violated.
     * Maps to HTTP 409 Conflict.
     */
    @ExceptionHandler(DuplicateCategorySlugException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCategorySlugException(
            DuplicateCategorySlugException ex,
            HttpServletRequest request
    ) {
        log.warn("Unique constraint conflict at [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handles InvalidOrderStatusTransitionException when an invalid order status change is attempted.
     * Maps to HTTP 409 Conflict.
     */
    @ExceptionHandler(com.restauranthub.order.exception.InvalidOrderStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderStatusTransitionException(
            com.restauranthub.order.exception.InvalidOrderStatusTransitionException ex,
            HttpServletRequest request
    ) {
        log.warn("Invalid order status transition conflict at [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handles CategoryInUseException when attempting to delete a category that still contains food items.
     * Maps to HTTP 409 Conflict.
     */
    @ExceptionHandler(com.restauranthub.category.exception.CategoryInUseException.class)
    public ResponseEntity<ErrorResponse> handleCategoryInUseException(
            com.restauranthub.category.exception.CategoryInUseException ex,
            HttpServletRequest request
    ) {
        log.warn("Category in use deletion conflict at [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handles DuplicateEmailException when user registration email is already taken.
     * Maps to HTTP 409 Conflict.
     */
    @ExceptionHandler(com.restauranthub.auth.exception.DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmailException(
            com.restauranthub.auth.exception.DuplicateEmailException ex,
            HttpServletRequest request
    ) {
        log.warn("Duplicate email registration attempt at [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handles DuplicatePhoneException when user registration phone is already taken.
     * Maps to HTTP 409 Conflict.
     */
    @ExceptionHandler(com.restauranthub.auth.exception.DuplicatePhoneException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatePhoneException(
            com.restauranthub.auth.exception.DuplicatePhoneException ex,
            HttpServletRequest request
    ) {
        log.warn("Duplicate phone registration attempt at [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handles BadCredentialsException when login authentication fails.
     * Maps to HTTP 401 Unauthorized with a generic message to prevent account enumeration.
     */
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            org.springframework.security.authentication.BadCredentialsException ex,
            HttpServletRequest request
    ) {
        log.warn("Authentication failed at [{}]: Invalid credentials", request.getRequestURI());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Invalid email or password. Please try again.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handles DisabledException when user account is disabled.
     * Maps to HTTP 401 Unauthorized.
     */
    @ExceptionHandler(org.springframework.security.authentication.DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(
            org.springframework.security.authentication.DisabledException ex,
            HttpServletRequest request
    ) {
        log.warn("Authentication failed at [{}]: Account is disabled", request.getRequestURI());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Account is disabled. Please contact support.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handles AccessDeniedException when an authenticated user lacks required privileges.
     * Maps to HTTP 403 Forbidden.
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn("Access denied at [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "Access denied: You do not have permission to access this resource.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handles Jakarta Validation errors triggered by @Valid annotations on request bodies.
     * Maps to HTTP 400 Bad Request with a detailed map of invalid field names and failure messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("Validation failed for request to [{}]: {}", request.getRequestURI(), fieldErrors);
        ErrorResponse errorResponse = ErrorResponse.ofValidation(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed for one or more request fields",
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles malformed JSON payloads or unreadable HTTP requests.
     * Maps to HTTP 400 Bad Request.
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            org.springframework.http.converter.HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.warn("Malformed JSON payload for request to [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Malformed JSON request body",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles illegal arguments or malformed request payloads.
     * Maps to HTTP 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        log.warn("Illegal argument for request to [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles NoResourceFoundException when a static resource (e.g. /media/**) is not found.
     * Maps to HTTP 404 Not Found.
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
            org.springframework.web.servlet.resource.NoResourceFoundException ex,
            HttpServletRequest request
    ) {
        log.debug("Resource not found at [{}]: {}", request.getRequestURI(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "Resource not found: " + ex.getResourcePath(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Catch-all fallback for unexpected server errors.
     * Maps to HTTP 500 Internal Server Error without leaking sensitive stack traces to clients.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled internal server error at [{}]", request.getRequestURI(), ex);
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected server error occurred. Please try again later.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
