package com.sms.model;

/**
 * Enrollment and academic standing status flags.
 */
public enum StudentStatus {
    ACTIVE("Active", "#10B981"),
    INACTIVE("Inactive", "#64748B"),
    SUSPENDED("Suspended", "#EF4444"),
    GRADUATED("Graduated", "#8B5CF6");

    private final String label;
    private final String hexColor;

    StudentStatus(String label, String hexColor) {
        this.label = label;
        this.hexColor = hexColor;
    }

    public String getLabel() {
        return label;
    }

    public String getDisplayName() {
        return label;
    }

    public String getHexColor() {
        return hexColor;
    }

    public static StudentStatus fromString(String str) {
        if (str == null) return ACTIVE;
        for (StudentStatus s : values()) {
            if (s.name().equalsIgnoreCase(str.trim()) || s.label.equalsIgnoreCase(str.trim())) {
                return s;
            }
        }
        return ACTIVE;
    }

    @Override
    public String toString() {
        return label;
    }
}
