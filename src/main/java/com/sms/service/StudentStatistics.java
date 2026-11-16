package com.sms.service;

import com.sms.model.Department;
import com.sms.model.StudentStatus;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encapsulates analytical statistics computed across the student population.
 */
public class StudentStatistics {

    private final int totalStudents;
    private final int activeStudents;
    private final double averageGpa;
    private final double highestGpa;
    private final double lowestGpa;
    private final Map<Department, Integer> departmentCounts;
    private final Map<StudentStatus, Integer> statusCounts;
    private final Map<String, Integer> gpaDistribution;
    private final Map<Integer, Integer> yearCounts;
    private final String topDepartment;

    public StudentStatistics(int totalStudents, int activeStudents, double averageGpa,
                             double highestGpa, double lowestGpa,
                             Map<Department, Integer> departmentCounts,
                             Map<StudentStatus, Integer> statusCounts,
                             Map<String, Integer> gpaDistribution,
                             Map<Integer, Integer> yearCounts,
                             String topDepartment) {
        this.totalStudents = totalStudents;
        this.activeStudents = activeStudents;
        this.averageGpa = Math.round(averageGpa * 100.0) / 100.0;
        this.highestGpa = highestGpa;
        this.lowestGpa = lowestGpa;
        this.departmentCounts = departmentCounts != null ? Collections.unmodifiableMap(departmentCounts) : Collections.emptyMap();
        this.statusCounts = statusCounts != null ? Collections.unmodifiableMap(statusCounts) : Collections.emptyMap();
        this.gpaDistribution = gpaDistribution != null ? Collections.unmodifiableMap(gpaDistribution) : Collections.emptyMap();
        this.yearCounts = yearCounts != null ? Collections.unmodifiableMap(yearCounts) : Collections.emptyMap();
        this.topDepartment = topDepartment != null ? topDepartment : "None";
    }

    public int getTotalStudents() { return totalStudents; }
    public int getActiveStudents() { return activeStudents; }
    public double getAverageGpa() { return averageGpa; }
    public double getHighestGpa() { return highestGpa; }
    public double getLowestGpa() { return lowestGpa; }
    public Map<Department, Integer> getDepartmentCounts() { return departmentCounts; }
    public Map<StudentStatus, Integer> getStatusCounts() { return statusCounts; }
    public Map<String, Integer> getGpaDistribution() { return gpaDistribution; }
    public Map<Integer, Integer> getYearCounts() { return yearCounts; }
    public String getTopDepartment() { return topDepartment; }

    public double getActivePercentage() {
        if (totalStudents == 0) return 0.0;
        return Math.round(((double) activeStudents / totalStudents * 100.0) * 10.0) / 10.0;
    }
}
