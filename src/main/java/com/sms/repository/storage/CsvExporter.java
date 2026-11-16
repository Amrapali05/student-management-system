package com.sms.repository.storage;

import com.sms.exception.StorageException;
import com.sms.model.Department;
import com.sms.model.Student;
import com.sms.model.StudentStatus;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise CSV import and export processor with RFC-4180 quote escaping support.
 */
public class CsvExporter {

    private static final String CSV_HEADER = "ID,FirstName,LastName,Email,Phone,DateOfBirth,Gender,Department,Year,GPA,Status,EnrollmentDate,Address";

    public static void exportToCsv(File file, List<Student> students) throws StorageException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.println(CSV_HEADER);
            if (students != null) {
                for (Student s : students) {
                    writer.println(toCsvLine(s));
                }
            }
        } catch (IOException e) {
            throw new StorageException("Failed to export students to CSV file " + file.getAbsolutePath() + ": " + e.getMessage(), e);
        }
    }

    public static List<Student> importFromCsv(File file) throws StorageException {
        List<Student> list = new ArrayList<>();
        if (!file.exists()) {
            throw new StorageException("CSV import file does not exist: " + file.getAbsolutePath());
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // Header
            if (line == null) {
                return list;
            }

            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;

                List<String> tokens = parseCsvLine(line);
                if (tokens.size() >= 4) {
                    try {
                        Student s = fromTokens(tokens);
                        if (s != null) {
                            list.add(s);
                        }
                    } catch (Exception e) {
                        System.err.println("Skipping malformed CSV line " + lineNum + ": " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            throw new StorageException("Failed to read CSV file: " + e.getMessage(), e);
        }

        return list;
    }

    private static String toCsvLine(Student s) {
        return escapeCsv(s.getId()) + "," +
                escapeCsv(s.getFirstName()) + "," +
                escapeCsv(s.getLastName()) + "," +
                escapeCsv(s.getEmail()) + "," +
                escapeCsv(s.getPhone()) + "," +
                escapeCsv(s.getDateOfBirth()) + "," +
                escapeCsv(s.getGender()) + "," +
                escapeCsv(s.getDepartment() != null ? s.getDepartment().name() : "") + "," +
                s.getAcademicYear() + "," +
                String.format(java.util.Locale.US, "%.2f", s.getGpa()) + "," +
                escapeCsv(s.getStatus() != null ? s.getStatus().name() : "") + "," +
                escapeCsv(s.getEnrollmentDate()) + "," +
                escapeCsv(s.getAddress());
    }

    private static String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n") || val.contains("\r")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    public static List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++; // skip escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens;
    }

    private static Student fromTokens(List<String> t) {
        String id = t.get(0);
        String fName = t.size() > 1 ? t.get(1) : "";
        String lName = t.size() > 2 ? t.get(2) : "";
        String email = t.size() > 3 ? t.get(3) : "";
        String phone = t.size() > 4 ? t.get(4) : "";
        String dob = t.size() > 5 ? t.get(5) : "";
        String gender = t.size() > 6 ? t.get(6) : "Female";

        Department dept = Department.COMPUTER_SCIENCE;
        if (t.size() > 7 && !t.get(7).isEmpty()) {
            try {
                dept = Department.valueOf(t.get(7).toUpperCase());
            } catch (Exception ignored) {}
        }

        int year = 1;
        if (t.size() > 8 && !t.get(8).isEmpty()) {
            try {
                year = Integer.parseInt(t.get(8));
            } catch (Exception ignored) {}
        }

        double gpa = 3.0;
        if (t.size() > 9 && !t.get(9).isEmpty()) {
            try {
                gpa = Double.parseDouble(t.get(9));
            } catch (Exception ignored) {}
        }

        StudentStatus status = StudentStatus.ACTIVE;
        if (t.size() > 10 && !t.get(10).isEmpty()) {
            try {
                status = StudentStatus.valueOf(t.get(10).toUpperCase());
            } catch (Exception ignored) {}
        }

        String enroll = t.size() > 11 ? t.get(11) : "";
        String address = t.size() > 12 ? t.get(12) : "";

        return new Student(id, fName, lName, email, phone, dob, gender, dept, year, gpa, status, enroll, address);
    }
}
