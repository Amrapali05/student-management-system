package com.sms;

import com.sms.repository.StudentRepository;
import com.sms.service.StudentService;
import com.sms.ui.MainFrame;

import javax.swing.*;
import java.io.File;

/**
 * Main application entry point for the Student Management System.
 */
public class Main {

    public static void main(String[] args) {
        // High-DPI and smooth font rendering hints
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");
        System.setProperty("sun.java2d.dpiaware", "true");

        try {
            // Apply native platform appearance where appropriate
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            // Ensure data directory exists
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            StudentRepository repository = new StudentRepository("data");
            StudentService service = new StudentService(repository);

            MainFrame frame = new MainFrame(service);
            frame.setVisible(true);
        });
    }
}
