package com.sms.repository;

import com.sms.exception.StorageException;
import com.sms.model.Student;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Enterprise Data Access Object (DAO) interface defining persistence operations
 * for Student records. Decouples business logic from physical storage technology.
 */
public interface StudentRepository {

    /**
     * Retrieves all student records.
     */
    List<Student> getAll();

    /**
     * Finds a student by unique ID.
     */
    Optional<Student> findById(String id);

    /**
     * Finds a student by unique Email.
     */
    Optional<Student> findByEmail(String email);

    /**
     * Checks if a student with the given ID exists.
     */
    boolean existsById(String id);

    /**
     * Checks if an email is registered to any student other than excludeId.
     */
    boolean existsByEmail(String email, String excludeId);

    /**
     * Persists a new student record.
     */
    boolean add(Student student) throws StorageException;

    /**
     * Updates an existing student record.
     */
    boolean update(Student student) throws StorageException;

    /**
     * Deletes a student by ID.
     */
    boolean delete(String id) throws StorageException;

    /**
     * Generates the next sequential unique student ID (e.g. STU-1013).
     */
    String generateNextId();

    /**
     * Resets repository dataset to default pre-populated sample records.
     */
    void resetToSampleData() throws StorageException;

    /**
     * Imports records from a CSV file.
     *
     * @return count of newly added or updated records
     */
    int importCsv(File file) throws StorageException;

    /**
     * Exports all stored records to a CSV file.
     */
    void exportCsv(File file) throws StorageException;

    /**
     * Returns total count of student records.
     */
    int count();

    /**
     * Slices student records for paginated browsing.
     */
    List<Student> getPaged(int offset, int limit);

    /**
     * Reloads data from physical storage.
     */
    void reload() throws StorageException;
}
