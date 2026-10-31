package com.sms.ui;

import com.sms.model.Department;
import com.sms.model.Student;
import com.sms.model.StudentStatus;
import com.sms.service.StudentService;
import com.sms.service.StudentStatistics;
import com.sms.ui.components.*;
import com.sms.ui.dialogs.StudentDetailDialog;
import com.sms.ui.dialogs.StudentFormDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application window combining real-time metrics, interactive data table,
 * analytics charts, and comprehensive student administration actions.
 */
public class MainFrame extends JFrame {

    private final StudentService service;

    // State
    private List<Student> currentStudents = new ArrayList<>();
    private StudentTableModel tableModel;
    private ModernTable table;

    // Metric Cards
    private StatCard totalCard;
    private StatCard activeCard;
    private StatCard gpaCard;
    private StatCard deptCard;

    // Charts
    private DepartmentPieChart departmentChart;
    private GpaBarChart gpaChart;

    // Filter controls
    private ModernTextField searchField;
    private JComboBox<String> deptFilterCombo;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> yearFilterCombo;
    private JLabel statusLabel;
    private JLabel countLabel;
    private ModernButton themeToggleBtn;

    public MainFrame(StudentService service) {
        this.service = service;

        setTitle("EduCore - Modern Student Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1320, 850);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        initUI();
        refreshAll();

        Theme.addListener(isDark -> {
            applyTheme();
        });
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout(0, 0));
        rootPanel.setBackground(Theme.bgMain());
        setContentPane(rootPanel);

        // Top Header
        rootPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Center Content Body
        JPanel bodyPanel = new JPanel(new BorderLayout(0, 16));
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(new EmptyBorder(16, 24, 16, 24));

        // Metric Cards Section
        bodyPanel.add(createMetricCardsPanel(), BorderLayout.NORTH);

        // Main Center Workspace (Table + Filter Bar + Side Analytics)
        bodyPanel.add(createWorkspacePanel(), BorderLayout.CENTER);

        rootPanel.add(bodyPanel, BorderLayout.CENTER);

        // Bottom Status Bar
        rootPanel.add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.bgHeader());
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.border()),
                new EmptyBorder(14, 24, 14, 24)
        ));

        // Brand & Title
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandPanel.setOpaque(false);

        JLabel logoLabel = new JLabel("🎓");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JPanel titleTextPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        titleTextPanel.setOpaque(false);

        JLabel titleLbl = new JLabel("EduCore Student Portal");
        titleLbl.setFont(Theme.FONT_TITLE);
        titleLbl.setForeground(Theme.textPrimary());

        JLabel subtitleLbl = new JLabel("Academic Administration & Performance Analytics");
        subtitleLbl.setFont(Theme.FONT_SMALL);
        subtitleLbl.setForeground(Theme.textSecondary());

        titleTextPanel.add(titleLbl);
        titleTextPanel.add(subtitleLbl);

        brandPanel.add(logoLabel);
        brandPanel.add(titleTextPanel);
        header.add(brandPanel, BorderLayout.WEST);

        // Header Actions
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerActions.setOpaque(false);

        ModernButton resetBtn = new ModernButton("Reset Sample Data", ModernButton.ButtonStyle.OUTLINE);
        resetBtn.addActionListener(e -> onResetData());

        themeToggleBtn = new ModernButton(Theme.isDark() ? "☀️ Light Mode" : "🌙 Dark Mode", ModernButton.ButtonStyle.OUTLINE);
        themeToggleBtn.addActionListener(e -> {
            Theme.toggleTheme();
            themeToggleBtn.setText(Theme.isDark() ? "☀️ Light Mode" : "🌙 Dark Mode");
        });

        headerActions.add(resetBtn);
        headerActions.add(themeToggleBtn);
        header.add(headerActions, BorderLayout.EAST);

        return header;
    }

    private JPanel createMetricCardsPanel() {
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsGrid.setOpaque(false);

        totalCard = new StatCard("TOTAL STUDENTS", "0", "Enrolled", "👥", Theme.ACCENT_PRIMARY);
        activeCard = new StatCard("ACTIVE STATUS", "0%", "Active Rate", "⚡", Theme.COLOR_SUCCESS);
        gpaCard = new StatCard("AVERAGE GPA", "0.00", "Out of 4.00", "⭐", Theme.COLOR_WARNING);
        deptCard = new StatCard("TOP DEPARTMENT", "-", "Highest Enrollment", "🏛️", Theme.ACCENT_SECONDARY);

        cardsGrid.add(totalCard);
        cardsGrid.add(activeCard);
        cardsGrid.add(gpaCard);
        cardsGrid.add(deptCard);

        return cardsGrid;
    }

    private JPanel createWorkspacePanel() {
        JPanel workspace = new JPanel(new BorderLayout(16, 0));
        workspace.setOpaque(false);

        // Left Section: Table and Search/Filter Bar
        JPanel leftSection = new JPanel(new BorderLayout(0, 12));
        leftSection.setOpaque(false);

        leftSection.add(createFilterAndActionToolbar(), BorderLayout.NORTH);
        leftSection.add(createTableCard(), BorderLayout.CENTER);

        workspace.add(leftSection, BorderLayout.CENTER);

        // Right Section: Analytics Panel
        JPanel rightAnalytics = new JPanel(new GridLayout(2, 1, 0, 16));
        rightAnalytics.setOpaque(false);
        rightAnalytics.setPreferredSize(new Dimension(340, 0));

        departmentChart = new DepartmentPieChart();
        gpaChart = new GpaBarChart();

        rightAnalytics.add(departmentChart);
        rightAnalytics.add(gpaChart);

        workspace.add(rightAnalytics, BorderLayout.EAST);

        return workspace;
    }

    private JPanel createFilterAndActionToolbar() {
        JPanel bar = new JPanel(new BorderLayout(12, 10));
        bar.setOpaque(false);

        // Search & Filters on Left/Center
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterRow.setOpaque(false);

        searchField = new ModernTextField("Search by name, ID, email...");
        searchField.setPreferredSize(new Dimension(240, 36));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterStudents(); }
            @Override public void removeUpdate(DocumentEvent e) { filterStudents(); }
            @Override public void changedUpdate(DocumentEvent e) { filterStudents(); }
        });
        filterRow.add(searchField);

        // Department combo
        List<String> deptOptions = new ArrayList<>();
        deptOptions.add("All Departments");
        for (Department d : Department.values()) {
            deptOptions.add(d.getFullName());
        }
        deptFilterCombo = createToolbarCombo(deptOptions.toArray(new String[0]));
        deptFilterCombo.addActionListener(e -> filterStudents());
        filterRow.add(deptFilterCombo);

        // Status combo
        List<String> statusOptions = new ArrayList<>();
        statusOptions.add("All Statuses");
        for (StudentStatus st : StudentStatus.values()) {
            statusOptions.add(st.getDisplayName());
        }
        statusFilterCombo = createToolbarCombo(statusOptions.toArray(new String[0]));
        statusFilterCombo.addActionListener(e -> filterStudents());
        filterRow.add(statusFilterCombo);

        // Year combo
        yearFilterCombo = createToolbarCombo(new String[]{"All Years", "Year 1", "Year 2", "Year 3", "Year 4"});
        yearFilterCombo.addActionListener(e -> filterStudents());
        filterRow.add(yearFilterCombo);

        // Clear Filters button
        ModernButton clearBtn = new ModernButton("Reset Filters", ModernButton.ButtonStyle.GHOST);
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            deptFilterCombo.setSelectedIndex(0);
            statusFilterCombo.setSelectedIndex(0);
            yearFilterCombo.setSelectedIndex(0);
            filterStudents();
        });
        filterRow.add(clearBtn);

        bar.add(filterRow, BorderLayout.WEST);

        // Action Buttons on Right
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionRow.setOpaque(false);

        ModernButton importBtn = new ModernButton("Import CSV", ModernButton.ButtonStyle.OUTLINE);
        importBtn.addActionListener(e -> onImportCsv());

        ModernButton exportBtn = new ModernButton("Export CSV", ModernButton.ButtonStyle.OUTLINE);
        exportBtn.addActionListener(e -> onExportCsv());

        ModernButton addBtn = new ModernButton("+ Add Student", ModernButton.ButtonStyle.PRIMARY);
        addBtn.addActionListener(e -> onAddStudent());

        actionRow.add(importBtn);
        actionRow.add(exportBtn);
        actionRow.add(addBtn);

        bar.add(actionRow, BorderLayout.EAST);

        return bar;
    }

    private <T> JComboBox<T> createToolbarCombo(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(Theme.FONT_REGULAR);
        combo.setPreferredSize(new Dimension(150, 36));
        combo.setBackground(Theme.inputBg());
        combo.setForeground(Theme.textPrimary());
        combo.setBorder(BorderFactory.createLineBorder(Theme.border(), 1));
        return combo;
    }

    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(Theme.bgCard());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.border(), 1),
                new EmptyBorder(0, 0, 0, 0)
        ));

        tableModel = new StudentTableModel();
        table = new ModernTable(tableModel);

        // Assign Custom Renderers
        table.getColumnModel().getColumn(4).setCellRenderer(new GpaBadgeRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(90);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(160); // Name
        table.getColumnModel().getColumn(2).setPreferredWidth(170); // Dept
        table.getColumnModel().getColumn(3).setPreferredWidth(75);  // Year
        table.getColumnModel().getColumn(4).setPreferredWidth(90);  // GPA
        table.getColumnModel().getColumn(5).setPreferredWidth(110); // Status
        table.getColumnModel().getColumn(6).setPreferredWidth(210); // Email

        // Double click on row opens detail modal
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    onViewSelectedStudent();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Theme.bgCard());

        card.add(scrollPane, BorderLayout.CENTER);

        // Table Bottom Action Bar
        JPanel tableFooter = new JPanel(new BorderLayout());
        tableFooter.setBackground(Theme.bgCardAlt());
        tableFooter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.border()),
                new EmptyBorder(8, 16, 8, 16)
        ));

        countLabel = new JLabel("Showing 0 students");
        countLabel.setFont(Theme.FONT_SMALL);
        countLabel.setForeground(Theme.textSecondary());
        tableFooter.add(countLabel, BorderLayout.WEST);

        JPanel rowActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rowActions.setOpaque(false);

        ModernButton viewBtn = new ModernButton("View Profile", ModernButton.ButtonStyle.OUTLINE);
        viewBtn.addActionListener(e -> onViewSelectedStudent());

        ModernButton editBtn = new ModernButton("Edit", ModernButton.ButtonStyle.OUTLINE);
        editBtn.addActionListener(e -> onEditSelectedStudent());

        ModernButton deleteBtn = new ModernButton("Delete", ModernButton.ButtonStyle.DANGER);
        deleteBtn.addActionListener(e -> onDeleteSelectedStudent());

        rowActions.add(viewBtn);
        rowActions.add(editBtn);
        rowActions.add(deleteBtn);
        tableFooter.add(rowActions, BorderLayout.EAST);

        card.add(tableFooter, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Theme.bgHeader());
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.border()),
                new EmptyBorder(6, 24, 6, 24)
        ));

        statusLabel = new JLabel("● Persistence active: data/students.json");
        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setForeground(Theme.COLOR_SUCCESS);

        JLabel versionLabel = new JLabel("EduCore v1.0.0 • Java 21 LTS");
        versionLabel.setFont(Theme.FONT_SMALL);
        versionLabel.setForeground(Theme.textMuted());

        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(versionLabel, BorderLayout.EAST);

        return statusBar;
    }

    private void filterStudents() {
        String query = searchField.getText();
        
        Department deptFilter = null;
        int deptIdx = deptFilterCombo.getSelectedIndex();
        if (deptIdx > 0 && deptIdx <= Department.values().length) {
            deptFilter = Department.values()[deptIdx - 1];
        }

        StudentStatus statusFilter = null;
        int statusIdx = statusFilterCombo.getSelectedIndex();
        if (statusIdx > 0 && statusIdx <= StudentStatus.values().length) {
            statusFilter = StudentStatus.values()[statusIdx - 1];
        }

        Integer yearFilter = null;
        int yearIdx = yearFilterCombo.getSelectedIndex();
        if (yearIdx > 0) {
            yearFilter = yearIdx;
        }

        currentStudents = service.filterStudents(query, deptFilter, statusFilter, yearFilter, "id", true);
        tableModel.fireTableDataChanged();

        int total = service.getAllStudents().size();
        countLabel.setText("Showing " + currentStudents.size() + " of " + total + " students");
    }

    private void refreshAll() {
        filterStudents();

        StudentStatistics stats = service.calculateStatistics();
        totalCard.updateData(String.valueOf(stats.getTotalStudents()), "Enrolled Students");
        activeCard.updateData(stats.getActivePercentage() + "%", stats.getActiveStudents() + " Active");
        gpaCard.updateData(String.format(java.util.Locale.US, "%.2f", stats.getAverageGpa()), 
                "High: " + String.format(java.util.Locale.US, "%.2f", stats.getHighestGpa()));
        deptCard.updateData(stats.getTopDepartment(), "Largest Major");

        departmentChart.updateData(stats.getDepartmentCounts());
        gpaChart.updateData(stats.getGpaDistribution());
    }

    private Student getSelectedStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1 || selectedRow >= currentStudents.size()) {
            JOptionPane.showMessageDialog(this, "Please select a student record from the table first.",
                    "No Student Selected", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return currentStudents.get(selectedRow);
    }

    private void onViewSelectedStudent() {
        Student s = getSelectedStudent();
        if (s == null) return;

        StudentDetailDialog dialog = new StudentDetailDialog(this, service, s);
        dialog.setVisible(true);

        if (dialog.getOutcome() == StudentDetailDialog.ActionOutcome.DELETED) {
            refreshAll();
        } else if (dialog.getOutcome() == StudentDetailDialog.ActionOutcome.EDIT) {
            openEditDialog(s);
        }
    }

    private void onEditSelectedStudent() {
        Student s = getSelectedStudent();
        if (s == null) return;
        openEditDialog(s);
    }

    private void openEditDialog(Student s) {
        StudentFormDialog dialog = new StudentFormDialog(this, service, s);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshAll();
            statusLabel.setText("● Updated student " + s.getId() + " successfully.");
        }
    }

    private void onAddStudent() {
        StudentFormDialog dialog = new StudentFormDialog(this, service, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshAll();
            statusLabel.setText("● New student registered successfully.");
        }
    }

    private void onDeleteSelectedStudent() {
        Student s = getSelectedStudent();
        if (s == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete student " + s.getFullName() + " (" + s.getId() + ")?",
                "Confirm Student Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.deleteStudent(s.getId());
                refreshAll();
                statusLabel.setText("● Student " + s.getId() + " deleted.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onExportCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Students to CSV");
        fileChooser.setSelectedFile(new File("students_export.csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }
            try {
                service.exportStudentsToCsv(fileToSave, currentStudents);
                JOptionPane.showMessageDialog(this,
                        "Successfully exported " + currentStudents.size() + " student records to:\n" + fileToSave.getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                statusLabel.setText("● Exported records to " + fileToSave.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onImportCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Students from CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();
            try {
                int count = service.importStudentsFromCsv(fileToOpen);
                refreshAll();
                JOptionPane.showMessageDialog(this,
                        "Successfully imported " + count + " new/updated student records.",
                        "Import Complete", JOptionPane.INFORMATION_MESSAGE);
                statusLabel.setText("● Imported " + count + " records from " + fileToOpen.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to import CSV: " + ex.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onResetData() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Reset all records to default sample data? Any newly added records will be replaced.",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.resetToDefaultData();
                refreshAll();
                statusLabel.setText("● System reset to default sample dataset.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Reset failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void applyTheme() {
        getContentPane().setBackground(Theme.bgMain());
        SwingUtilities.updateComponentTreeUI(this);
        repaint();
    }

    /**
     * Inner Table Model
     */
    private class StudentTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Name", "Department", "Year", "GPA", "Status", "Email"};

        @Override public int getRowCount() { return currentStudents.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int column) { return columns[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Student s = currentStudents.get(rowIndex);
            switch (columnIndex) {
                case 0: return s.getId();
                case 1: return s.getFullName();
                case 2: return s.getDepartment().getFullName();
                case 3: return "Yr " + s.getAcademicYear();
                case 4: return s.getGpa();
                case 5: return s.getStatus();
                case 6: return s.getEmail();
                default: return "";
            }
        }
    }
}
