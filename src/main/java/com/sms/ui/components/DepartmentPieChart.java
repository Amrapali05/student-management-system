package com.sms.ui.components;

import com.sms.model.Department;
import com.sms.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

/**
 * Modern Java 2D Donut Chart component for Department breakdown.
 */
public class DepartmentPieChart extends JPanel {

    private Map<Department, Integer> departmentCounts;
    private int total = 0;

    private static final Color[] PALETTE = {
            new Color(99, 102, 241),  // Indigo
            new Color(14, 165, 233),  // Sky Blue
            new Color(16, 185, 129),  // Emerald
            new Color(245, 158, 11),  // Amber
            new Color(168, 85, 247),  // Purple
            new Color(236, 72, 153),  // Pink
            new Color(20, 184, 166)   // Teal
    };

    public DepartmentPieChart() {
        setOpaque(false);
        setPreferredSize(new Dimension(300, 220));
        setMinimumSize(new Dimension(260, 200));

        Theme.addListener(isDark -> repaint());
    }

    public void updateData(Map<Department, Integer> counts) {
        this.departmentCounts = counts;
        this.total = 0;
        if (counts != null) {
            for (int count : counts.values()) {
                this.total += count;
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.enableQualityRendering(g2);

        int width = getWidth();
        int height = getHeight();

        // Background Card
        g2.setColor(Theme.bgCard());
        g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 14, 14));
        g2.setColor(Theme.border());
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, width - 1, height - 1, 14, 14));

        // Title
        g2.setFont(Theme.FONT_SUBHEADER);
        g2.setColor(Theme.textPrimary());
        g2.drawString("Students by Department", 18, 26);

        if (total == 0 || departmentCounts == null) {
            g2.setFont(Theme.FONT_REGULAR);
            g2.setColor(Theme.textMuted());
            g2.drawString("No student data available", 20, height / 2);
            g2.dispose();
            return;
        }

        // Donut Chart Geometry
        int donutSize = Math.min(width / 2 - 20, height - 70);
        int donutX = 20;
        int donutY = 40 + (height - 40 - donutSize) / 2 - 10;
        int holeSize = (int) (donutSize * 0.58);
        int holeX = donutX + (donutSize - holeSize) / 2;
        int holeY = donutY + (donutSize - holeSize) / 2;

        double curAngle = 90.0;
        int colorIdx = 0;

        for (Map.Entry<Department, Integer> entry : departmentCounts.entrySet()) {
            int count = entry.getValue();
            if (count <= 0) continue;

            double arcAngle = (count / (double) total) * 360.0;
            Color arcColor = PALETTE[colorIdx % PALETTE.length];

            g2.setColor(arcColor);
            g2.fill(new Arc2D.Double(donutX, donutY, donutSize, donutSize, curAngle, -arcAngle, Arc2D.PIE));

            curAngle -= arcAngle;
            colorIdx++;
        }

        // Inner Hole for Donut appearance
        g2.setColor(Theme.bgCard());
        g2.fillOval(holeX, holeY, holeSize, holeSize);

        // Center Total Text
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2.setColor(Theme.textPrimary());
        FontMetrics fm = g2.getFontMetrics();
        String totalStr = String.valueOf(total);
        g2.drawString(totalStr, holeX + (holeSize - fm.stringWidth(totalStr)) / 2, holeY + holeSize / 2 + 2);

        g2.setFont(Theme.FONT_SMALL);
        g2.setColor(Theme.textMuted());
        fm = g2.getFontMetrics();
        String labelStr = "Total";
        g2.drawString(labelStr, holeX + (holeSize - fm.stringWidth(labelStr)) / 2, holeY + holeSize / 2 + 16);

        // Legend on Right side
        int legendX = donutX + donutSize + 20;
        int legendY = 55;
        int legendItemHeight = 20;
        colorIdx = 0;

        for (Map.Entry<Department, Integer> entry : departmentCounts.entrySet()) {
            int count = entry.getValue();
            if (count <= 0) continue;

            Color arcColor = PALETTE[colorIdx % PALETTE.length];

            // Color Dot
            g2.setColor(arcColor);
            g2.fillOval(legendX, legendY - 8, 8, 8);

            // Dept Name & Count
            g2.setFont(Theme.FONT_SMALL);
            g2.setColor(Theme.textPrimary());
            String name = entry.getKey().getCode();
            int pct = (int) Math.round((count / (double) total) * 100);
            String itemText = name + ": " + count + " (" + pct + "%)";
            g2.drawString(itemText, legendX + 14, legendY);

            legendY += legendItemHeight;
            colorIdx++;
            if (legendY > height - 15) break; // Don't overflow
        }

        g2.dispose();
    }
}
