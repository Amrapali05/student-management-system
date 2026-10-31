package com.sms.repository;

import com.sms.model.Department;
import com.sms.model.Student;
import com.sms.model.StudentStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Student data persistence with thread-safe file sync.
 */
public class StudentRepository {

    private final Path storagePath;
    private final List<Student> students = new ArrayList<>();

    public StudentRepository(String dataDirectory) {
        this.storagePath = Paths.get(dataDirectory, "students.json");
        load();
    }

    public synchronized void load() {
        students.clear();
        try {
            List<Student> loaded = JsonStorage.loadFromFile(storagePath);
            if (loaded.isEmpty()) {
                initSampleData();
                save();
            } else {
                students.addAll(loaded);
            }
        } catch (Exception e) {
            System.err.println("Failed to load records from " + storagePath + ": " + e.getMessage());
            initSampleData();
            try {
                save();
            } catch (Exception ignored) {}
        }
    }

    public synchronized void save() throws IOException {
        JsonStorage.saveToFile(storagePath, students);
    }

    public synchronized List<Student> getAll() {
        List<Student> copy = new ArrayList<>();
        for (Student s : students) {
            copy.add(s.clone());
        }
        return copy;
    }

    public synchronized Optional<Student> findById(String id) {
        if (id == null) return Optional.empty();
        return students.stream()
                .filter(s -> s.getId().equalsIgnoreCase(id.trim()))
                .findFirst()
                .map(Student::clone);
    }

    public synchronized boolean add(Student student) throws IOException {
        if (student == null || student.getId() == null) return false;
        if (students.stream().anyMatch(s -> s.getId().equalsIgnoreCase(student.getId()))) {
            return false; // ID already exists
        }
        students.add(student.clone());
        save();
        return true;
    }

    public synchronized boolean update(Student student) throws IOException {
        if (student == null || student.getId() == null) return false;
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getId().equalsIgnoreCase(student.getId())) {
                students.set(i, student.clone());
                save();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean delete(String id) throws IOException {
        if (id == null) return false;
        boolean removed = students.removeIf(s -> s.getId().equalsIgnoreCase(id.trim()));
        if (removed) {
            save();
        }
        return removed;
    }

    public synchronized String generateNextId() {
        int maxNum = 1000;
        for (Student s : students) {
            String id = s.getId();
            if (id != null && id.toUpperCase().startsWith("STU-")) {
                try {
                    int num = Integer.parseInt(id.substring(4).trim());
                    if (num > maxNum) {
                        maxNum = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return "STU-" + (maxNum + 1);
    }

    public synchronized void resetToSampleData() throws IOException {
        students.clear();
        initSampleData();
        save();
    }

    public synchronized int importCsv(File file) throws IOException {
        List<Student> imported = CsvExporter.importFromCsv(file);
        int added = 0;
        for (Student s : imported) {
            if (s.getId() == null || s.getId().trim().isEmpty()) {
                s.setId(generateNextId());
            }
            // Check if exists, update or add
            boolean found = false;
            for (int i = 0; i < students.size(); i++) {
                if (students.get(i).getId().equalsIgnoreCase(s.getId())) {
                    students.set(i, s.clone());
                    found = true;
                    break;
                }
            }
            if (!found) {
                students.add(s.clone());
                added++;
            }
        }
        save();
        return added;
    }

    public synchronized void exportCsv(File file) throws IOException {
        CsvExporter.exportToCsv(file, students);
    }

    private void initSampleData() {
        students.add(new Student("STU-1001", "Emma", "Watson", "emma.watson@university.edu", "+1 (555) 234-5678",
                "2003-04-15", "Female", Department.COMPUTER_SCIENCE, 3, 3.88, StudentStatus.ACTIVE, "2023-09-01", "742 Evergreen Terrace, Seattle, WA"));
        students.add(new Student("STU-1002", "Alexander", "Hayes", "alex.hayes@university.edu", "+1 (555) 345-6789",
                "2002-11-20", "Male", Department.DATA_SCIENCE, 4, 3.95, StudentStatus.ACTIVE, "2022-09-01", "10880 Wilshire Blvd, Los Angeles, CA"));
        students.add(new Student("STU-1003", "Sophia", "Chen", "sophia.chen@university.edu", "+1 (555) 456-7890",
                "2004-02-10", "Female", Department.ELECTRICAL_ENG, 2, 3.72, StudentStatus.ACTIVE, "2024-09-01", "450 Serra Mall, Stanford, CA"));
        students.add(new Student("STU-1004", "Marcus", "Johnson", "marcus.j@university.edu", "+1 (555) 567-8901",
                "2001-08-30", "Male", Department.BUSINESS_ADMIN, 4, 3.45, StudentStatus.GRADUATED, "2021-09-01", "200 Park Ave, New York, NY"));
        students.add(new Student("STU-1005", "Olivia", "Martinez", "olivia.m@university.edu", "+1 (555) 678-9012",
                "2003-07-25", "Female", Department.BIOMEDICAL, 3, 3.65, StudentStatus.ACTIVE, "2023-09-01", "300 Pasteur Dr, Palo Alto, CA"));
        students.add(new Student("STU-1006", "Lucas", "Kim", "lucas.kim@university.edu", "+1 (555) 789-0123",
                "2004-12-05", "Male", Department.COMPUTER_SCIENCE, 1, 3.20, StudentStatus.ACTIVE, "2025-01-15", "123 Tech Way, Austin, TX"));
        students.add(new Student("STU-1007", "Isabella", "Taylor", "isabella.t@university.edu", "+1 (555) 890-1234",
                "2002-03-18", "Female", Department.MECHANICAL_ENG, 4, 2.85, StudentStatus.INACTIVE, "2022-09-01", "555 Campus Drive, Boston, MA"));
        students.add(new Student("STU-1008", "David", "Miller", "david.m@university.edu", "+1 (555) 901-2345",
                "2003-09-14", "Male", Department.CIVIL_ENG, 2, 3.10, StudentStatus.ACTIVE, "2024-09-01", "88 University Ave, Denver, CO"));
        students.add(new Student("STU-1009", "Aria", "Patel", "aria.patel@university.edu", "+1 (555) 012-3456",
                "2004-05-22", "Female", Department.DATA_SCIENCE, 2, 4.00, StudentStatus.ACTIVE, "2024-09-01", "700 Forbes Ave, Pittsburgh, PA"));
        students.add(new Student("STU-1010", "Noah", "Williams", "noah.w@university.edu", "+1 (555) 123-4567",
                "2002-06-12", "Male", Department.BUSINESS_ADMIN, 3, 2.40, StudentStatus.SUSPENDED, "2023-09-01", "401 Terry Ave N, Chicago, IL"));
        students.add(new Student("STU-1011", "Chloe", "Garcia", "chloe.g@university.edu", "+1 (555) 234-8765",
                "2003-10-08", "Female", Department.COMPUTER_SCIENCE, 3, 3.78, StudentStatus.ACTIVE, "2023-09-01", "100 Bay State Rd, San Jose, CA"));
        students.add(new Student("STU-1012", "Ethan", "Wright", "ethan.w@university.edu", "+1 (555) 345-9876",
                "2001-01-19", "Male", Department.ELECTRICAL_ENG, 4, 3.52, StudentStatus.GRADUATED, "2021-09-01", "77 Massachusetts Ave, Cambridge, MA"));
    }

    public static void main(String[] args) {
        String dir = args.length > 0 ? args[0] : "data";
        StudentRepository repo = new StudentRepository(dir);
        System.out.println("Initialized StudentRepository at '" + dir + "' with " + repo.getAll().size() + " records.");
    }
}
