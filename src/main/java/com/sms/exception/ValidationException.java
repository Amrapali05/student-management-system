package com.sms.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thrown when entity data fails business or syntactic validation constraints.
 * Supports multi-field error reporting.
 */
public class ValidationException extends SmsException {
    private static final long serialVersionUID = 1L;

    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(message, "VALIDATION_FAILED");
        this.fieldErrors = Collections.emptyMap();
    }

    public ValidationException(String field, String error) {
        super("Validation failed for field '" + field + "': " + error, "VALIDATION_FAILED");
        Map<String, String> map = new LinkedHashMap<>();
        map.put(field, error);
        this.fieldErrors = Collections.unmodifiableMap(map);
    }

    public ValidationException(Map<String, String> fieldErrors) {
        super(buildMessage(fieldErrors), "VALIDATION_FAILED");
        this.fieldErrors = Collections.unmodifiableMap(new LinkedHashMap<>(fieldErrors));
    }

    private static String buildMessage(Map<String, String> errors) {
        if (errors == null || errors.isEmpty()) {
            return "Validation failed with errors.";
        }
        StringBuilder sb = new StringBuilder("Validation errors: ");
        errors.forEach((field, msg) -> sb.append("[").append(field).append(": ").append(msg).append("] "));
        return sb.toString().trim();
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public String getFirstError() {
        if (fieldErrors.isEmpty()) return getMessage();
        return fieldErrors.values().iterator().next();
    }
}
