package com.sms.service;

import com.sms.model.Department;
import com.sms.model.Student;
import com.sms.model.StudentStatus;
import com.sms.repository.StudentRepository;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Business logic service layer for student operations, validation, and analytics.
 */
public class StudentService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    public List<Student> getAllStudents() {
        return repository.getAll();
    }

    public Optional<Student> getStudentById(String id) {
        return repository.findById(id);
    }

    public String generateNextId() {
        return repository.generateNextId();
    }

    public void addStudent(Student student) throws IllegalArgumentException, IOException {
        validateStudent(student, true);
        boolean success = repository.add(student);
        if (!success) {
            throw new IllegalArgumentException("Student with ID " + student.getId() + " already exists.");
        }
    }

    public void updateStudent(Student student) throws IllegalArgumentException, IOException {
        validateStudent(student, false);
        boolean success = repository.update(student);
        if (!success) {
            throw new IllegalArgumentException("Student with ID " + student.getId() + " does not exist.");
        }
    }

    public void deleteStudent(String id) throws IOException {
        boolean removed = repository.delete(id);
        if (!removed) {
            throw new IllegalArgumentException("Student with ID " + id + " not found.");
        }
    }

    public void resetToDefaultData() throws IOException {
        repository.resetToSampleData();
    }

    public int importStudentsFromCsv(File file) throws IOException {
        return repository.importCsv(file);
    }

    public void exportStudentsToCsv(File file, List<Student> customList) throws IOException {
        if (customList == null) {
            repository.exportCsv(file);
        } else {
            com.sms.repository.CsvExporter.exportToCsv(file, customList);
        }
    }

    /**
     * Filters, searches, and sorts the student list based on criteria.
     */
    public List<Student> filterStudents(String search, Department deptFilter, StudentStatus statusFilter,
                                       Integer yearFilter, String sortBy, boolean ascending) {
        List<Student> list = repository.getAll();

        return list.stream()
                .filter(s -> {
                    // Search text across multiple fields
                    if (search != null && !search.trim().isEmpty()) {
                        String query = search.trim().toLowerCase();
                        boolean match = s.getId().toLowerCase().contains(query) ||
                                s.getFullName().toLowerCase().contains(query) ||
                                s.getEmail().toLowerCase().contains(query) ||
                                (s.getPhone() != null && s.getPhone().toLowerCase().contains(query)) ||
                                (s.getAddress() != null && s.getAddress().toLowerCase().contains(query));
                        if (!match) return false;
                    }

                    // Department filter
                    if (deptFilter != null && s.getDepartment() != deptFilter) {
                        return false;
                    }

                    // Status filter
                    if (statusFilter != null && s.getStatus() != statusFilter) {
                        return false;
                    }

                    // Year filter (0 means all)
                    if (yearFilter != null && yearFilter > 0 && s.getAcademicYear() != yearFilter) {
                        return false;
                    }

                    return true;
                })
                .sorted((s1, s2) -> {
                    int result = 0;
                    String sortKey = sortBy != null ? sortBy.toLowerCase() : "id";
                    switch (sortKey) {
                        case "name":
                            result = s1.getFullName().compareToIgnoreCase(s2.getFullName());
                            break;
                        case "gpa":
                            result = Double.compare(s1.getGpa(), s2.getGpa());
                            break;
                        case "department":
                            result = s1.getDepartment().getFullName().compareToIgnoreCase(s2.getDepartment().getFullName());
                            break;
                        case "year":
                            result = Integer.compare(s1.getAcademicYear(), s2.getAcademicYear());
                            break;
                        case "status":
                            result = s1.getStatus().name().compareToIgnoreCase(s2.getStatus().name());
                            break;
                        case "enrollment":
                            result = Objects.toString(s1.getEnrollmentDate(), "").compareToIgnoreCase(Objects.toString(s2.getEnrollmentDate(), ""));
                            break;
                        case "id":
                        default:
                            result = s1.getId().compareToIgnoreCase(s2.getId());
                            break;
                    }
                    return ascending ? result : -result;
                })
                .collect(Collectors.toList());
    }

    /**
     * Computes full analytical statistics.
     */
    public StudentStatistics calculateStatistics() {
        List<Student> list = repository.getAll();
        int total = list.size();
        if (total == 0) {
            return new StudentStatistics(0, 0, 0.0, 0.0, 0.0,
                    new LinkedHashMap<>(), new LinkedHashMap<>(),
                    new LinkedHashMap<>(), new LinkedHashMap<>(), "None");
        }

        int active = 0;
        double sumGpa = 0.0;
        double highestGpa = 0.0;
        double lowestGpa = 4.0;

        Map<Department, Integer> deptCounts = new LinkedHashMap<>();
        for (Department d : Department.values()) {
            deptCounts.put(d, 0);
        }

        Map<StudentStatus, Integer> statusCounts = new LinkedHashMap<>();
        for (StudentStatus st : StudentStatus.values()) {
            statusCounts.put(st, 0);
        }

        Map<String, Integer> gpaDist = new LinkedHashMap<>();
        gpaDist.put("3.5 - 4.0 (Honor)", 0);
        gpaDist.put("3.0 - 3.4 (Good)", 0);
        gpaDist.put("2.0 - 2.9 (Satisfactory)", 0);
        gpaDist.put("< 2.0 (Probation)", 0);

        Map<Integer, Integer> yearCounts = new LinkedHashMap<>();
        for (int y = 1; y <= 4; y++) {
            yearCounts.put(y, 0);
        }

        for (Student s : list) {
            if (s.getStatus() == StudentStatus.ACTIVE) {
                active++;
            }

            double gpa = s.getGpa();
            sumGpa += gpa;
            if (gpa > highestGpa) highestGpa = gpa;
            if (gpa < lowestGpa) lowestGpa = gpa;

            // Department count
            Department d = s.getDepartment();
            if (d != null) {
                deptCounts.put(d, deptCounts.getOrDefault(d, 0) + 1);
            }

            // Status count
            StudentStatus st = s.getStatus();
            if (st != null) {
                statusCounts.put(st, statusCounts.getOrDefault(st, 0) + 1);
            }

            // Year count
            int yr = s.getAcademicYear();
            yearCounts.put(yr, yearCounts.getOrDefault(yr, 0) + 1);

            // GPA Distribution
            if (gpa >= 3.5) {
                gpaDist.put("3.5 - 4.0 (Honor)", gpaDist.get("3.5 - 4.0 (Honor)") + 1);
            } else if (gpa >= 3.0) {
                gpaDist.put("3.0 - 3.4 (Good)", gpaDist.get("3.0 - 3.4 (Good)") + 1);
            } else if (gpa >= 2.0) {
                gpaDist.put("2.0 - 2.9 (Satisfactory)", gpaDist.get("2.0 - 2.9 (Satisfactory)") + 1);
            } else {
                gpaDist.put("< 2.0 (Probation)", gpaDist.get("< 2.0 (Probation)") + 1);
            }
        }

        double avgGpa = sumGpa / total;

        // Find top department
        String topDept = "None";
        int maxDeptCount = -1;
        for (Map.Entry<Department, Integer> entry : deptCounts.entrySet()) {
            if (entry.getValue() > maxDeptCount) {
                maxDeptCount = entry.getValue();
                topDept = entry.getKey().getFullName();
            }
        }

        return new StudentStatistics(total, active, avgGpa, highestGpa, lowestGpa,
                deptCounts, statusCounts, gpaDist, yearCounts, topDept);
    }

    private void validateStudent(Student student, boolean isNew) {
        if (student == null) {
            throw new IllegalArgumentException("Student record cannot be null.");
        }
        if (student.getId() == null || student.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID is required.");
        }
        if (student.getFirstName() == null || student.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (student.getLastName() == null || student.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required.");
        }
        if (student.getEmail() == null || !EMAIL_PATTERN.matcher(student.getEmail().trim()).matches()) {
            throw new IllegalArgumentException("A valid email address is required (e.g. name@domain.com).");
        }
        if (student.getGpa() < 0.0 || student.getGpa() > 4.0) {
            throw new IllegalArgumentException("GPA must be between 0.00 and 4.00.");
        }
        if (student.getAcademicYear() < 1 || student.getAcademicYear() > 6) {
            throw new IllegalArgumentException("Academic year must be between 1 and 6.");
        }

        // Email uniqueness check
        List<Student> all = repository.getAll();
        for (Student s : all) {
            if (s.getEmail().equalsIgnoreCase(student.getEmail().trim())) {
                if (isNew || !s.getId().equalsIgnoreCase(student.getId())) {
                    throw new IllegalArgumentException("Email '" + student.getEmail() + "' is already assigned to another student (" + s.getId() + ").");
                }
            }
        }
    }
}
