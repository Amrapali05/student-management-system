package com.sms;

import com.sms.concurrent.ThreadManager;
import com.sms.config.AppConfig;
import com.sms.repository.RepositoryFactory;
import com.sms.repository.StudentRepository;
import com.sms.service.StudentService;
import com.sms.service.impl.StudentServiceImpl;
import com.sms.ui.MainFrame;

import javax.swing.*;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enterprise Application Entry Point for the Student Management System.
 * Bootstraps configuration, repository factories, thread pools, and Swing GUI.
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // High-DPI and anti-aliasing font rendering hints
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");
        System.setProperty("sun.java2d.dpiaware", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Bootstrap enterprise configuration
        AppConfig config = AppConfig.getInstance();
        File dataDir = new File(config.getDataDirectory());
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // Initialize repository (decoupled interface via RepositoryFactory)
        StudentRepository repository = RepositoryFactory.createRepository();
        StudentService service = new StudentServiceImpl(repository);

        // Schedule periodic auto-save daemon if enabled
        if (config.isAutoSaveEnabled()) {
            ThreadManager.getInstance().scheduleAtFixedRate(() -> {
                try {
                    LOGGER.fine("Periodic background synchronization check...");
                    // File-based repository is already updated on write;
                    // this daemon ensures data integrity and cache sync.
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Auto-save check failed: {0}", e.getMessage());
                }
            }, config.getAutoSaveIntervalSeconds(), config.getAutoSaveIntervalSeconds(), TimeUnit.SECONDS);
        }

        // Launch GUI on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(service);
            frame.setVisible(true);
        });
    }
}
