package com.sms.repository.impl;

import com.sms.exception.DataAccessException;
import com.sms.exception.StorageException;
import com.sms.model.Department;
import com.sms.model.Student;
import com.sms.model.StudentStatus;
import com.sms.repository.StudentRepository;
import com.sms.repository.storage.CsvExporter;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enterprise relational database persistence implementation of {@link StudentRepository}.
 * Employs standard JDBC with {@link PreparedStatement} for safe parameter binding
 * and SQL-injection prevention.
 */
public class DatabaseStudentRepository implements StudentRepository {

    private static final Logger LOGGER = Logger.getLogger(DatabaseStudentRepository.class.getName());

    public DatabaseStudentRepository() {
        DatabaseConnectionFactory.initializeSchema();
        if (count() == 0) {
            LOGGER.info("Database students table is empty. Seeding initial records...");
            initSampleData();
        }
    }

    @Override
    public List<Student> getAll() {
        String sql = "SELECT * FROM students ORDER BY id ASC";
        List<Student> list = new ArrayList<>();
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch all students: " + e.getMessage(), e);
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public Optional<Student> findById(String id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT * FROM students WHERE UPPER(id) = UPPER(?)";
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find student by ID " + id + ": " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Student> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String sql = "SELECT * FROM students WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find student by email: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public boolean existsById(String id) {
        if (id == null) return false;
        String sql = "SELECT 1 FROM students WHERE UPPER(id) = UPPER(?)";
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error checking existence of ID " + id, e);
        }
    }

    @Override
    public boolean existsByEmail(String email, String excludeId) {
        if (email == null) return false;
        String sql = "SELECT id FROM students WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String existingId = rs.getString("id");
                    return excludeId == null || !existingId.equalsIgnoreCase(excludeId.trim());
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error checking existence of email " + email, e);
        }
        return false;
    }

