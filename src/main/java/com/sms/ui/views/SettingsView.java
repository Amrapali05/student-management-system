package com.sms.ui.views;

import com.sms.concurrent.ThreadManager;
import com.sms.config.AppConfig;
import com.sms.exception.StorageException;
import com.sms.repository.impl.DatabaseConnectionFactory;
import com.sms.repository.storage.JsonStorageEngine;
import com.sms.service.StudentService;
import com.sms.ui.Theme;
import com.sms.ui.components.ModernButton;
import com.sms.ui.components.ModernTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;

/**
 * Enterprise Settings and Database Integration Hub.
 * Provides live storage switching, JDBC connection diagnostics, schema initialization,
 * disaster recovery backups, and batch CSV management.
 */
public class SettingsView extends JPanel {

    private final Frame parentFrame;
    private final StudentService service;
    private final Runnable onDataChanged;

    private JRadioButton fileStorageRadio;
    private JRadioButton dbStorageRadio;
    private ModernTextField jdbcUrlField;
    private JLabel statusFeedbackLabel;

    public SettingsView(Frame parentFrame, StudentService service, Runnable onDataChanged) {
        this.parentFrame = parentFrame;
        this.service = service;
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
    }

    private void initUI() {
        // Scrollable content wrapper
        JPanel container = new JPanel(new GridLayout(2, 2, 16, 16));
        container.setOpaque(false);

        // Card 1: Storage Engine Selection
        container.add(createStorageEngineCard());

        // Card 2: Relational Database Integration
        container.add(createDatabaseConsoleCard());

        // Card 3: Disaster Recovery & Backups
        container.add(createBackupRecoveryCard());

        // Card 4: Data Portability & CSV Hub
        container.add(createDataHubCard());

        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Theme.bgMain());
        scrollPane.setOpaque(false);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createStorageEngineCard() {
        JPanel card = createStyledCard("💾 Persistence Storage Engine", "Configure active repository storage provider");

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        AppConfig config = AppConfig.getInstance();
        boolean isDb = config.getStorageType() == AppConfig.StorageType.DATABASE;

        fileStorageRadio = new JRadioButton("JSON File Persistence (Thread-safe Atomic Engine)", !isDb);
        dbStorageRadio = new JRadioButton("Relational Database (JDBC / SQLite / MySQL)", isDb);

        ButtonGroup group = new ButtonGroup();
        group.add(fileStorageRadio);
        group.add(dbStorageRadio);

        styleRadio(fileStorageRadio);
        styleRadio(dbStorageRadio);

        content.add(fileStorageRadio);
        content.add(Box.createVerticalStrut(4));
        content.add(createDetailLabel("  • Uses ReentrantReadWriteLock for high-throughput concurrency"));
        content.add(createDetailLabel("  • Writes to .tmp and executes atomic swap to prevent corruption"));
        content.add(Box.createVerticalStrut(12));

        content.add(dbStorageRadio);
        content.add(Box.createVerticalStrut(4));
        content.add(createDetailLabel("  • Connects via JDBC PreparedStatement with SQL-injection protection"));
        content.add(createDetailLabel("  • Compatible with SQLite, PostgreSQL, MySQL, and H2 databases"));

        content.add(Box.createVerticalStrut(16));
        ModernButton applyStorageBtn = new ModernButton("Apply Storage Mode", ModernButton.ButtonVariant.PRIMARY);
        applyStorageBtn.addActionListener(e -> {
            AppConfig.StorageType selected = dbStorageRadio.isSelected() ? 
                    AppConfig.StorageType.DATABASE : AppConfig.StorageType.FILE_JSON;
            config.setStorageType(selected);
            JOptionPane.showMessageDialog(parentFrame,
                    "Storage provider configured to: " + selected + 
                    "\nSettings saved. Restart or reconnect to activate live adapter.",
                    "Storage Mode Updated", JOptionPane.INFORMATION_MESSAGE);
        });
        content.add(applyStorageBtn);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDatabaseConsoleCard() {
        JPanel card = createStyledCard("🗄️ Relational Database Console", "Test live connection and initialize SQL schemas");

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        content.add(createFieldLabel("JDBC Connection URL"));
        content.add(Box.createVerticalStrut(4));

        AppConfig config = AppConfig.getInstance();
        jdbcUrlField = new ModernTextField("jdbc:sqlite:data/students.db");
        jdbcUrlField.setText(config.getDatabaseConfig().getJdbcUrl());
        jdbcUrlField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        content.add(jdbcUrlField);

        content.add(Box.createVerticalStrut(12));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);

        ModernButton testConnBtn = new ModernButton("Test Connection", ModernButton.ButtonVariant.OUTLINE);
        testConnBtn.addActionListener(e -> onTestDbConnection());

        ModernButton initSchemaBtn = new ModernButton("Initialize Schema", ModernButton.ButtonVariant.OUTLINE);
        initSchemaBtn.addActionListener(e -> onInitDbSchema());

        btnRow.add(testConnBtn);
        btnRow.add(initSchemaBtn);
        content.add(btnRow);

        content.add(Box.createVerticalStrut(12));
        statusFeedbackLabel = new JLabel("Status: Schema ready in data/schema.sql");
        statusFeedbackLabel.setFont(Theme.FONT_SMALL);
        statusFeedbackLabel.setForeground(Theme.textSecondary());
        content.add(statusFeedbackLabel);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createBackupRecoveryCard() {
        JPanel card = createStyledCard("🛡️ Disaster Recovery & Backups", "Automated backup retention (.bak) and data restore");

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        content.add(createDetailLabel("Primary File: data/students.json"));
        content.add(createDetailLabel("Backup File:  data/students.json.bak"));
        content.add(Box.createVerticalStrut(14));

        JPanel btnRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow1.setOpaque(false);

        ModernButton createBackupBtn = new ModernButton("Create Backup Now", ModernButton.ButtonVariant.OUTLINE);
        createBackupBtn.addActionListener(e -> onCreateBackup());

        ModernButton restoreBackupBtn = new ModernButton("Restore from Backup", ModernButton.ButtonVariant.OUTLINE);
        restoreBackupBtn.addActionListener(e -> onRestoreBackup());

        btnRow1.add(createBackupBtn);
        btnRow1.add(restoreBackupBtn);
        content.add(btnRow1);

        content.add(Box.createVerticalStrut(16));
        content.add(createDetailLabel("Restore Factory Preset Dataset:"));
        content.add(Box.createVerticalStrut(6));

        ModernButton resetBtn = new ModernButton("Reset to Default Sample Records", ModernButton.ButtonVariant.DANGER);
        resetBtn.addActionListener(e -> onResetData());
        content.add(resetBtn);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDataHubCard() {
        JPanel card = createStyledCard("📥 Data Portability & CSV Hub", "Batch export and import institutional student rosters");

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        content.add(createDetailLabel("• RFC-4180 quote escaping support for special characters and commas"));
        content.add(createDetailLabel("• Automatic schema matching and field validation during CSV import"));
        content.add(Box.createVerticalStrut(16));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);

        ModernButton exportCsvBtn = new ModernButton("Export All to CSV", ModernButton.ButtonVariant.PRIMARY);
        exportCsvBtn.addActionListener(e -> onExportCsvAll());

        ModernButton importCsvBtn = new ModernButton("Import CSV Roster", ModernButton.ButtonVariant.OUTLINE);
        importCsvBtn.addActionListener(e -> onImportCsv());

        btnRow.add(exportCsvBtn);
        btnRow.add(importCsvBtn);
        content.add(btnRow);

        content.add(Box.createVerticalStrut(16));
        content.add(createDetailLabel("Active Roster Count: " + service.getAllStudents().size() + " records"));

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createStyledCard(String title, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(0, 14)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.enableQualityRendering(g2);
                g2.setColor(Theme.bgCard());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(Theme.border());
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 14, 14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setOpaque(false);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.FONT_HEADER);
        titleLbl.setForeground(Theme.textPrimary());

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(Theme.FONT_SMALL);
        subLbl.setForeground(Theme.textSecondary());

        header.add(titleLbl);
        header.add(subLbl);
        card.add(header, BorderLayout.NORTH);

        return card;
    }

    private void styleRadio(JRadioButton radio) {
        radio.setOpaque(false);
        radio.setFont(Theme.FONT_BOLD);
        radio.setForeground(Theme.textPrimary());
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.textSecondary());
        return l;
    }

