package com.sms.repository;

import com.sms.model.Department;
import com.sms.model.Student;
import com.sms.model.StudentStatus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles CSV export and import operations for Student records.
 */
public class CsvExporter {

    private static final String HEADER = "ID,First Name,Last Name,Email,Phone,Date of Birth,Gender,Department,Academic Year,GPA,Status,Enrollment Date,Address";

    public static void exportToCsv(File file, List<Student> students) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write(HEADER);
            writer.newLine();

            for (Student s : students) {
                StringBuilder line = new StringBuilder();
                line.append(escapeCsv(s.getId())).append(",");
                line.append(escapeCsv(s.getFirstName())).append(",");
                line.append(escapeCsv(s.getLastName())).append(",");
                line.append(escapeCsv(s.getEmail())).append(",");
                line.append(escapeCsv(s.getPhone())).append(",");
                line.append(escapeCsv(s.getDateOfBirth())).append(",");
                line.append(escapeCsv(s.getGender())).append(",");
                line.append(escapeCsv(s.getDepartment() != null ? s.getDepartment().getFullName() : "")).append(",");
                line.append(s.getAcademicYear()).append(",");
                line.append(String.format(java.util.Locale.US, "%.2f", s.getGpa())).append(",");
                line.append(escapeCsv(s.getStatus() != null ? s.getStatus().getDisplayName() : "")).append(",");
                line.append(escapeCsv(s.getEnrollmentDate())).append(",");
                line.append(escapeCsv(s.getAddress()));

                writer.write(line.toString());
                writer.newLine();
            }
        }
    }

    public static List<Student> importFromCsv(File file) throws IOException {
        List<Student> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            boolean isFirst = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (isFirst) {
                    isFirst = false;
                    // Skip header if it matches header tokens
                    if (line.toLowerCase().contains("email") || line.toLowerCase().contains("first name")) {
                        continue;
                    }
                }

                List<String> tokens = parseCsvLine(line);
                if (tokens.size() >= 4) { // Minimum required to parse
                    Student s = new Student();
                    s.setId(getSafe(tokens, 0));
                    s.setFirstName(getSafe(tokens, 1));
                    s.setLastName(getSafe(tokens, 2));
                    s.setEmail(getSafe(tokens, 3));
                    s.setPhone(getSafe(tokens, 4));
                    s.setDateOfBirth(getSafe(tokens, 5));
                    s.setGender(getSafe(tokens, 6, "Female"));
                    s.setDepartment(Department.fromString(getSafe(tokens, 7)));

                    try {
                        s.setAcademicYear(Integer.parseInt(getSafe(tokens, 8, "1")));
                    } catch (NumberFormatException e) {
                        s.setAcademicYear(1);
                    }

                    try {
                        s.setGpa(Double.parseDouble(getSafe(tokens, 9, "3.0")));
                    } catch (NumberFormatException e) {
                        s.setGpa(3.0);
                    }

                    s.setStatus(StudentStatus.fromString(getSafe(tokens, 10)));
                    s.setEnrollmentDate(getSafe(tokens, 11));
                    s.setAddress(getSafe(tokens, 12));

                    list.add(s);
                }
            }
        }
        return list;
    }

    private static String getSafe(List<String> tokens, int index) {
        return getSafe(tokens, index, "");
    }

    private static String getSafe(List<String> tokens, int index, String def) {
        if (index < tokens.size() && tokens.get(index) != null && !tokens.get(index).trim().isEmpty()) {
            return tokens.get(index).trim();
        }
        return def;
    }

    private static String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n") || val.contains("\r")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    cur.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        tokens.add(cur.toString());
        return tokens;
    }
}
