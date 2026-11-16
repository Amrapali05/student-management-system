package com.sms.exception;

import java.time.Instant;

/**
 * Root domain exception for all Student Management System errors.
 */
public class SmsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final Instant timestamp;
    private final String errorCode;

    public SmsException(String message) {
        this(message, "SMS_ERROR", null);
    }

    public SmsException(String message, Throwable cause) {
        this(message, "SMS_ERROR", cause);
    }

    public SmsException(String message, String errorCode) {
        this(message, errorCode, null);
    }

    public SmsException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.timestamp = Instant.now();
        this.errorCode = errorCode != null ? errorCode : "SMS_ERROR";
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
