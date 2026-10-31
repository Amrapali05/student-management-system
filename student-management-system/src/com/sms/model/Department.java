package com.sms.model;

/**
 * Academic departments supported in the system.
 */
public enum Department {
    COMPUTER_SCIENCE("Computer Science", "CS"),
    DATA_SCIENCE("Data Science & AI", "DS"),
    ELECTRICAL_ENG("Electrical Engineering", "EE"),
    MECHANICAL_ENG("Mechanical Engineering", "ME"),
    BUSINESS_ADMIN("Business Administration", "BA"),
    BIOMEDICAL("Biomedical Engineering", "BME"),
    CIVIL_ENG("Civil Engineering", "CE");

    private final String fullName;
    private final String code;

    Department(String fullName, String code) {
        this.fullName = fullName;
        this.code = code;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCode() {
        return code;
    }

    public static Department fromString(String text) {
        if (text == null) return COMPUTER_SCIENCE;
        for (Department d : Department.values()) {
            if (d.name().equalsIgnoreCase(text.trim()) || 
                d.fullName.equalsIgnoreCase(text.trim()) || 
                d.code.equalsIgnoreCase(text.trim())) {
                return d;
            }
        }
        return COMPUTER_SCIENCE;
    }

    @Override
    public String toString() {
        return fullName;
    }
}
