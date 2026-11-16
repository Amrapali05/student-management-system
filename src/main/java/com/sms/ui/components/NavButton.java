package com.sms.ui.components;

import com.sms.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern sidebar navigation button with active indicator bar and smooth hover effects.
 */
public class NavButton extends JButton {

    private final String iconText;
    private final String titleText;
    private boolean active = false;
    private boolean hovered = false;

    public NavButton(String iconText, String titleText) {
        this.iconText = iconText;
        this.titleText = titleText;

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(210, 44));
        setMaximumSize(new Dimension(240, 44));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.enableQualityRendering(g2);

        int w = getWidth();
        int h = getHeight();

        // Background
        if (active) {
            g2.setColor(Theme.sidebarActiveBg());
            g2.fill(new RoundRectangle2D.Float(8, 2, w - 16, h - 4, 10, 10));

            // Left vertical indicator bar
            g2.setColor(Theme.ACCENT_PRIMARY);
            g2.fill(new RoundRectangle2D.Float(10, 8, 4, h - 16, 4, 4));
        } else if (hovered) {
            g2.setColor(Theme.sidebarHoverBg());
            g2.fill(new RoundRectangle2D.Float(8, 2, w - 16, h - 4, 10, 10));
        }

        // Icon
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        g2.setColor(active ? Theme.ACCENT_PRIMARY : Theme.textSecondary());
        FontMetrics fmIcon = g2.getFontMetrics();
        int iconY = (h - fmIcon.getHeight()) / 2 + fmIcon.getAscent();
        g2.drawString(iconText, 24, iconY);

        // Label
        g2.setFont(active ? Theme.FONT_HEADER : Theme.FONT_REGULAR);
        g2.setColor(active ? (Theme.isDark() ? Color.WHITE : Theme.ACCENT_PRIMARY) : Theme.textPrimary());
        FontMetrics fmLbl = g2.getFontMetrics();
        int labelY = (h - fmLbl.getHeight()) / 2 + fmLbl.getAscent();
        g2.drawString(titleText, 56, labelY);

        g2.dispose();
    }
}
