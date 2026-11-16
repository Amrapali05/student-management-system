package com.sms.ui.components;

import com.sms.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

/**
 * Modern Java 2D Bar Chart showing GPA distribution breakdown.
 */
public class GpaBarChart extends JPanel {

    private Map<String, Integer> distribution;

    private static final Color[] BAR_COLORS = {
            new Color(16, 185, 129), // 3.5 - 4.0 Emerald
            new Color(59, 130, 246), // 3.0 - 3.4 Blue
            new Color(245, 158, 11), // 2.0 - 2.9 Amber
            new Color(239, 68, 68)   // < 2.0 Red
    };

    public GpaBarChart() {
        setOpaque(false);
        setPreferredSize(new Dimension(300, 220));
        setMinimumSize(new Dimension(260, 200));

        Theme.addListener(isDark -> repaint());
    }

    public void updateData(Map<String, Integer> dist) {
        this.distribution = dist;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.enableQualityRendering(g2);

        int width = getWidth();
        int height = getHeight();

        // Card Background
        g2.setColor(Theme.bgCard());
        g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 14, 14));
        g2.setColor(Theme.border());
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, width - 1, height - 1, 14, 14));

        // Title
        g2.setFont(Theme.FONT_SUBHEADER);
        g2.setColor(Theme.textPrimary());
        g2.drawString("GPA Performance Distribution", 18, 26);

        if (distribution == null || distribution.isEmpty()) {
            g2.setFont(Theme.FONT_REGULAR);
            g2.setColor(Theme.textMuted());
            g2.drawString("No GPA data available", 20, height / 2);
            g2.dispose();
            return;
        }

        int maxVal = 1;
        for (int v : distribution.values()) {
            if (v > maxVal) maxVal = v;
        }

        int bottomY = height - 42;
        int topY = 60;
        int maxBarHeight = bottomY - topY;

        int numBars = distribution.size();
        int availableW = width - 40;
        int barWidth = Math.min(38, (availableW / numBars) - 14);
        int spacing = (availableW - (numBars * barWidth)) / (numBars + 1);

        int curX = 20 + spacing;
        int idx = 0;

        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            int count = entry.getValue();
            int barHeight = (int) (((double) count / maxVal) * maxBarHeight);
            if (barHeight < 4 && count > 0) barHeight = 4; // Min visible height

            int barY = bottomY - barHeight;
            Color barColor = BAR_COLORS[idx % BAR_COLORS.length];

            // Subtle vertical gradient for bar
            GradientPaint gp = new GradientPaint(
                    curX, barY, barColor,
                    curX, bottomY, new Color(barColor.getRed(), barColor.getGreen(), barColor.getBlue(), 160)
            );
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Float(curX, barY, barWidth, barHeight, 8, 8));

            // Count label on top of bar
            g2.setFont(Theme.FONT_BOLD);
            g2.setColor(Theme.textPrimary());
            FontMetrics fm = g2.getFontMetrics();
            String countStr = String.valueOf(count);
            int countW = fm.stringWidth(countStr);
            g2.drawString(countStr, curX + (barWidth - countW) / 2, Math.max(topY - 4, barY - 6));

            // Short label below bar
            g2.setFont(Theme.FONT_SMALL);
            g2.setColor(Theme.textMuted());
            String key = entry.getKey();
            String shortLabel;
            if (key.contains("3.5")) shortLabel = "3.5-4.0";
            else if (key.contains("3.0")) shortLabel = "3.0-3.4";
            else if (key.contains("2.0")) shortLabel = "2.0-2.9";
            else shortLabel = "< 2.0";

            FontMetrics fmLbl = g2.getFontMetrics();
            int lblW = fmLbl.stringWidth(shortLabel);
            g2.drawString(shortLabel, curX + (barWidth - lblW) / 2, bottomY + 16);

            curX += barWidth + spacing;
            idx++;
        }

        // Baseline axis line
        g2.setColor(Theme.border());
        g2.drawLine(20, bottomY, width - 20, bottomY);

        g2.dispose();
    }
}
