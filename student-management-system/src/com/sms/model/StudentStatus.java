package com.sms.model;

/**
 * Represents the academic and enrollment status of a student.
 */
public enum StudentStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended"),
    GRADUATED("Graduated");

    private final String displayName;

    StudentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static StudentStatus fromString(String text) {
        if (text == null) return ACTIVE;
        for (StudentStatus status : StudentStatus.values()) {
            if (status.name().equalsIgnoreCase(text.trim()) || 
                status.displayName.equalsIgnoreCase(text.trim())) {
                return status;
            }
        }
        return ACTIVE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
