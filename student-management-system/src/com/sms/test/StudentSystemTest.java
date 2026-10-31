package com.sms.test;

import com.sms.model.Department;
import com.sms.model.Student;
import com.sms.model.StudentStatus;
import com.sms.repository.CsvExporter;
import com.sms.repository.JsonStorage;
import com.sms.repository.StudentRepository;
import com.sms.service.StudentService;
import com.sms.service.StudentStatistics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Automated verification test suite for the Student Management System.
 */
public class StudentSystemTest {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("🚀 RUNNING STUDENT MANAGEMENT SYSTEM VERIFICATION");
        System.out.println("==================================================");

        int passed = 0;
        int failed = 0;

        try {
            // Setup isolated test directory
            Path tempDir = Files.createTempDirectory("sms_test_");
            System.out.println("[INFO] Test workspace: " + tempDir.toAbsolutePath());

            // Test 1: Repository initialization and sample data
            StudentRepository repo = new StudentRepository(tempDir.toString());
            List<Student> initialStudents = repo.getAll();
            if (initialStudents.size() == 12) {
                System.out.println("✔ Test 1 Passed: Repository initialized with 12 sample students.");
                passed++;
            } else {
                System.err.println("✘ Test 1 Failed: Expected 12 students, got " + initialStudents.size());
                failed++;
            }

            // Test 2: JSON serialization & deserialization round-trip
            Path jsonFile = tempDir.resolve("students.json");
            List<Student> loadedJson = JsonStorage.loadFromFile(jsonFile);
            if (loadedJson.size() == 12 && loadedJson.get(0).getId().equals("STU-1001")) {
                System.out.println("✔ Test 2 Passed: JSON persistence round-trip successful.");
                passed++;
            } else {
                System.err.println("✘ Test 2 Failed: JSON round-trip mismatch.");
                failed++;
            }

            // Test 3: Business Service Operations
            StudentService service = new StudentService(repo);

            // Adding a student
            String newId = service.generateNextId();
            Student newStudent = new Student(newId, "Grace", "Hopper", "grace.hopper@university.edu",
                    "+1 (555) 999-1111", "2002-12-09", "Female", Department.COMPUTER_SCIENCE,
                    4, 3.99, StudentStatus.ACTIVE, "2022-09-01", "Washington, DC");
            service.addStudent(newStudent);

            if (service.getAllStudents().size() == 13 && service.getStudentById(newId).isPresent()) {
                System.out.println("✔ Test 3 Passed: Successfully registered new student " + newId);
                passed++;
            } else {
                System.err.println("✘ Test 3 Failed: Student addition failed.");
                failed++;
            }

            // Test 4: Validation checks
            try {
                // Invalid email test
                Student invalidStudent = new Student(service.generateNextId(), "Bad", "Email", "invalid-email-address",
                        "", "2000-01-01", "Male", Department.CIVIL_ENG, 1, 3.0, StudentStatus.ACTIVE, "2024-01-01", "");
                service.addStudent(invalidStudent);
                System.err.println("✘ Test 4 Failed: Expected IllegalArgumentException for invalid email.");
                failed++;
            } catch (IllegalArgumentException e) {
                System.out.println("✔ Test 4 Passed: Validation properly rejected invalid email (" + e.getMessage() + ")");
                passed++;
            }

            // Test 5: Search and Filtering
            List<Student> searchResults = service.filterStudents("Hopper", null, null, null, "name", true);
            if (searchResults.size() == 1 && searchResults.get(0).getLastName().equals("Hopper")) {
                System.out.println("✔ Test 5 Passed: Search filter returned correct record.");
                passed++;
            } else {
                System.err.println("✘ Test 5 Failed: Search filter mismatch.");
                failed++;
            }

            // Department filter
            List<Student> csStudents = service.filterStudents(null, Department.COMPUTER_SCIENCE, null, null, "id", true);
            if (csStudents.size() >= 3) {
                System.out.println("✔ Test 6 Passed: Department filter returned " + csStudents.size() + " CS students.");
                passed++;
            } else {
                System.err.println("✘ Test 6 Failed: Department filter unexpected count.");
                failed++;
            }

            // Test 7: Analytics and Statistics
            StudentStatistics stats = service.calculateStatistics();
            if (stats.getTotalStudents() == 13 && stats.getActiveStudents() >= 8 && stats.getAverageGpa() > 3.0) {
                System.out.println("✔ Test 7 Passed: Computed analytics (Total: " + stats.getTotalStudents() +
                        ", Active Rate: " + stats.getActivePercentage() + "%, Avg GPA: " + stats.getAverageGpa() + ")");
                passed++;
            } else {
                System.err.println("✘ Test 7 Failed: Statistics calculation error.");
                failed++;
            }

            // Test 8: CSV Export & Import
            File csvFile = tempDir.resolve("export_test.csv").toFile();
            service.exportStudentsToCsv(csvFile, null);
            List<Student> importedCsv = CsvExporter.importFromCsv(csvFile);
            if (importedCsv.size() == 13) {
                System.out.println("✔ Test 8 Passed: CSV export and import verified with " + importedCsv.size() + " records.");
                passed++;
            } else {
                System.err.println("✘ Test 8 Failed: CSV mismatch. Exported 13, read " + importedCsv.size());
                failed++;
            }

            // Test 9: Deletion
            service.deleteStudent(newId);
            if (service.getAllStudents().size() == 12 && service.getStudentById(newId).isEmpty()) {
                System.out.println("✔ Test 9 Passed: Successfully deleted student " + newId);
                passed++;
            } else {
                System.err.println("✘ Test 9 Failed: Deletion failed.");
                failed++;
            }

            // Cleanup
            try {
                Files.deleteIfExists(jsonFile);
                Files.deleteIfExists(csvFile.toPath());
                Files.deleteIfExists(tempDir);
            } catch (Exception ignored) {}

        } catch (Exception e) {
            System.err.println("✘ Unexpected Exception during tests: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        System.out.println("==================================================");
        System.out.println("TEST RESULTS: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
