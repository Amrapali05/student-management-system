package com.sms.ui.views;

import com.sms.concurrent.ThreadManager;
import com.sms.exception.StorageException;
import com.sms.exception.StudentNotFoundException;
import com.sms.model.Department;
import com.sms.model.Student;
import com.sms.model.StudentStatus;
import com.sms.service.StudentService;
import com.sms.ui.Theme;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Full Student Directory View providing advanced live search, multi-criteria filtering,
 * paginated browsing, and complete CRUD administration.
 */
public class DirectoryView extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(DirectoryView.class.getName());

    private final Frame parentFrame;
    private final StudentService service;
    private final Runnable onDataChanged;

    // Data State
    private List<Student> filteredStudents = new ArrayList<>();
    private List<Student> pageStudents = new ArrayList<>();
    private StudentTableModel tableModel;
    private ModernTable table;

    // Pagination
    private int currentPage = 1;
    private int pageSize = 10;
    private int totalPages = 1;

    // Filter controls
    private ModernTextField searchField;
    private JComboBox<String> deptFilterCombo;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> yearFilterCombo;
    private JLabel countLabel;
    private JLabel pageInfoLabel;
    private ModernButton prevPageBtn;
    private ModernButton nextPageBtn;
    private JComboBox<String> pageSizeCombo;

    private final AtomicInteger searchGeneration = new AtomicInteger(0);

    public DirectoryView(Frame parentFrame, StudentService service, Runnable onDataChanged) {
        this.parentFrame = parentFrame;
        this.service = service;
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(new EmptyBorder(16, 24, 16, 24));

        initUI();
    }

    public void setSearchQuery(String query) {
        searchField.setText(query);
    }

    public void refreshTableAsync() {
        filterStudentsAsync();
    }

    private void initUI() {
        // --- 1. TOP TOOLBAR ---
        add(createFilterAndActionToolbar(), BorderLayout.NORTH);

        // --- 2. CENTER TABLE CARD ---
        add(createTableCard(), BorderLayout.CENTER);
    }

    private JPanel createFilterAndActionToolbar() {
        JPanel bar = new JPanel(new BorderLayout(12, 10));
        bar.setOpaque(false);

        // Left/Center Filters
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterRow.setOpaque(false);

        searchField = new ModernTextField("Search by name, ID, email...");
        searchField.setPreferredSize(new Dimension(220, 36));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterStudentsAsync(); }
            @Override public void removeUpdate(DocumentEvent e) { filterStudentsAsync(); }
            @Override public void changedUpdate(DocumentEvent e) { filterStudentsAsync(); }
        });
        filterRow.add(searchField);

        // Department combo
        List<String> deptOptions = new ArrayList<>();
        deptOptions.add("All Departments");
        for (Department d : Department.values()) {
            deptOptions.add(d.getFullName());
        }
        deptFilterCombo = createToolbarCombo(deptOptions.toArray(new String[0]));
        deptFilterCombo.addActionListener(e -> filterStudentsAsync());
        filterRow.add(deptFilterCombo);

        // Status combo
        List<String> statusOptions = new ArrayList<>();
        statusOptions.add("All Statuses");
        for (StudentStatus st : StudentStatus.values()) {
            statusOptions.add(st.getLabel());
        }
        statusFilterCombo = createToolbarCombo(statusOptions.toArray(new String[0]));
        statusFilterCombo.addActionListener(e -> filterStudentsAsync());
        filterRow.add(statusFilterCombo);

        // Year combo
        yearFilterCombo = createToolbarCombo(new String[]{"All Years", "Year 1", "Year 2", "Year 3", "Year 4"});
        yearFilterCombo.addActionListener(e -> filterStudentsAsync());
        filterRow.add(yearFilterCombo);

        // Reset Filters Button
        ModernButton clearBtn = new ModernButton("Reset Filters", ModernButton.ButtonVariant.OUTLINE);
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            deptFilterCombo.setSelectedIndex(0);
            statusFilterCombo.setSelectedIndex(0);
            yearFilterCombo.setSelectedIndex(0);
            filterStudentsAsync();
        });
        filterRow.add(clearBtn);

        bar.add(filterRow, BorderLayout.WEST);

        // Right Action Buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionRow.setOpaque(false);

        ModernButton importBtn = new ModernButton("Import CSV", ModernButton.ButtonVariant.OUTLINE);
        importBtn.addActionListener(e -> onImportCsv());

        ModernButton exportBtn = new ModernButton("Export CSV", ModernButton.ButtonVariant.OUTLINE);
        exportBtn.addActionListener(e -> onExportCsv());

        ModernButton addBtn = new ModernButton("+ Add Student", ModernButton.ButtonVariant.PRIMARY);
        addBtn.addActionListener(e -> onAddStudent());

        actionRow.add(importBtn);
        actionRow.add(exportBtn);
        actionRow.add(addBtn);

        bar.add(actionRow, BorderLayout.EAST);

        return bar;
    }

    private <T> JComboBox<T> createToolbarCombo(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(Theme.FONT_BODY);
        combo.setPreferredSize(new Dimension(145, 36));
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

        table.getColumnModel().getColumn(4).setCellRenderer(new GpaBadgeRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(90);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(170); // Name
        table.getColumnModel().getColumn(2).setPreferredWidth(170); // Dept
        table.getColumnModel().getColumn(3).setPreferredWidth(75);  // Year
        table.getColumnModel().getColumn(4).setPreferredWidth(95);  // GPA
        table.getColumnModel().getColumn(5).setPreferredWidth(110); // Status
        table.getColumnModel().getColumn(6).setPreferredWidth(210); // Email

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

        // --- FOOTER & PAGINATION BAR ---
        JPanel tableFooter = new JPanel(new BorderLayout(16, 0));
        tableFooter.setBackground(Theme.bgCardAlt());
        tableFooter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.border()),
                new EmptyBorder(8, 16, 8, 16)
        ));

        // Left info
        countLabel = new JLabel("Showing 0 students");
        countLabel.setFont(Theme.FONT_SMALL);
        countLabel.setForeground(Theme.textSecondary());
        tableFooter.add(countLabel, BorderLayout.WEST);

        // Center: Pagination Controls
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        paginationPanel.setOpaque(false);

        prevPageBtn = new ModernButton("◀ Prev", ModernButton.ButtonVariant.OUTLINE);
        prevPageBtn.setPreferredSize(new Dimension(80, 32));
        prevPageBtn.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                updatePageSlice();
            }
        });

        pageInfoLabel = new JLabel("Page 1 of 1");
        pageInfoLabel.setFont(Theme.FONT_BOLD);
        pageInfoLabel.setForeground(Theme.textPrimary());

        nextPageBtn = new ModernButton("Next ▶", ModernButton.ButtonVariant.OUTLINE);
        nextPageBtn.setPreferredSize(new Dimension(80, 32));
        nextPageBtn.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                updatePageSlice();
            }
        });

        pageSizeCombo = new JComboBox<>(new String[]{"10 / page", "25 / page", "50 / page", "All"});
        pageSizeCombo.setFont(Theme.FONT_SMALL);
        pageSizeCombo.setPreferredSize(new Dimension(95, 32));
        pageSizeCombo.addActionListener(e -> {
            int idx = pageSizeCombo.getSelectedIndex();
            switch (idx) {
                case 1: pageSize = 25; break;
                case 2: pageSize = 50; break;
                case 3: pageSize = Integer.MAX_VALUE; break;
                case 0: default: pageSize = 10; break;
            }
            currentPage = 1;
            updatePageSlice();
        });

        paginationPanel.add(prevPageBtn);
        paginationPanel.add(pageInfoLabel);
        paginationPanel.add(nextPageBtn);
        paginationPanel.add(Box.createHorizontalStrut(8));
        paginationPanel.add(pageSizeCombo);

        tableFooter.add(paginationPanel, BorderLayout.CENTER);

        // Right: Row Actions
        JPanel rowActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rowActions.setOpaque(false);

        ModernButton viewBtn = new ModernButton("View Profile", ModernButton.ButtonVariant.OUTLINE);
        viewBtn.addActionListener(e -> onViewSelectedStudent());

        ModernButton editBtn = new ModernButton("Edit", ModernButton.ButtonVariant.OUTLINE);
        editBtn.addActionListener(e -> onEditSelectedStudent());

        ModernButton deleteBtn = new ModernButton("Delete", ModernButton.ButtonVariant.DANGER);
        deleteBtn.addActionListener(e -> onDeleteSelectedStudent());

        rowActions.add(viewBtn);
        rowActions.add(editBtn);
        rowActions.add(deleteBtn);
        tableFooter.add(rowActions, BorderLayout.EAST);

        card.add(tableFooter, BorderLayout.SOUTH);

        return card;
    }

    private void filterStudentsAsync() {
        final int gen = searchGeneration.incrementAndGet();
        final String query = searchField.getText();

        Department dept = null;
        int deptIdx = deptFilterCombo.getSelectedIndex();
        if (deptIdx > 0 && deptIdx <= Department.values().length) {
            dept = Department.values()[deptIdx - 1];
        }

        StudentStatus status = null;
        int statusIdx = statusFilterCombo.getSelectedIndex();
        if (statusIdx > 0 && statusIdx <= StudentStatus.values().length) {
            status = StudentStatus.values()[statusIdx - 1];
        }

        Integer year = null;
        int yearIdx = yearFilterCombo.getSelectedIndex();
        if (yearIdx > 0) {
            year = yearIdx;
        }

        final Department finalDept = dept;
        final StudentStatus finalStatus = status;
        final Integer finalYear = year;

        service.filterStudentsAsync(query, finalDept, finalStatus, finalYear, "id", true)
                .thenAccept(results -> SwingUtilities.invokeLater(() -> {
                    if (gen == searchGeneration.get()) {
                        filteredStudents = results;
                        currentPage = 1;
                        updatePageSlice();
                    }
                }))
                .exceptionally(ex -> {
                    LOGGER.log(Level.WARNING, "Async directory filter failed: {0}", ex.getMessage());
                    return null;
                });
    }

    private void updatePageSlice() {
        int total = filteredStudents.size();
        if (pageSize == Integer.MAX_VALUE || total == 0) {
            totalPages = 1;
            currentPage = 1;
            pageStudents = new ArrayList<>(filteredStudents);
        } else {
            totalPages = (int) Math.ceil((double) total / pageSize);
            if (totalPages < 1) totalPages = 1;
            if (currentPage > totalPages) currentPage = totalPages;

            int from = (currentPage - 1) * pageSize;
            int to = Math.min(from + pageSize, total);
            pageStudents = new ArrayList<>(filteredStudents.subList(from, to));
        }

        tableModel.fireTableDataChanged();

        int allCount = service.getAllStudents().size();
        countLabel.setText("Showing " + pageStudents.size() + " of " + total + " matches (Total: " + allCount + ")");
        pageInfoLabel.setText("Page " + currentPage + " of " + totalPages);
        prevPageBtn.setEnabled(currentPage > 1);
        nextPageBtn.setEnabled(currentPage < totalPages);
    }

    private Student getSelectedStudent() {
        int row = table.getSelectedRow();
        if (row == -1 || row >= pageStudents.size()) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table first.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return pageStudents.get(row);
    }

    private void onViewSelectedStudent() {
        Student s = getSelectedStudent();
        if (s == null) return;
        StudentDetailDialog dialog = new StudentDetailDialog(parentFrame, service, s);
        dialog.setVisible(true);
        if (dialog.getOutcome() == StudentDetailDialog.ActionOutcome.DELETED) {
            filterStudentsAsync();
            if (onDataChanged != null) onDataChanged.run();
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
        StudentFormDialog dialog = new StudentFormDialog(parentFrame, service, s);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            filterStudentsAsync();
            if (onDataChanged != null) onDataChanged.run();
        }
    }

    public void onAddStudent() {
        StudentFormDialog dialog = new StudentFormDialog(parentFrame, service, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            filterStudentsAsync();
            if (onDataChanged != null) onDataChanged.run();
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
            ThreadManager.getInstance().runAsync(() -> {
                try {
                    service.deleteStudent(s.getId());
                    SwingUtilities.invokeLater(() -> {
                        filterStudentsAsync();
                        if (onDataChanged != null) onDataChanged.run();
                    });
                } catch (StudentNotFoundException | StorageException ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Failed to delete: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
                }
            });
        }
    }

    private void onExportCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Students to CSV");
        fileChooser.setSelectedFile(new File("students_export.csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int res = fileChooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = fileChooser.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".csv")) {
                target = new File(target.getAbsolutePath() + ".csv");
            }
            final File finalTarget = target;
            final List<Student> snapshot = new ArrayList<>(filteredStudents);

            ThreadManager.getInstance().runAsync(() -> {
                try {
                    service.exportStudentsToCsv(finalTarget, snapshot);
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Successfully exported " + snapshot.size() + " records to:\n" + finalTarget.getAbsolutePath(),
                            "Export Completed", JOptionPane.INFORMATION_MESSAGE));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Export failed: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE));
                }
            });
        }
    }

    private void onImportCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Students from CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int res = fileChooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            ThreadManager.getInstance().runAsync(() -> {
                try {
                    int count = service.importStudentsFromCsv(file);
                    SwingUtilities.invokeLater(() -> {
                        filterStudentsAsync();
                        if (onDataChanged != null) onDataChanged.run();
                        JOptionPane.showMessageDialog(this,
                                "Successfully imported " + count + " new/updated student records.",
                                "Import Complete", JOptionPane.INFORMATION_MESSAGE);
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Import failed: " + ex.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE));
                }
            });
        }
    }

    private class StudentTableModel extends AbstractTableModel {
        private final String[] cols = {"ID", "Name", "Department", "Year", "GPA", "Status", "Email"};

        @Override public int getRowCount() { return pageStudents.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int col) { return cols[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Student s = pageStudents.get(row);
            switch (col) {
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
