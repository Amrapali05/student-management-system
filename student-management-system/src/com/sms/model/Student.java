package com.sms.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Core entity representing a Student in the system.
 */
public class Student implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String dateOfBirth; // YYYY-MM-DD
    private String gender;      // Male, Female, Other
    private Department department;
    private int academicYear;   // 1 to 4
    private double gpa;         // 0.00 to 4.00
    private StudentStatus status;
    private String enrollmentDate; // YYYY-MM-DD
    private String address;

    public Student() {
        this.status = StudentStatus.ACTIVE;
        this.department = Department.COMPUTER_SCIENCE;
        this.academicYear = 1;
        this.gpa = 3.0;
        this.gender = "Female";
    }

    public Student(String id, String firstName, String lastName, String email, String phone,
                   String dateOfBirth, String gender, Department department, int academicYear,
                   double gpa, StudentStatus status, String enrollmentDate, String address) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.department = department != null ? department : Department.COMPUTER_SCIENCE;
        this.academicYear = academicYear;
        this.gpa = Math.round(gpa * 100.0) / 100.0;
        this.status = status != null ? status : StudentStatus.ACTIVE;
        this.enrollmentDate = enrollmentDate;
        this.address = address;
    }

    // Convenience Getters
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "").trim();
    }

    public String getYearLabel() {
        switch (academicYear) {
            case 1: return "Year 1 (Freshman)";
            case 2: return "Year 2 (Sophomore)";
            case 3: return "Year 3 (Junior)";
            case 4: return "Year 4 (Senior)";
            default: return "Year " + academicYear;
        }
    }

    // Standard Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public int getAcademicYear() { return academicYear; }
    public void setAcademicYear(int academicYear) { this.academicYear = academicYear; }

    public double getGpa() { return gpa; }
    public void setGpa(double gpa) { this.gpa = Math.round(gpa * 100.0) / 100.0; }

    public StudentStatus getStatus() { return status; }
    public void setStatus(StudentStatus status) { this.status = status; }

    public String getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(String enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public Student clone() {
        try {
            return (Student) super.clone();
        } catch (CloneNotSupportedException e) {
            return new Student(id, firstName, lastName, email, phone, dateOfBirth, gender,
                    department, academicYear, gpa, status, enrollmentDate, address);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(id, student.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Student{" +
                "id='" + id + '\'' +
                ", name='" + getFullName() + '\'' +
                ", department=" + department +
                ", gpa=" + gpa +
                ", status=" + status +
                '}';
    }
}