    @Override
    public boolean add(Student s) throws StorageException {
        if (s == null || s.getId() == null) return false;
        if (existsById(s.getId())) return false;

        String sql = "INSERT INTO students (id, first_name, last_name, email, phone, date_of_birth, " +
                "gender, department, academic_year, gpa, status, enrollment_date, address) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindStudentParameters(ps, s);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert student " + s.getId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Student s) throws StorageException {
        if (s == null || s.getId() == null) return false;

        String sql = "UPDATE students SET first_name=?, last_name=?, email=?, phone=?, date_of_birth=?, " +
                "gender=?, department=?, academic_year=?, gpa=?, status=?, enrollment_date=?, address=? " +
                "WHERE UPPER(id) = UPPER(?)";
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getFirstName());
            ps.setString(2, s.getLastName());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getPhone());
            ps.setString(5, s.getDateOfBirth());
            ps.setString(6, s.getGender());
            ps.setString(7, s.getDepartment() != null ? s.getDepartment().name() : "");
            ps.setInt(8, s.getAcademicYear());
            ps.setDouble(9, s.getGpa());
            ps.setString(10, s.getStatus() != null ? s.getStatus().name() : "");
            ps.setString(11, s.getEnrollmentDate());
            ps.setString(12, s.getAddress());
            ps.setString(13, s.getId().trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update student " + s.getId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String id) throws StorageException {
        if (id == null) return false;
        String sql = "DELETE FROM students WHERE UPPER(id) = UPPER(?)";
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete student " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public String generateNextId() {
        String sql = "SELECT id FROM students WHERE UPPER(id) LIKE 'STU-%'";
        int max = 1000;
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("id");
                try {
                    int num = Integer.parseInt(id.substring(4).trim());
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error querying max ID: {0}", e.getMessage());
        }
        return "STU-" + (max + 1);
    }

    @Override
    public void resetToSampleData() throws StorageException {
        String sql = "DELETE FROM students";
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            initSampleData();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to reset student database: " + e.getMessage(), e);
        }
    }

    @Override
    public int importCsv(File file) throws StorageException {
        List<Student> imported = CsvExporter.importFromCsv(file);
        int count = 0;
        for (Student s : imported) {
            if (s.getId() == null || s.getId().trim().isEmpty()) {
                s.setId(generateNextId());
            }
            if (existsById(s.getId())) {
                update(s);
            } else {
                add(s);
            }
            count++;
        }
        return count;
    }

    @Override
    public void exportCsv(File file) throws StorageException {
        CsvExporter.exportToCsv(file, getAll());
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM students";
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error querying count: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public List<Student> getPaged(int offset, int limit) {
        String sql = "SELECT * FROM students ORDER BY id ASC LIMIT ? OFFSET ?";
        List<Student> list = new ArrayList<>();
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch paged students: " + e.getMessage(), e);
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public void reload() {
        // In a database, state is always live.
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String fName = rs.getString("first_name");
        String lName = rs.getString("last_name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String dob = rs.getString("date_of_birth");
        String gender = rs.getString("gender");

        Department dept = Department.COMPUTER_SCIENCE;
        try {
            String deptStr = rs.getString("department");
            if (deptStr != null && !deptStr.isEmpty()) dept = Department.valueOf(deptStr);
        } catch (Exception ignored) {}

        int year = rs.getInt("academic_year");
        double gpa = rs.getDouble("gpa");

        StudentStatus status = StudentStatus.ACTIVE;
        try {
            String stStr = rs.getString("status");
            if (stStr != null && !stStr.isEmpty()) status = StudentStatus.valueOf(stStr);
        } catch (Exception ignored) {}

        String enroll = rs.getString("enrollment_date");
        String address = rs.getString("address");

        return new Student(id, fName, lName, email, phone, dob, gender, dept, year, gpa, status, enroll, address);
    }

    private void bindStudentParameters(PreparedStatement ps, Student s) throws SQLException {
        ps.setString(1, s.getId());
        ps.setString(2, s.getFirstName());
        ps.setString(3, s.getLastName());
        ps.setString(4, s.getEmail());
        ps.setString(5, s.getPhone());
        ps.setString(6, s.getDateOfBirth());
        ps.setString(7, s.getGender());
        ps.setString(8, s.getDepartment() != null ? s.getDepartment().name() : "");
        ps.setInt(9, s.getAcademicYear());
        ps.setDouble(10, s.getGpa());
        ps.setString(11, s.getStatus() != null ? s.getStatus().name() : "");
        ps.setString(12, s.getEnrollmentDate());
        ps.setString(13, s.getAddress());
    }

    private void initSampleData() {
        add(new Student("STU-1001", "Emma", "Watson", "emma.watson@university.edu", "+1 (555) 234-5678",
                "2003-04-15", "Female", Department.COMPUTER_SCIENCE, 3, 3.88, StudentStatus.ACTIVE, "2023-09-01", "742 Evergreen Terrace, Seattle, WA"));
        add(new Student("STU-1002", "Alexander", "Hayes", "alex.hayes@university.edu", "+1 (555) 345-6789",
                "2002-11-20", "Male", Department.DATA_SCIENCE, 4, 3.95, StudentStatus.ACTIVE, "2022-09-01", "10880 Wilshire Blvd, Los Angeles, CA"));
        add(new Student("STU-1003", "Sophia", "Chen", "sophia.chen@university.edu", "+1 (555) 456-7890",
                "2004-02-10", "Female", Department.ELECTRICAL_ENG, 2, 3.72, StudentStatus.ACTIVE, "2024-09-01", "450 Serra Mall, Stanford, CA"));
        add(new Student("STU-1004", "Marcus", "Johnson", "marcus.j@university.edu", "+1 (555) 567-8901",
                "2001-08-30", "Male", Department.BUSINESS_ADMIN, 4, 3.45, StudentStatus.GRADUATED, "2021-09-01", "200 Park Ave, New York, NY"));
        add(new Student("STU-1005", "Olivia", "Martinez", "olivia.m@university.edu", "+1 (555) 678-9012",
                "2003-07-25", "Female", Department.BIOMEDICAL, 3, 3.65, StudentStatus.ACTIVE, "2023-09-01", "300 Pasteur Dr, Palo Alto, CA"));
        add(new Student("STU-1006", "Lucas", "Kim", "lucas.kim@university.edu", "+1 (555) 789-0123",
                "2004-12-05", "Male", Department.COMPUTER_SCIENCE, 1, 3.20, StudentStatus.ACTIVE, "2025-01-15", "123 Tech Way, Austin, TX"));
        add(new Student("STU-1007", "Isabella", "Taylor", "isabella.t@university.edu", "+1 (555) 890-1234",
                "2002-03-18", "Female", Department.MECHANICAL_ENG, 4, 2.85, StudentStatus.INACTIVE, "2022-09-01", "555 Campus Drive, Boston, MA"));
        add(new Student("STU-1008", "David", "Miller", "david.m@university.edu", "+1 (555) 901-2345",
                "2003-09-14", "Male", Department.CIVIL_ENG, 2, 3.10, StudentStatus.ACTIVE, "2024-09-01", "88 University Ave, Denver, CO"));
        add(new Student("STU-1009", "Aria", "Patel", "aria.patel@university.edu", "+1 (555) 012-3456",
                "2004-05-22", "Female", Department.DATA_SCIENCE, 2, 4.00, StudentStatus.ACTIVE, "2024-09-01", "700 Forbes Ave, Pittsburgh, PA"));
        add(new Student("STU-1010", "Noah", "Williams", "noah.w@university.edu", "+1 (555) 123-4567",
                "2002-06-12", "Male", Department.BUSINESS_ADMIN, 3, 2.40, StudentStatus.SUSPENDED, "2023-09-01", "401 Terry Ave N, Chicago, IL"));
        add(new Student("STU-1011", "Chloe", "Garcia", "chloe.g@university.edu", "+1 (555) 234-8765",
                "2003-10-08", "Female", Department.COMPUTER_SCIENCE, 3, 3.78, StudentStatus.ACTIVE, "2023-09-01", "100 Bay State Rd, San Jose, CA"));
        add(new Student("STU-1012", "Ethan", "Wright", "ethan.w@university.edu", "+1 (555) 345-9876",
                "2001-01-19", "Male", Department.ELECTRICAL_ENG, 4, 3.52, StudentStatus.GRADUATED, "2021-09-01", "77 Massachusetts Ave, Cambridge, MA"));
    }
}
