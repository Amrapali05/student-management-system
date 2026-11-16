package com.sms.repository.impl;

import com.sms.exception.StorageException;
import com.sms.model.Department;
import com.sms.model.Student;
import com.sms.model.StudentStatus;
import com.sms.repository.StudentRepository;
import com.sms.repository.storage.CsvExporter;
import com.sms.repository.storage.JsonStorageEngine;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enterprise JSON file-backed implementation of {@link StudentRepository}.
 *
 * <p>Key Architecture Highlights:
 * <ul>
 *   <li><b>Collections Migration:</b> Uses {@link List}, {@link Map}, and secondary indices ({@link ConcurrentHashMap})
 *       instead of primitive fixed-size arrays, enabling O(1) key lookups and dynamic sizing.</li>
 *   <li><b>Multithreading:</b> Concurrency is governed by a {@link ReentrantReadWriteLock}, allowing high-concurrency
 *       parallel reads while strictly serializing write mutations.</li>
 *   <li><b>File Persistence:</b> Delegates to {@link JsonStorageEngine} for atomic temporary file swaps,
 *       automatic backup retention (.bak), and automated disaster recovery.</li>
 *   <li><b>Exception Handling:</b> Wraps persistence and I/O failures in custom domain {@link StorageException}.</li>
 * </ul>
 */
public class JsonStudentRepository implements StudentRepository {

    private static final Logger LOGGER = Logger.getLogger(JsonStudentRepository.class.getName());

    private final Path storagePath;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    // Primary collection and O(1) indexed secondary lookup maps
    private final List<Student> students = new ArrayList<>();
    private final Map<String, Student> idIndex = new ConcurrentHashMap<>();
    private final Map<String, String> emailIndex = new ConcurrentHashMap<>(); // lowercase email -> studentId

    public JsonStudentRepository(String dataDirectory) {
        this.storagePath = Paths.get(dataDirectory, "students.json");
        reload();
    }

    public JsonStudentRepository(Path storagePath) {
        this.storagePath = storagePath;
        reload();
    }

