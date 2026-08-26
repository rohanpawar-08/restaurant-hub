package com.restauranthub.media;

import java.util.Locale;
import org.springframework.util.StringUtils;

/**
 * Controlled enumeration of supported media upload purposes and target subdirectories.
 * Prevents arbitrary user-supplied directory path injections.
 */
public enum MediaPurpose {
    FOOD("food"),
    LOGO("logo"),
    HERO("hero");

    private final String subdirectory;

    MediaPurpose(String subdirectory) {
        this.subdirectory = subdirectory;
    }

    public String getSubdirectory() {
        return subdirectory;
    }

    /**
     * Safely resolves a MediaPurpose from an incoming purpose or folder request parameter.
     * Defaults to FOOD if absent or unrecognized.
     */
    public static MediaPurpose fromParameter(String param) {
        if (!StringUtils.hasText(param)) {
            return FOOD;
        }

        String normalized = param.trim().toLowerCase(Locale.ROOT);

        // Allow matching either enum name or folder name or legacy prefixes
        if (normalized.contains("logo")) {
            return LOGO;
        }
        if (normalized.contains("hero")) {
            return HERO;
        }
        if (normalized.contains("food") || normalized.contains("menu")) {
            return FOOD;
        }

        for (MediaPurpose purpose : values()) {
            if (purpose.name().equalsIgnoreCase(normalized) || purpose.getSubdirectory().equalsIgnoreCase(normalized)) {
                return purpose;
            }
        }

        return FOOD;
    }
}
