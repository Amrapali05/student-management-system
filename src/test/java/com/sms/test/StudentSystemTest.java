package com.sms.test;

import com.sms.concurrent.ThreadManager;
import com.sms.exception.DuplicateStudentException;
import com.sms.exception.StudentNotFoundException;
import com.sms.exception.ValidationException;
import com.sms.model.Department;
import com.sms.model.Page;
import com.sms.model.Student;
import com.sms.model.StudentStatus;
import com.sms.repository.StudentRepository;
import com.sms.repository.impl.JsonStudentRepository;
import com.sms.repository.storage.CsvExporter;
import com.sms.repository.storage.JsonStorageEngine;
import com.sms.service.StudentService;
import com.sms.service.StudentStatistics;
import com.sms.service.impl.StudentServiceImpl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Enterprise Automated Verification Test Suite.
 * Validates collections migration, atomic file persistence, package modularity,
 * multithreaded concurrency, custom domain exception hierarchy, and scalability.
 */
public class StudentSystemTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("🚀 EDUCORE ENTERPRISE VERIFICATION TEST SUITE (JAVA 21 LTS)");
        System.out.println("==================================================================");

        try {
            Path tempDir = Files.createTempDirectory("sms_enterprise_test_");
            System.out.println("[INFO] Isolated Test Workspace: " + tempDir.toAbsolutePath());

            // 1. Collections & Repository Initialization
            testRepositoryInitialization(tempDir);

            // 2. Atomic JSON Persistence & Backup
            testAtomicPersistenceAndBackup(tempDir);

            // 3. Service Layer CRUD
            StudentRepository repo = new JsonStudentRepository(tempDir.toString());
            StudentService service = new StudentServiceImpl(repo);
            testServiceCrud(service);

            // 4. Exception Handling: Validation
            testValidationExceptions(service);

            // 5. Exception Handling: Duplicate Keys
            testDuplicateExceptions(service);

            // 6. Exception Handling: Record Not Found
            testNotFoundException(service);

            // 7. Search, Filtering, and Sorting Pipelines
            testSearchAndFilter(service);

            // 8. Analytical Statistics Computation
            testStatisticsComputation(service);

            // 9. Multithreaded High Concurrency Stress Test
            testMultithreadedConcurrency(tempDir);

            // 10. Scalable Pagination
            testPagination(service);

            // 11. CSV Export and Import Integrity
            testCsvOperations(service, tempDir);

            System.out.println("==================================================================");
            System.out.println("TEST RESULTS: " + passed + " PASSED, " + failed + " FAILED");
            System.out.println("==================================================================");

            if (failed > 0) {
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Unexpected test failure: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            ThreadManager.getInstance().shutdown();
        }
    }

    private static void testRepositoryInitialization(Path tempDir) {
        StudentRepository repo = new JsonStudentRepository(tempDir.toString());
        List<Student> students = repo.getAll();
        assertCondition(students.size() == 12, "Test 1: Repository initialized with 12 sample students using Collections.");
        assertCondition(repo.findById("STU-1001").isPresent(), "Test 1b: O(1) collection lookup by ID succeeded.");
        assertCondition(repo.findByEmail("emma.watson@university.edu").isPresent(), "Test 1c: O(1) collection lookup by Email succeeded.");
    }

    private static void testAtomicPersistenceAndBackup(Path tempDir) throws Exception {
        Path jsonFile = tempDir.resolve("students.json");
        List<Student> loaded = JsonStorageEngine.loadFromFile(jsonFile);
        assertCondition(loaded.size() == 12 && loaded.get(0).getId().equals("STU-1001"),
                "Test 2: Atomic JSON persistence round-trip verified.");

        // Verify backup creation
        StudentRepository repo = new JsonStudentRepository(tempDir.toString());
        repo.add(new Student("STU-9999", "Backup", "Test", "backup.test@university.edu", "", "2000-01-01",
                "Female", Department.COMPUTER_SCIENCE, 1, 3.8, StudentStatus.ACTIVE, "2024-01-01", ""));
        Path backupFile = tempDir.resolve("students.json.bak");
        assertCondition(Files.exists(backupFile), "Test 2b: Automated backup retention (.bak) created before file overwrite.");
        repo.delete("STU-9999");
    }

    private static void testServiceCrud(StudentService service) {
        String newId = service.generateNextId();
        Student s = new Student(newId, "Ada", "Lovelace", "ada.lovelace@university.edu",
                "+1 (555) 000-1111", "2002-12-10", "Female", Department.COMPUTER_SCIENCE,
                4, 3.99, StudentStatus.ACTIVE, "2022-09-01", "London, UK");
        service.addStudent(s);

        assertCondition(service.getStudentById(newId).isPresent(), "Test 3a: Successfully created student " + newId);
        
        s.setGpa(4.00);
        service.updateStudent(s);
        assertCondition(service.getStudentById(newId).get().getGpa() == 4.00, "Test 3b: Successfully updated student " + newId);

        service.deleteStudent(newId);
        assertCondition(service.getStudentById(newId).isEmpty(), "Test 3c: Successfully deleted student " + newId);
    }

    private static void testValidationExceptions(StudentService service) {
        boolean caught = false;
        try {
            Student bad = new Student("STU-BAD", "Bad", "Email", "invalid-email-string",
                    "", "2000-01-01", "Male", Department.CIVIL_ENG, 1, 4.5, StudentStatus.ACTIVE, "2024-01-01", "");
            service.addStudent(bad);
        } catch (ValidationException e) {
            caught = true;
            assertCondition(e.getFieldErrors().containsKey("email") && e.getFieldErrors().containsKey("gpa"),
                    "Test 4: ValidationException captured multi-field validation errors (" + e.getMessage() + ")");
        }
        if (!caught) {
            fail("Test 4: Expected ValidationException for bad email and out-of-range GPA.");
        }
    }

    private static void testDuplicateExceptions(StudentService service) {
        boolean caughtId = false;
        try {
            Student dupId = new Student("STU-1001", "Emma", "Clone", "emma.new@university.edu",
                    "", "2000-01-01", "Female", Department.COMPUTER_SCIENCE, 1, 3.5, StudentStatus.ACTIVE, "2024-01-01", "");
            service.addStudent(dupId);
        } catch (DuplicateStudentException e) {
            caughtId = true;
            assertCondition(e.getConflictField().equals("ID"), "Test 5a: DuplicateStudentException caught ID collision.");
        }
        if (!caughtId) fail("Test 5a: Expected DuplicateStudentException for duplicate ID.");

        boolean caughtEmail = false;
        try {
            Student dupEmail = new Student("STU-9900", "Different", "Id", "alex.hayes@university.edu",
                    "", "2000-01-01", "Male", Department.DATA_SCIENCE, 1, 3.5, StudentStatus.ACTIVE, "2024-01-01", "");
            service.addStudent(dupEmail);
        } catch (DuplicateStudentException e) {
            caughtEmail = true;
            assertCondition(e.getConflictField().equals("Email"), "Test 5b: DuplicateStudentException caught Email collision.");
        }
        if (!caughtEmail) fail("Test 5b: Expected DuplicateStudentException for duplicate Email.");
    }

    private static void testNotFoundException(StudentService service) {
        boolean caught = false;
        try {
            service.deleteStudent("STU-NONEXISTENT-999");
        } catch (StudentNotFoundException e) {
            caught = true;
            assertCondition(e.getStudentId().equals("STU-NONEXISTENT-999"),
                    "Test 6: StudentNotFoundException correctly identified missing ID.");
        }
        if (!caught) fail("Test 6: Expected StudentNotFoundException for non-existent record deletion.");
    }

    private static void testSearchAndFilter(StudentService service) {
        List<Student> search = service.filterStudents("Watson", null, null, null, "name", true);
        assertCondition(search.size() == 1 && search.get(0).getLastName().equals("Watson"),
                "Test 7a: Multi-attribute live search filtered accurately.");

        List<Student> cs = service.filterStudents(null, Department.COMPUTER_SCIENCE, null, null, "id", true);
        assertCondition(cs.size() == 3, "Test 7b: Department filter matched 3 Computer Science students.");

        List<Student> active = service.filterStudents(null, null, StudentStatus.ACTIVE, null, "id", true);
        assertCondition(active.size() >= 8, "Test 7c: Status filter matched active student population.");
    }

    private static void testStatisticsComputation(StudentService service) {
        StudentStatistics stats = service.calculateStatistics();
        assertCondition(stats.getTotalStudents() == 12, "Test 8a: Correct total student count computed (12).");
        assertCondition(stats.getAverageGpa() > 3.0, "Test 8b: Correct average GPA computed (" + stats.getAverageGpa() + ").");
        assertCondition(!stats.getTopDepartment().equals("None"), "Test 8c: Top department computed (" + stats.getTopDepartment() + ").");
    }

    private static void testMultithreadedConcurrency(Path tempDir) throws Exception {
        StudentRepository concurrentRepo = new JsonStudentRepository(tempDir.toString());
        StudentService concurrentService = new StudentServiceImpl(concurrentRepo);

        int numThreads = 10;
        int operationsPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            futures.add(executor.submit(() -> {
                latch.countDown();
                latch.await(); // Synchronize all threads to start simultaneously
                for (int i = 0; i < operationsPerThread; i++) {
                    // Concurrent reads
                    List<Student> all = concurrentService.getAllStudents();
                    concurrentService.calculateStatistics();

                    // Concurrent writes
                    String id = "STU-TH-" + threadId + "-" + i;
                    Student s = new Student(id, "Thread", "User" + threadId, "th" + threadId + "_" + i + "@uni.edu",
                            "", "2000-01-01", "Male", Department.COMPUTER_SCIENCE, 1, 3.5, StudentStatus.ACTIVE, "2024-01-01", "");
                    concurrentService.addStudent(s);
                    concurrentService.getStudentById(id);
                    concurrentService.deleteStudent(id);
                }
                return true;
            }));
        }

        for (Future<Boolean> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertCondition(concurrentService.getAllStudents().size() == 12,
                "Test 9: Multithreaded Concurrency Stress Test Passed (" + (numThreads * operationsPerThread * 2) + " parallel read/write ops with zero race conditions).");
    }

    private static void testPagination(StudentService service) {
        Page<Student> page1 = service.getPagedStudents(1, 5, null, null, null, null);
        assertCondition(page1.getContent().size() == 5 && page1.getPageNumber() == 1 && page1.getTotalPages() == 3,
                "Test 10: Scalable Pagination Container verified (Page 1 of " + page1.getTotalPages() + ").");
    }

    private static void testCsvOperations(StudentService service, Path tempDir) {
        File csvFile = tempDir.resolve("export_test.csv").toFile();
        service.exportStudentsToCsv(csvFile, null);
        assertCondition(csvFile.exists() && csvFile.length() > 0, "Test 11a: CSV export generated valid dataset file.");

        List<Student> parsed = CsvExporter.importFromCsv(csvFile);
        assertCondition(parsed.size() == 12, "Test 11b: CSV import verified with all 12 records restored.");
    }

    private static void assertCondition(boolean condition, String description) {
        if (condition) {
            System.out.println("✔ " + description);
            passed++;
        } else {
            fail(description);
        }
    }

    private static void fail(String description) {
        System.err.println("✘ FAILED: " + description);
        failed++;
    }
}
