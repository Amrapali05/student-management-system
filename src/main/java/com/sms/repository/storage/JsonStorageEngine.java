package com.sms.repository.storage;

import com.sms.exception.StorageException;
import com.sms.model.Department;
import com.sms.model.Student;
import com.sms.model.StudentStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enterprise JSON persistence engine featuring atomic file writes,
 * automated backups, and disaster recovery.
 */
public class JsonStorageEngine {
    private static final Logger LOGGER = Logger.getLogger(JsonStorageEngine.class.getName());

    /**
     * Atomically writes student records to file with automatic backup creation.
     */
    public static void saveToFile(Path targetPath, List<Student> students) throws StorageException {
        try {
            if (targetPath.getParent() != null && !Files.exists(targetPath.getParent())) {
                Files.createDirectories(targetPath.getParent());
            }

            // 1. Serialize in-memory to JSON string
            String json = serialize(students);

            // 2. Write to a temporary file first
            Path tempPath = targetPath.resolveSibling(targetPath.getFileName().toString() + ".tmp");
            Files.writeString(tempPath, json, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

            // 3. If target file already exists, create a backup (.bak)
            if (Files.exists(targetPath)) {
                Path backupPath = targetPath.resolveSibling(targetPath.getFileName().toString() + ".bak");
                try {
                    Files.copy(targetPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to create backup file: {0}", e.getMessage());
                }
            }

            // 4. Atomically replace target file with temp file
            try {
                Files.move(tempPath, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                // Fallback for filesystems that do not support atomic move
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            LOGGER.log(Level.FINE, "Persisted {0} students atomically to {1}", new Object[]{students.size(), targetPath});
        } catch (IOException e) {
            throw new StorageException("Failed to persist student records to " + targetPath + ": " + e.getMessage(), e);
        }
    }

    /**
     * Loads student records from file, automatically recovering from backup if primary file is corrupted.
     */
    public static List<Student> loadFromFile(Path targetPath) throws StorageException {
        if (!Files.exists(targetPath)) {
            return new ArrayList<>();
        }

        try {
            String json = Files.readString(targetPath, StandardCharsets.UTF_8);
            return deserialize(json);
        } catch (Exception primaryEx) {
            LOGGER.log(Level.WARNING, "Primary file {0} unreadable ({1}). Attempting disaster recovery from backup...", 
                    new Object[]{targetPath, primaryEx.getMessage()});

            Path backupPath = targetPath.resolveSibling(targetPath.getFileName().toString() + ".bak");
            if (Files.exists(backupPath)) {
                try {
                    String backupJson = Files.readString(backupPath, StandardCharsets.UTF_8);
                    List<Student> recovered = deserialize(backupJson);
                    LOGGER.log(Level.INFO, "Successfully recovered {0} records from backup {1}", 
                            new Object[]{recovered.size(), backupPath});
                    return recovered;
                } catch (Exception backupEx) {
                    LOGGER.log(Level.SEVERE, "Both primary and backup files are corrupted.", backupEx);
                }
            }

            throw new StorageException("Failed to load records from " + targetPath, primaryEx);
        }
    }

    public static String serialize(List<Student> students) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        if (students != null) {
            for (int i = 0; i < students.size(); i++) {
                Student s = students.get(i);
                sb.append("  {\n");
                sb.append("    \"id\": ").append(escapeJson(s.getId())).append(",\n");
                sb.append("    \"firstName\": ").append(escapeJson(s.getFirstName())).append(",\n");
                sb.append("    \"lastName\": ").append(escapeJson(s.getLastName())).append(",\n");
                sb.append("    \"email\": ").append(escapeJson(s.getEmail())).append(",\n");
                sb.append("    \"phone\": ").append(escapeJson(s.getPhone())).append(",\n");
                sb.append("    \"dateOfBirth\": ").append(escapeJson(s.getDateOfBirth())).append(",\n");
                sb.append("    \"gender\": ").append(escapeJson(s.getGender())).append(",\n");
                sb.append("    \"department\": ").append(escapeJson(s.getDepartment() != null ? s.getDepartment().name() : "")).append(",\n");
                sb.append("    \"academicYear\": ").append(s.getAcademicYear()).append(",\n");
                sb.append("    \"gpa\": ").append(String.format(java.util.Locale.US, "%.2f", s.getGpa())).append(",\n");
                sb.append("    \"status\": ").append(escapeJson(s.getStatus() != null ? s.getStatus().name() : "")).append(",\n");
                sb.append("    \"enrollmentDate\": ").append(escapeJson(s.getEnrollmentDate())).append(",\n");
                sb.append("    \"address\": ").append(escapeJson(s.getAddress())).append("\n");
                sb.append("  }");
                if (i < students.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
        }
        sb.append("]\n");
        return sb.toString();
    }

    private static String escapeJson(String str) {
        if (str == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 32 || c >= 127) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    public static List<Student> deserialize(String json) {
        List<Student> students = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) {
            return students;
        }

        int index = 0;
        int length = json.length();

        while (index < length && json.charAt(index) != '[') {
            index++;
        }
        if (index >= length) return students;
        index++; // Skip '['

        while (index < length) {
            while (index < length && Character.isWhitespace(json.charAt(index))) index++;
            if (index >= length || json.charAt(index) == ']') break;
            if (json.charAt(index) == ',') {
                index++;
                continue;
            }

            if (json.charAt(index) == '{') {
                int objStart = index;
                int braceDepth = 0;
                boolean inQuotes = false;
                boolean escape = false;

                while (index < length) {
                    char c = json.charAt(index);
                    if (escape) {
                        escape = false;
                    } else if (c == '\\') {
                        escape = true;
                    } else if (c == '"') {
                        inQuotes = !inQuotes;
                    } else if (!inQuotes) {
                        if (c == '{') braceDepth++;
                        else if (c == '}') {
                            braceDepth--;
                            if (braceDepth == 0) {
                                index++;
                                break;
                            }
                        }
                    }
                    index++;
                }

                String objJson = json.substring(objStart, index);
                Student s = parseStudentObject(objJson);
                if (s != null) {
                    students.add(s);
                }
            } else {
                index++;
            }
        }

        return students;
    }

    private static Student parseStudentObject(String objJson) {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        int i = 0;
        int len = objJson.length();

        while (i < len) {
            while (i < len && objJson.charAt(i) != '"' && objJson.charAt(i) != '}') i++;
            if (i >= len || objJson.charAt(i) == '}') break;

            // Read key
            i++; // skip opening quote
            int keyStart = i;
            while (i < len && objJson.charAt(i) != '"') {
                if (objJson.charAt(i) == '\\') i++;
                i++;
            }
            String key = objJson.substring(keyStart, i);
            i++; // skip closing quote

            // Skip to colon
            while (i < len && objJson.charAt(i) != ':') i++;
            if (i >= len) break;
            i++; // skip colon

            // Skip whitespace
            while (i < len && Character.isWhitespace(objJson.charAt(i))) i++;
            if (i >= len) break;

            // Read value
            String val = null;
            if (objJson.charAt(i) == '"') {
                i++; // skip quote
                StringBuilder valSb = new StringBuilder();
                while (i < len) {
                    char c = objJson.charAt(i);
                    if (c == '\\' && i + 1 < len) {
                        char next = objJson.charAt(i + 1);
                        if (next == 'n') valSb.append('\n');
                        else if (next == 'r') valSb.append('\r');
                        else if (next == 't') valSb.append('\t');
                        else if (next == '"') valSb.append('"');
                        else if (next == '\\') valSb.append('\\');
                        else valSb.append(next);
                        i += 2;
                    } else if (c == '"') {
                        i++;
                        break;
                    } else {
                        valSb.append(c);
                        i++;
                    }
                }
                val = valSb.toString();
            } else {
                int valStart = i;
                while (i < len && objJson.charAt(i) != ',' && objJson.charAt(i) != '}' && !Character.isWhitespace(objJson.charAt(i))) {
                    i++;
                }
                val = objJson.substring(valStart, i).trim();
                if ("null".equals(val)) val = null;
            }

            map.put(key, val);
        }

        if (map.isEmpty()) return null;

        String id = map.get("id");
        String fName = map.get("firstName");
        String lName = map.get("lastName");
        String email = map.get("email");
        String phone = map.get("phone");
        String dob = map.get("dateOfBirth");
        String gender = map.get("gender");

        Department dept = Department.COMPUTER_SCIENCE;
        try {
            String deptStr = map.get("department");
            if (deptStr != null && !deptStr.isEmpty()) {
                dept = Department.valueOf(deptStr);
            }
        } catch (Exception ignored) {}

        int year = 1;
        try {
            String yStr = map.get("academicYear");
            if (yStr != null) year = Integer.parseInt(yStr);
        } catch (Exception ignored) {}

        double gpa = 0.0;
        try {
            String gpaStr = map.get("gpa");
            if (gpaStr != null) gpa = Double.parseDouble(gpaStr);
        } catch (Exception ignored) {}

        StudentStatus status = StudentStatus.ACTIVE;
        try {
            String stStr = map.get("status");
            if (stStr != null && !stStr.isEmpty()) {
                status = StudentStatus.valueOf(stStr);
            }
        } catch (Exception ignored) {}

        String enroll = map.get("enrollmentDate");
        String address = map.get("address");

        return new Student(id, fName, lName, email, phone, dob, gender, dept, year, gpa, status, enroll, address);
    }
}
