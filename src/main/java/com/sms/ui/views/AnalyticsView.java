package com.sms.ui.views;

import com.sms.model.Department;
import com.sms.model.StudentStatus;
import com.sms.service.StudentStatistics;
import com.sms.ui.Theme;
import com.sms.ui.components.DepartmentPieChart;
import com.sms.ui.components.GpaBarChart;
import com.sms.ui.components.StatCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Enterprise Analytics View presenting interactive visual distributions,
 * academic standing breakdowns, and department demographic analysis.
 */
public class AnalyticsView extends JPanel {

    private StatCard totalCard;
    private StatCard avgGpaCard;
    private StatCard honorRollCard;
    private StatCard probationCard;

    private DepartmentPieChart departmentChart;
    private GpaBarChart gpaChart;

    private List<DeptSummaryRow> deptRows = new ArrayList<>();
    private DeptSummaryTableModel deptTableModel;
    private JTable deptTable;

    public AnalyticsView() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
    }

    public void updateData(StudentStatistics stats) {
        if (stats == null) return;

        totalCard.updateData(String.valueOf(stats.getTotalStudents()), stats.getActiveStudents() + " Currently Active");
        avgGpaCard.updateData(String.format(java.util.Locale.US, "%.2f", stats.getAverageGpa()),
                "Highest: " + String.format(java.util.Locale.US, "%.2f", stats.getHighestGpa()));

        int honorCount = stats.getGpaDistribution().getOrDefault("3.5 - 4.0 (Honor)", 0);
        int probationCount = stats.getGpaDistribution().getOrDefault("< 2.0 (Probation)", 0);

        honorRollCard.updateData(String.valueOf(honorCount), "GPA ≥ 3.50 (Dean's List)");
        probationCard.updateData(String.valueOf(probationCount), "GPA < 2.00 (At Risk)");

        departmentChart.updateData(stats.getDepartmentCounts());
        gpaChart.updateData(stats.getGpaDistribution());

        // Update Dept Breakdown Table
        deptRows.clear();
        int total = Math.max(1, stats.getTotalStudents());
        for (Map.Entry<Department, Integer> entry : stats.getDepartmentCounts().entrySet()) {
            double pct = Math.round(((double) entry.getValue() / total * 100.0) * 10.0) / 10.0;
            deptRows.add(new DeptSummaryRow(entry.getKey().getFullName(), entry.getKey().getCode(), entry.getValue(), pct + "%"));
        }
        deptTableModel.fireTableDataChanged();
    }

    private void initUI() {
        // --- 1. TOP STAT CARDS ROW ---
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);

        totalCard = new StatCard("TOTAL ENROLLED", "0", "0 Active", "🎓", Theme.ACCENT_PRIMARY);
        avgGpaCard = new StatCard("INSTITUTIONAL GPA", "0.00", "High: 0.00", "📈", Theme.COLOR_SUCCESS);
        honorRollCard = new StatCard("HONOR ROLL", "0", "Dean's List Standing", "⭐", Theme.COLOR_WARNING);
        probationCard = new StatCard("ACADEMIC PROBATION", "0", "Requires Attention", "⚠️", Theme.COLOR_DANGER);

        statsRow.add(totalCard);
        statsRow.add(avgGpaCard);
        statsRow.add(honorRollCard);
        statsRow.add(probationCard);

        add(statsRow, BorderLayout.NORTH);

        // --- 2. CENTER CHARTS & BREAKDOWN GRID ---
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        centerGrid.setOpaque(false);

        // Left Column: Interactive Java 2D Charts
        JPanel chartsCol = new JPanel(new GridLayout(2, 1, 0, 14));
        chartsCol.setOpaque(false);

        departmentChart = new DepartmentPieChart();
        gpaChart = new GpaBarChart();

        chartsCol.add(departmentChart);
        chartsCol.add(gpaChart);

        centerGrid.add(chartsCol);

        // Right Column: Demographic & Performance Breakdown Card
        JPanel breakdownCard = createBreakdownCard();
        centerGrid.add(breakdownCard);

        add(centerGrid, BorderLayout.CENTER);
    }

    private JPanel createBreakdownCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
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
        JLabel title = new JLabel("🏛️ Department Enrollment Analysis");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.textPrimary());

        JLabel subtitle = new JLabel("Student density and proportion across institutional faculties");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.textSecondary());

        header.add(title);
        header.add(subtitle);
        card.add(header, BorderLayout.NORTH);

        deptTableModel = new DeptSummaryTableModel();
        deptTable = new JTable(deptTableModel);
        deptTable.setRowHeight(38);
        deptTable.setShowGrid(false);
        deptTable.setFont(Theme.FONT_REGULAR);
        deptTable.setSelectionBackground(Theme.tableSelectionBg());
        deptTable.setSelectionForeground(Theme.tableSelectionFg());
        deptTable.setBackground(Theme.bgCard());
        deptTable.setForeground(Theme.textPrimary());
        deptTable.getTableHeader().setFont(Theme.FONT_BOLD);
        deptTable.getTableHeader().setBackground(Theme.bgCardAlt());
        deptTable.getTableHeader().setForeground(Theme.textSecondary());
        deptTable.getTableHeader().setPreferredSize(new Dimension(0, 36));

        JScrollPane scroll = new JScrollPane(deptTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.bgCard());
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private static class DeptSummaryRow {
        final String name;
        final String code;
        final int count;
        final String percentage;

        DeptSummaryRow(String name, String code, int count, String percentage) {
            this.name = name;
            this.code = code;
            this.count = count;
            this.percentage = percentage;
        }
    }

    private class DeptSummaryTableModel extends AbstractTableModel {
        private final String[] cols = {"Department Name", "Code", "Students", "Share"};

        @Override public int getRowCount() { return deptRows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int col) { return cols[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            DeptSummaryRow r = deptRows.get(row);
            switch (col) {
                case 0: return r.name;
                case 1: return r.code;
                case 2: return r.count;
                case 3: return r.percentage;
                default: return "";
            }
        }
    }
}
