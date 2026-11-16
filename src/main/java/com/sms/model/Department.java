package com.sms.model;

/**
 * Academic departments with full labels and color representations.
 */
public enum Department {
    COMPUTER_SCIENCE("Computer Science", "CS", "#3B82F6"),
    DATA_SCIENCE("Data Science", "DS", "#8B5CF6"),
    ELECTRICAL_ENG("Electrical Engineering", "EE", "#EC4899"),
    MECHANICAL_ENG("Mechanical Engineering", "ME", "#F97316"),
    BUSINESS_ADMIN("Business Administration", "BA", "#10B981"),
    BIOMEDICAL("Biomedical Sciences", "BMS", "#06B6D4"),
    CIVIL_ENG("Civil Engineering", "CE", "#64748B");

    private final String fullName;
    private final String code;
    private final String hexColor;

    Department(String fullName, String code, String hexColor) {
        this.fullName = fullName;
        this.code = code;
        this.hexColor = hexColor;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCode() {
        return code;
    }

    public String getHexColor() {
        return hexColor;
    }

    @Override
    public String toString() {
        return fullName;
    }
}