    private JLabel createDetailLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.textMuted());
        return l;
    }

    private void onTestDbConnection() {
        String url = jdbcUrlField.getText().trim();
        AppConfig.getInstance().getDatabaseConfig().setJdbcUrl(url);

        ThreadManager.getInstance().runAsync(() -> {
            try (Connection conn = DatabaseConnectionFactory.getConnection()) {
                boolean valid = conn != null && !conn.isClosed();
                SwingUtilities.invokeLater(() -> {
                    if (valid) {
                        statusFeedbackLabel.setText("✔ Connection successful to: " + url);
                        statusFeedbackLabel.setForeground(Theme.COLOR_SUCCESS);
                        JOptionPane.showMessageDialog(parentFrame,
                                "Successfully established JDBC database connection:\n" + url,
                                "Connection Successful", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        statusFeedbackLabel.setText("✘ Database connection failed.");
                        statusFeedbackLabel.setForeground(Theme.COLOR_DANGER);
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    statusFeedbackLabel.setText("✘ Error: " + ex.getMessage());
                    statusFeedbackLabel.setForeground(Theme.COLOR_DANGER);
                    JOptionPane.showMessageDialog(parentFrame,
                            "Database connection test failed:\n" + ex.getMessage(),
                            "Connection Failed", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    private void onInitDbSchema() {
        ThreadManager.getInstance().runAsync(() -> {
            try {
                DatabaseConnectionFactory.initializeSchema();
                SwingUtilities.invokeLater(() -> {
                    statusFeedbackLabel.setText("✔ Schema verified and initialized.");
                    statusFeedbackLabel.setForeground(Theme.COLOR_SUCCESS);
                    JOptionPane.showMessageDialog(parentFrame,
                            "Relational database schema and indexes initialized successfully.",
                            "Schema Initialized", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    statusFeedbackLabel.setText("✘ Schema init error: " + ex.getMessage());
                    statusFeedbackLabel.setForeground(Theme.COLOR_DANGER);
                    JOptionPane.showMessageDialog(parentFrame,
                            "Failed to initialize schema: " + ex.getMessage(),
                            "Schema Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    private void onCreateBackup() {
        Path target = Paths.get("data", "students.json");
        Path backup = Paths.get("data", "students.json.bak");
        try {
            if (Files.exists(target)) {
                Files.copy(target, backup, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(parentFrame,
                        "Backup created successfully at:\n" + backup.toAbsolutePath(),
                        "Backup Created", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(parentFrame,
                        "Primary data file does not exist yet.",
                        "No Data", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Failed to create backup: " + ex.getMessage(), "Backup Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRestoreBackup() {
        Path target = Paths.get("data", "students.json");
        Path backup = Paths.get("data", "students.json.bak");
        try {
            if (Files.exists(backup)) {
                Files.copy(backup, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                if (onDataChanged != null) onDataChanged.run();
                JOptionPane.showMessageDialog(parentFrame,
                        "Successfully restored database state from backup:\n" + backup.toAbsolutePath(),
                        "Restore Complete", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(parentFrame,
                        "No backup file (.bak) found in data directory.",
                        "Backup Not Found", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Failed to restore backup: " + ex.getMessage(), "Restore Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onResetData() {
        int confirm = JOptionPane.showConfirmDialog(
                parentFrame,
                "Reset all records to default sample data? Any newly added records will be replaced.",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            ThreadManager.getInstance().runAsync(() -> {
                try {
                    service.resetToDefaultData();
                    SwingUtilities.invokeLater(() -> {
                        if (onDataChanged != null) onDataChanged.run();
                        JOptionPane.showMessageDialog(parentFrame,
                                "Successfully reset student database to default sample records.",
                                "Reset Complete", JOptionPane.INFORMATION_MESSAGE);
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parentFrame,
                            "Reset failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
                }
            });
        }
    }

    private void onExportCsvAll() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export All Records to CSV");
        fileChooser.setSelectedFile(new File("students_roster_full.csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int res = fileChooser.showSaveDialog(parentFrame);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = fileChooser.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".csv")) {
                target = new File(target.getAbsolutePath() + ".csv");
            }
            final File finalTarget = target;
            ThreadManager.getInstance().runAsync(() -> {
                try {
                    service.exportStudentsToCsv(finalTarget, null);
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parentFrame,
                            "Successfully exported full student roster to:\n" + finalTarget.getAbsolutePath(),
                            "Export Completed", JOptionPane.INFORMATION_MESSAGE));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parentFrame,
                            "Export failed: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE));
                }
            });
        }
    }

    private void onImportCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Students from CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int res = fileChooser.showOpenDialog(parentFrame);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            ThreadManager.getInstance().runAsync(() -> {
                try {
                    int count = service.importStudentsFromCsv(file);
                    SwingUtilities.invokeLater(() -> {
                        if (onDataChanged != null) onDataChanged.run();
                        JOptionPane.showMessageDialog(parentFrame,
                                "Successfully imported " + count + " new/updated student records.",
                                "Import Complete", JOptionPane.INFORMATION_MESSAGE);
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parentFrame,
                            "Import failed: " + ex.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE));
                }
            });
        }
    }
}
