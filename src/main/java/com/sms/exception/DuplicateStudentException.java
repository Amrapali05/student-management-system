package com.sms.exception;

/**
 * Thrown when attempting to register or update a student with a conflicting unique key (e.g. ID or Email).
 */
public class DuplicateStudentException extends SmsException {
    private static final long serialVersionUID = 1L;

    private final String conflictField;
    private final String conflictValue;

    public DuplicateStudentException(String conflictField, String conflictValue, String message) {
        super(message, "DUPLICATE_STUDENT");
        this.conflictField = conflictField;
        this.conflictValue = conflictValue;
    }

    public static DuplicateStudentException forId(String id) {
        return new DuplicateStudentException("ID", id, "Student with ID '" + id + "' already exists.");
    }

    public static DuplicateStudentException forEmail(String email, String existingId) {
        return new DuplicateStudentException("Email", email, 
                "Email '" + email + "' is already assigned to student (" + existingId + ").");
    }

    public String getConflictField() {
        return conflictField;
    }

    public String getConflictValue() {
        return conflictValue;
    }
}
