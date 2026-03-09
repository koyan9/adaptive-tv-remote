package io.github.koyan9.tvremote.integration;

import java.util.Locale;

public enum IntegrationMode {
    MOCK,
    REAL;

    public static IntegrationMode from(String value) {
        if (value == null || value.isBlank()) {
            return MOCK;
        }
        return valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}


