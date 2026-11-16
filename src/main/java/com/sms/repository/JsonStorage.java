package com.sms.repository;

import com.sms.model.Student;
import com.sms.repository.storage.JsonStorageEngine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Backward compatibility facade delegating to {@link JsonStorageEngine}.
 */
public class JsonStorage {

    public static void saveToFile(Path path, List<Student> students) throws IOException {
        JsonStorageEngine.saveToFile(path, students);
    }

    public static List<Student> loadFromFile(Path path) throws IOException {
        return JsonStorageEngine.loadFromFile(path);
    }

    public static String serialize(List<Student> students) {
        return JsonStorageEngine.serialize(students);
    }

    public static List<Student> deserialize(String json) {
        return JsonStorageEngine.deserialize(json);
    }
}
