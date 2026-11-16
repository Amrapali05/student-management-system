package com.sms.repository;

import com.sms.model.Student;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Backward compatibility facade delegating to {@link com.sms.repository.storage.CsvExporter}.
 */
public class CsvExporter {

    public static void exportToCsv(File file, List<Student> students) throws IOException {
        com.sms.repository.storage.CsvExporter.exportToCsv(file, students);
    }

    public static List<Student> importFromCsv(File file) throws IOException {
        return com.sms.repository.storage.CsvExporter.importFromCsv(file);
    }
}
