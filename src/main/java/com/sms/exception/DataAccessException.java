package com.sms.exception;

/**
 * Thrown when database queries or transactions fail.
 */
public class DataAccessException extends SmsException {
    private static final long serialVersionUID = 1L;

    public DataAccessException(String message) {
        super(message, "DATA_ACCESS_ERROR");
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, "DATA_ACCESS_ERROR", cause);
    }
}