    @Override
    public void reload() throws StorageException {
        rwLock.writeLock().lock();
        try {
            students.clear();
            idIndex.clear();
            emailIndex.clear();

            List<Student> loaded = JsonStorageEngine.loadFromFile(storagePath);
            if (loaded.isEmpty()) {
                LOGGER.info("No existing records found at " + storagePath + ". Initializing default sample dataset.");
                initSampleData();
                save();
            } else {
                for (Student s : loaded) {
                    students.add(s);
                    indexStudent(s);
                }
                LOGGER.info("Successfully loaded and indexed " + students.size() + " student records.");
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private void indexStudent(Student s) {
        if (s.getId() != null) {
            idIndex.put(s.getId().toUpperCase(), s);
        }
        if (s.getEmail() != null) {
            emailIndex.put(s.getEmail().trim().toLowerCase(), s.getId());
        }
    }

    private void unindexStudent(Student s) {
        if (s.getId() != null) {
            idIndex.remove(s.getId().toUpperCase());
        }
        if (s.getEmail() != null) {
            emailIndex.remove(s.getEmail().trim().toLowerCase());
        }
    }

    private void save() throws StorageException {
        // Must be called with write lock held
        JsonStorageEngine.saveToFile(storagePath, students);
    }

    @Override
    public List<Student> getAll() {
        rwLock.readLock().lock();
        try {
            List<Student> copy = new ArrayList<>(students.size());
            for (Student s : students) {
                copy.add(s.clone());
            }
            return Collections.unmodifiableList(copy);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public Optional<Student> findById(String id) {
        if (id == null) return Optional.empty();
        rwLock.readLock().lock();
        try {
            Student s = idIndex.get(id.trim().toUpperCase());
            return s != null ? Optional.of(s.clone()) : Optional.empty();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public Optional<Student> findByEmail(String email) {
        if (email == null) return Optional.empty();
        rwLock.readLock().lock();
        try {
            String studentId = emailIndex.get(email.trim().toLowerCase());
            if (studentId != null) {
                return findById(studentId);
            }
            return Optional.empty();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) return false;
        rwLock.readLock().lock();
        try {
            return idIndex.containsKey(id.trim().toUpperCase());
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public boolean existsByEmail(String email, String excludeId) {
        if (email == null) return false;
        rwLock.readLock().lock();
        try {
            String existingId = emailIndex.get(email.trim().toLowerCase());
            if (existingId == null) return false;
            if (excludeId != null && existingId.equalsIgnoreCase(excludeId.trim())) {
                return false;
            }
            return true;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public boolean add(Student student) throws StorageException {
        if (student == null || student.getId() == null) return false;

        rwLock.writeLock().lock();
        try {
            String idKey = student.getId().trim().toUpperCase();
            if (idIndex.containsKey(idKey)) {
                return false;
            }

            Student clone = student.clone();
            students.add(clone);
            indexStudent(clone);
            save();
            return true;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean update(Student student) throws StorageException {
        if (student == null || student.getId() == null) return false;

        rwLock.writeLock().lock();
        try {
            String idKey = student.getId().trim().toUpperCase();
            Student oldStudent = idIndex.get(idKey);
            if (oldStudent == null) {
                return false;
            }

            unindexStudent(oldStudent);

            for (int i = 0; i < students.size(); i++) {
                if (students.get(i).getId().equalsIgnoreCase(student.getId())) {
                    Student clone = student.clone();
                    students.set(i, clone);
                    indexStudent(clone);
                    break;
                }
            }

            save();
            return true;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean delete(String id) throws StorageException {
        if (id == null) return false;

        rwLock.writeLock().lock();
        try {
            String idKey = id.trim().toUpperCase();
            Student s = idIndex.get(idKey);
            if (s == null) {
                return false;
            }

            unindexStudent(s);
            students.removeIf(item -> item.getId().equalsIgnoreCase(id.trim()));
            save();
            return true;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public String generateNextId() {
        rwLock.readLock().lock();
        try {
            int maxNum = 1000;
            for (String key : idIndex.keySet()) {
                if (key.startsWith("STU-")) {
                    try {
                        int num = Integer.parseInt(key.substring(4).trim());
                        if (num > maxNum) {
                            maxNum = num;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            return "STU-" + (maxNum + 1);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void resetToSampleData() throws StorageException {
        rwLock.writeLock().lock();
        try {
            students.clear();
            idIndex.clear();
            emailIndex.clear();
            initSampleData();
            save();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public int importCsv(File file) throws StorageException {
        List<Student> imported = CsvExporter.importFromCsv(file);

        rwLock.writeLock().lock();
        try {
            int count = 0;
            for (Student s : imported) {
                if (s.getId() == null || s.getId().trim().isEmpty()) {
                    s.setId(generateNextId());
                }

                String idKey = s.getId().trim().toUpperCase();
                if (idIndex.containsKey(idKey)) {
                    update(s);
                } else {
                    Student clone = s.clone();
                    students.add(clone);
                    indexStudent(clone);
                }
                count++;
            }
            save();
            return count;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void exportCsv(File file) throws StorageException {
        rwLock.readLock().lock();
        try {
            CsvExporter.exportToCsv(file, students);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public int count() {
        rwLock.readLock().lock();
        try {
            return students.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public List<Student> getPaged(int offset, int limit) {
        rwLock.readLock().lock();
        try {
            if (offset < 0) offset = 0;
            if (offset >= students.size()) return Collections.emptyList();
            int toIndex = Math.min(offset + limit, students.size());
            List<Student> sub = students.subList(offset, toIndex);
            List<Student> copy = new ArrayList<>(sub.size());
            for (Student s : sub) copy.add(s.clone());
            return Collections.unmodifiableList(copy);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    private void initSampleData() {
        addSample(new Student("STU-1001", "Emma", "Watson", "emma.watson@university.edu", "+1 (555) 234-5678",
                "2003-04-15", "Female", Department.COMPUTER_SCIENCE, 3, 3.88, StudentStatus.ACTIVE, "2023-09-01", "742 Evergreen Terrace, Seattle, WA"));
        addSample(new Student("STU-1002", "Alexander", "Hayes", "alex.hayes@university.edu", "+1 (555) 345-6789",
                "2002-11-20", "Male", Department.DATA_SCIENCE, 4, 3.95, StudentStatus.ACTIVE, "2022-09-01", "10880 Wilshire Blvd, Los Angeles, CA"));
        addSample(new Student("STU-1003", "Sophia", "Chen", "sophia.chen@university.edu", "+1 (555) 456-7890",
                "2004-02-10", "Female", Department.ELECTRICAL_ENG, 2, 3.72, StudentStatus.ACTIVE, "2024-09-01", "450 Serra Mall, Stanford, CA"));
        addSample(new Student("STU-1004", "Marcus", "Johnson", "marcus.j@university.edu", "+1 (555) 567-8901",
                "2001-08-30", "Male", Department.BUSINESS_ADMIN, 4, 3.45, StudentStatus.GRADUATED, "2021-09-01", "200 Park Ave, New York, NY"));
        addSample(new Student("STU-1005", "Olivia", "Martinez", "olivia.m@university.edu", "+1 (555) 678-9012",
                "2003-07-25", "Female", Department.BIOMEDICAL, 3, 3.65, StudentStatus.ACTIVE, "2023-09-01", "300 Pasteur Dr, Palo Alto, CA"));
        addSample(new Student("STU-1006", "Lucas", "Kim", "lucas.kim@university.edu", "+1 (555) 789-0123",
                "2004-12-05", "Male", Department.COMPUTER_SCIENCE, 1, 3.20, StudentStatus.ACTIVE, "2025-01-15", "123 Tech Way, Austin, TX"));
        addSample(new Student("STU-1007", "Isabella", "Taylor", "isabella.t@university.edu", "+1 (555) 890-1234",
                "2002-03-18", "Female", Department.MECHANICAL_ENG, 4, 2.85, StudentStatus.INACTIVE, "2022-09-01", "555 Campus Drive, Boston, MA"));
        addSample(new Student("STU-1008", "David", "Miller", "david.m@university.edu", "+1 (555) 901-2345",
                "2003-09-14", "Male", Department.CIVIL_ENG, 2, 3.10, StudentStatus.ACTIVE, "2024-09-01", "88 University Ave, Denver, CO"));
        addSample(new Student("STU-1009", "Aria", "Patel", "aria.patel@university.edu", "+1 (555) 012-3456",
                "2004-05-22", "Female", Department.DATA_SCIENCE, 2, 4.00, StudentStatus.ACTIVE, "2024-09-01", "700 Forbes Ave, Pittsburgh, PA"));
        addSample(new Student("STU-1010", "Noah", "Williams", "noah.w@university.edu", "+1 (555) 123-4567",
                "2002-06-12", "Male", Department.BUSINESS_ADMIN, 3, 2.40, StudentStatus.SUSPENDED, "2023-09-01", "401 Terry Ave N, Chicago, IL"));
        addSample(new Student("STU-1011", "Chloe", "Garcia", "chloe.g@university.edu", "+1 (555) 234-8765",
                "2003-10-08", "Female", Department.COMPUTER_SCIENCE, 3, 3.78, StudentStatus.ACTIVE, "2023-09-01", "100 Bay State Rd, San Jose, CA"));
        addSample(new Student("STU-1012", "Ethan", "Wright", "ethan.w@university.edu", "+1 (555) 345-9876",
                "2001-01-19", "Male", Department.ELECTRICAL_ENG, 4, 3.52, StudentStatus.GRADUATED, "2021-09-01", "77 Massachusetts Ave, Cambridge, MA"));
    }

    private void addSample(Student s) {
        students.add(s);
        indexStudent(s);
    }
}
