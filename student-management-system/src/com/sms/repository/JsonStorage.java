package com.sms.repository;

import com.sms.model.Department;
import com.sms.model.Student;
import com.sms.model.StudentStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight, zero-dependency JSON serializer and parser for Student records.
 */
public class JsonStorage {

    public static void saveToFile(Path path, List<Student> students) throws IOException {
        String json = serialize(students);
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        Files.writeString(path, json, StandardCharsets.UTF_8);
    }

    public static List<Student> loadFromFile(Path path) throws IOException {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        String json = Files.readString(path, StandardCharsets.UTF_8);
        return deserialize(json);
    }

    public static String serialize(List<Student> students) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
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

        // Find start of array
        while (index < length && json.charAt(index) != '[') {
            index++;
        }
        if (index >= length) return students;
        index++; // Skip '['

        while (index < length) {
            // Skip whitespace and commas
            while (index < length && (Character.isWhitespace(json.charAt(index)) || json.charAt(index) == ',')) {
                index++;
            }
            if (index >= length || json.charAt(index) == ']') {
                break;
            }

            if (json.charAt(index) == '{') {
                int objStart = index;
                int braceDepth = 0;
                boolean insideQuotes = false;
                boolean escaped = false;

                while (index < length) {
                    char c = json.charAt(index);
                    if (escaped) {
                        escaped = false;
                    } else if (c == '\\') {
                        escaped = true;
                    } else if (c == '"') {
                        insideQuotes = !insideQuotes;
                    } else if (!insideQuotes) {
                        if (c == '{') braceDepth++;
                        else if (c == '}') {
                            braceDepth--;
                            if (braceDepth == 0) {
                                index++; // include the closing brace
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
        Map<String, String> map = new HashMap<>();
        int i = 0;
        int len = objJson.length();

        while (i < len) {
            // Find key starting quote
            while (i < len && objJson.charAt(i) != '"') {
                if (objJson.charAt(i) == '}') break;
                i++;
            }
            if (i >= len || objJson.charAt(i) == '}') break;
            i++; // Skip opening quote

            // Read key
            StringBuilder keySb = new StringBuilder();
            while (i < len && objJson.charAt(i) != '"') {
                keySb.append(objJson.charAt(i));
                i++;
            }
            i++; // Skip closing quote of key

            // Find colon
            while (i < len && objJson.charAt(i) != ':') {
                i++;
            }
            i++; // Skip colon

            // Skip whitespace
            while (i < len && Character.isWhitespace(objJson.charAt(i))) {
                i++;
            }

            // Read value
            StringBuilder valSb = new StringBuilder();
            if (i < len && objJson.charAt(i) == '"') {
                // String value
                i++; // Skip opening quote
                boolean escaped = false;
                while (i < len) {
                    char c = objJson.charAt(i);
                    if (escaped) {
                        if (c == 'n') valSb.append('\n');
                        else if (c == 't') valSb.append('\t');
                        else if (c == 'r') valSb.append('\r');
                        else if (c == '\"') valSb.append('\"');
                        else if (c == '\\') valSb.append('\\');
                        else valSb.append(c);
                        escaped = false;
                    } else if (c == '\\') {
                        escaped = true;
                    } else if (c == '"') {
                        i++; // Skip closing quote
                        break;
                    } else {
                        valSb.append(c);
                    }
                    i++;
                }
            } else {
                // Numeric / Boolean / null value
                while (i < len && objJson.charAt(i) != ',' && objJson.charAt(i) != '}') {
                    valSb.append(objJson.charAt(i));
                    i++;
                }
            }

            map.put(keySb.toString().trim(), valSb.toString().trim());

            // Move to next delimiter
            while (i < len && objJson.charAt(i) != ',' && objJson.charAt(i) != '}') {
                i++;
            }
            if (i < len && objJson.charAt(i) == ',') {
                i++;
            }
        }

        if (map.isEmpty() || !map.containsKey("id")) {
            return null;
        }

        Student s = new Student();
        s.setId(map.getOrDefault("id", ""));
        s.setFirstName(map.getOrDefault("firstName", ""));
        s.setLastName(map.getOrDefault("lastName", ""));
        s.setEmail(map.getOrDefault("email", ""));
        s.setPhone(map.getOrDefault("phone", ""));
        s.setDateOfBirth(map.getOrDefault("dateOfBirth", ""));
        s.setGender(map.getOrDefault("gender", "Female"));
        s.setDepartment(Department.fromString(map.get("department")));
        
        try {
            s.setAcademicYear(Integer.parseInt(map.getOrDefault("academicYear", "1")));
        } catch (NumberFormatException e) {
            s.setAcademicYear(1);
        }

        try {
            s.setGpa(Double.parseDouble(map.getOrDefault("gpa", "3.0")));
        } catch (NumberFormatException e) {
            s.setGpa(3.0);
        }

        s.setStatus(StudentStatus.fromString(map.get("status")));
        s.setEnrollmentDate(map.getOrDefault("enrollmentDate", ""));
        s.setAddress(map.getOrDefault("address", ""));

        return s;
    }
}
