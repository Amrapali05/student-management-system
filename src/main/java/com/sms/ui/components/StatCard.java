package com.sms.ui.components;

import com.sms.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern dashboard metric card displaying an analytical key indicator with custom icon badge.
 */
public class StatCard extends JPanel {

    private String title;
    private String value;
    private String subtitle;
    private String iconSymbol;
    private Color accentColor;

    public StatCard(String title, String value, String subtitle, String iconSymbol, Color accentColor) {
        this.title = title;
        this.value = value;
        this.subtitle = subtitle;
        this.iconSymbol = iconSymbol;
        this.accentColor = accentColor;

        setOpaque(false);
        setPreferredSize(new Dimension(220, 105));
        setMinimumSize(new Dimension(180, 100));

        Theme.addListener(isDark -> repaint());
    }

    public void updateData(String value, String subtitle) {
        this.value = value;
        this.subtitle = subtitle;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.enableQualityRendering(g2);

        int width = getWidth();
        int height = getHeight();
        int arc = 14;

        // Card Background
        g2.setColor(Theme.bgCard());
        g2.fill(new RoundRectangle2D.Float(0, 0, width, height, arc, arc));

        // Border
        g2.setColor(Theme.border());
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, width - 1, height - 1, arc, arc));

        // Accent top indicator bar
        g2.setColor(accentColor);
        g2.fillRoundRect(12, 1, Math.min(width - 24, 60), 3, 2, 2);

        // Icon badge circle (top right)
        int iconSize = 34;
        int iconX = width - iconSize - 16;
        int iconY = 16;
        g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 35));
        g2.fillOval(iconX, iconY, iconSize, iconSize);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        FontMetrics fmIcon = g2.getFontMetrics();
        int iconTextW = fmIcon.stringWidth(iconSymbol);
        int iconTextH = fmIcon.getAscent();
        g2.setColor(accentColor);
        g2.drawString(iconSymbol, iconX + (iconSize - iconTextW) / 2, iconY + (iconSize + iconTextH) / 2 - 2);

        // Title
        g2.setFont(Theme.FONT_SUBHEADER);
        g2.setColor(Theme.textSecondary());
        g2.drawString(title, 18, 30);

        // Value
        g2.setFont(Theme.FONT_METRIC);
        g2.setColor(Theme.textPrimary());
        // If string is long, scale down font slightly
        if (value.length() > 10) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 19));
        } else if (value.length() > 6) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
        }
        g2.drawString(value, 18, 64);

        // Subtitle
        g2.setFont(Theme.FONT_SMALL);
        g2.setColor(Theme.textMuted());
        g2.drawString(subtitle, 18, 88);

        g2.dispose();
    }
}
