package com.sms.service;

import com.sms.exception.DuplicateStudentException;
import com.sms.exception.StorageException;
import com.sms.exception.StudentNotFoundException;
import com.sms.exception.ValidationException;
import com.sms.model.Department;
import com.sms.model.Page;
import com.sms.model.Student;
import com.sms.model.StudentStatus;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Enterprise Service Layer interface defining business logic, data validation,
 * search/filter pipelines, and asynchronous analytics computation.
 */
public interface StudentService {

    List<Student> getAllStudents();

    Optional<Student> getStudentById(String id);

    String generateNextId();

    void addStudent(Student student) throws ValidationException, DuplicateStudentException, StorageException;

    void updateStudent(Student student) throws ValidationException, StudentNotFoundException, DuplicateStudentException, StorageException;

    void deleteStudent(String id) throws StudentNotFoundException, StorageException;

    void resetToDefaultData() throws StorageException;

    int importStudentsFromCsv(File file) throws StorageException;

    void exportStudentsToCsv(File file, List<Student> customList) throws StorageException;

    List<Student> filterStudents(String search, Department deptFilter, StudentStatus statusFilter,
                                Integer yearFilter, String sortBy, boolean ascending);

    Page<Student> getPagedStudents(int pageNumber, int pageSize, String search,
                                  Department deptFilter, StudentStatus statusFilter, Integer yearFilter);

    StudentStatistics calculateStatistics();

    CompletableFuture<StudentStatistics> calculateStatisticsAsync();

    CompletableFuture<List<Student>> filterStudentsAsync(String search, Department deptFilter,
                                                         StudentStatus statusFilter, Integer yearFilter,
                                                         String sortBy, boolean ascending);
}
