package com.sms.ui.views;

import com.sms.model.Student;
import com.sms.service.StudentService;
import com.sms.service.StudentStatistics;
import com.sms.ui.Theme;
import com.sms.ui.components.*;
import com.sms.ui.dialogs.StudentDetailDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Executive Dashboard View presenting high-level KPI cards, dual interactive
 * visual charts, and a preview table of top academic performers.
 */
public class DashboardView extends JPanel {

    private final Frame parentFrame;
    private final StudentService service;
    private final Runnable onViewDirectory;

    private StatCard totalCard;
    private StatCard activeCard;
    private StatCard gpaCard;
    private StatCard deptCard;

    private DepartmentPieChart departmentChart;
    private GpaBarChart gpaChart;

    private List<Student> previewStudents = new ArrayList<>();
    private PreviewTableModel tableModel;
    private ModernTable previewTable;

    public DashboardView(Frame parentFrame, StudentService service, Runnable onViewDirectory) {
        this.parentFrame = parentFrame;
        this.service = service;
        this.onViewDirectory = onViewDirectory;

        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
    }

    public void updateData(StudentStatistics stats, List<Student> allStudents) {
        if (stats != null) {
            totalCard.updateData(String.valueOf(stats.getTotalStudents()), "Enrolled Students");
            activeCard.updateData(stats.getActivePercentage() + "%", stats.getActiveStudents() + " Active");
            gpaCard.updateData(String.format(java.util.Locale.US, "%.2f", stats.getAverageGpa()),
                    "High: " + String.format(java.util.Locale.US, "%.2f", stats.getHighestGpa()));
            deptCard.updateData(stats.getTopDepartment(), "Largest Major");

            departmentChart.updateData(stats.getDepartmentCounts());
            gpaChart.updateData(stats.getGpaDistribution());
        }

        // Top 5 performers by GPA
        previewStudents.clear();
        if (allStudents != null) {
            List<Student> sorted = new ArrayList<>(allStudents);
            sorted.sort((s1, s2) -> Double.compare(s2.getGpa(), s1.getGpa()));
            int limit = Math.min(5, sorted.size());
            for (int i = 0; i < limit; i++) {
                previewStudents.add(sorted.get(i));
            }
        }
        tableModel.fireTableDataChanged();
    }

    private void initUI() {
        // --- 1. TOP METRICS ROW ---
        JPanel metricsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        metricsGrid.setOpaque(false);

        totalCard = new StatCard("TOTAL STUDENTS", "0", "Enrolled", "👥", Theme.ACCENT_PRIMARY);
        activeCard = new StatCard("ACTIVE STATUS", "0%", "Active Rate", "⚡", Theme.COLOR_SUCCESS);
        gpaCard = new StatCard("AVERAGE GPA", "0.00", "Out of 4.00", "⭐", Theme.COLOR_WARNING);
        deptCard = new StatCard("TOP DEPARTMENT", "-", "Largest Major", "🏛️", Theme.ACCENT_SECONDARY);

        metricsGrid.add(totalCard);
        metricsGrid.add(activeCard);
        metricsGrid.add(gpaCard);
        metricsGrid.add(deptCard);

        add(metricsGrid, BorderLayout.NORTH);

        // --- 2. CENTER SECTION: CHARTS + TOP PERFORMERS ---
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        centerGrid.setOpaque(false);

        // Charts Column (Left)
        JPanel chartsPanel = new JPanel(new GridLayout(2, 1, 0, 14));
        chartsPanel.setOpaque(false);

        departmentChart = new DepartmentPieChart();
        gpaChart = new GpaBarChart();

        chartsPanel.add(departmentChart);
        chartsPanel.add(gpaChart);

        centerGrid.add(chartsPanel);

        // Right Column: Top Performers Card
        JPanel rightCard = createTopPerformersCard();
        centerGrid.add(rightCard);

        add(centerGrid, BorderLayout.CENTER);
    }

    private JPanel createTopPerformersCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
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
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Header
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel titleLbl = new JLabel("🏆 Top Academic Performers");
        titleLbl.setFont(Theme.FONT_HEADER);
        titleLbl.setForeground(Theme.textPrimary());

        JLabel subtitleLbl = new JLabel("Highest cumulative GPA students in the institution");
        subtitleLbl.setFont(Theme.FONT_SMALL);
        subtitleLbl.setForeground(Theme.textSecondary());

        titleBlock.add(titleLbl);
        titleBlock.add(subtitleLbl);
        cardHeader.add(titleBlock, BorderLayout.WEST);

        ModernButton viewAllBtn = new ModernButton("View Full Directory ➔", ModernButton.ButtonVariant.OUTLINE);
        viewAllBtn.addActionListener(e -> {
            if (onViewDirectory != null) onViewDirectory.run();
        });
        cardHeader.add(viewAllBtn, BorderLayout.EAST);

        card.add(cardHeader, BorderLayout.NORTH);

        // Table
        tableModel = new PreviewTableModel();
        previewTable = new ModernTable(tableModel);
        previewTable.getColumnModel().getColumn(3).setCellRenderer(new GpaBadgeRenderer());
        previewTable.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeRenderer());

        previewTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        previewTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        previewTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        previewTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        previewTable.getColumnModel().getColumn(4).setPreferredWidth(95);

        previewTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && previewTable.getSelectedRow() != -1) {
                    int row = previewTable.getSelectedRow();
                    if (row >= 0 && row < previewStudents.size()) {
                        Student s = previewStudents.get(row);
                        StudentDetailDialog dialog = new StudentDetailDialog(parentFrame, service, s);
                        dialog.setVisible(true);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(previewTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Theme.bgCard());

        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private class PreviewTableModel extends AbstractTableModel {
        private final String[] cols = {"ID", "Name", "Department", "GPA", "Status"};

        @Override public int getRowCount() { return previewStudents.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int col) { return cols[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Student s = previewStudents.get(row);
            switch (col) {
                case 0: return s.getId();
                case 1: return s.getFullName();
                case 2: return s.getDepartment().getCode();
                case 3: return s.getGpa();
                case 4: return s.getStatus();
                default: return "";
            }
        }
    }
}
