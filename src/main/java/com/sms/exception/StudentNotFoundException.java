package com.sms.exception;

/**
 * Thrown when a student record cannot be found by ID or other criteria.
 */
public class StudentNotFoundException extends SmsException {
    private static final long serialVersionUID = 1L;

    private final String studentId;

    public StudentNotFoundException(String studentId) {
        super("Student with ID '" + studentId + "' was not found.", "STUDENT_NOT_FOUND");
        this.studentId = studentId;
    }

    public String getStudentId() {
        return studentId;
    }
}
