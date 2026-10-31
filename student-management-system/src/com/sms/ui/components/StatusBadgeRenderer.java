package com.sms.ui.components;

import com.sms.model.StudentStatus;
import com.sms.ui.Theme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Table cell renderer that renders StudentStatus as a modern status pill badge.
 */
public class StatusBadgeRenderer extends DefaultTableCellRenderer {

    private StudentStatus currentStatus = StudentStatus.ACTIVE;
    private boolean isSelected = false;

    public StatusBadgeRenderer() {
        setOpaque(false);
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        this.isSelected = isSelected;
        if (value instanceof StudentStatus) {
            this.currentStatus = (StudentStatus) value;
        } else if (value != null) {
            this.currentStatus = StudentStatus.fromString(value.toString());
        } else {
            this.currentStatus = StudentStatus.ACTIVE;
        }
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.enableQualityRendering(g2);

        int width = getWidth();
        int height = getHeight();

        Color baseColor;
        switch (currentStatus) {
            case ACTIVE:
                baseColor = Theme.COLOR_SUCCESS;
                break;
            case INACTIVE:
                baseColor = Theme.COLOR_WARNING;
                break;
            case SUSPENDED:
                baseColor = Theme.COLOR_DANGER;
                break;
            case GRADUATED:
            default:
                baseColor = Theme.COLOR_INFO;
                break;
        }

        // Pill dimensions
        int badgeWidth = Math.min(width - 16, 95);
        int badgeHeight = 24;
        int badgeX = (width - badgeWidth) / 2;
        int badgeY = (height - badgeHeight) / 2;

        // Pill background
        Color bgPill = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), isSelected ? 60 : 35);
        g2.setColor(bgPill);
        g2.fill(new RoundRectangle2D.Float(badgeX, badgeY, badgeWidth, badgeHeight, 12, 12));

        // Pill border
        g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 120));
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(badgeX, badgeY, badgeWidth, badgeHeight, 12, 12));

        // Dot indicator
        int dotSize = 6;
        int dotX = badgeX + 10;
        int dotY = badgeY + (badgeHeight - dotSize) / 2;
        g2.setColor(baseColor);
        g2.fillOval(dotX, dotY, dotSize, dotSize);

        // Text
        g2.setFont(Theme.FONT_BOLD);
        FontMetrics fm = g2.getFontMetrics();
        String text = currentStatus.getDisplayName();
        int textX = dotX + dotSize + 6;
        int textY = badgeY + (badgeHeight + fm.getAscent() - fm.getDescent()) / 2;

        g2.setColor(baseColor);
        g2.drawString(text, textX, textY);

        g2.dispose();
    }
}
