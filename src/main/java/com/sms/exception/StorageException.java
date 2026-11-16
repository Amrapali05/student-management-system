package com.sms.exception;

/**
 * Thrown when persistence operations (file read/write, JSON serialization) fail.
 */
public class StorageException extends SmsException {
    private static final long serialVersionUID = 1L;

    public StorageException(String message) {
        super(message, "STORAGE_ERROR");
    }

    public StorageException(String message, Throwable cause) {
        super(message, "STORAGE_ERROR", cause);
    }
}
