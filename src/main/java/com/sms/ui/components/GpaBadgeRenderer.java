package com.sms.ui.components;

import com.sms.ui.Theme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Table cell renderer for Student GPA featuring an inline progress indicator.
 */
public class GpaBadgeRenderer extends DefaultTableCellRenderer {

    private double gpa = 0.0;
    private boolean isSelected = false;

    public GpaBadgeRenderer() {
        setOpaque(false);
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        this.isSelected = isSelected;
        if (value instanceof Number) {
            this.gpa = ((Number) value).doubleValue();
        } else if (value != null) {
            try {
                this.gpa = Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
                this.gpa = 0.0;
            }
        }
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.enableQualityRendering(g2);

        int width = getWidth();
        int height = getHeight();

        Color gpaColor;
        if (gpa >= 3.5) {
            gpaColor = Theme.COLOR_SUCCESS;
        } else if (gpa >= 3.0) {
            gpaColor = Theme.COLOR_INFO;
        } else if (gpa >= 2.0) {
            gpaColor = Theme.COLOR_WARNING;
        } else {
            gpaColor = Theme.COLOR_DANGER;
        }

        // GPA Text
        String gpaText = String.format(java.util.Locale.US, "%.2f", gpa);
        g2.setFont(Theme.FONT_BOLD);
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(gpaText);
        int textX = (width - textW) / 2;
        int textY = (height / 2) + 2;

        g2.setColor(isSelected ? (Theme.isDark() ? Color.WHITE : Theme.textPrimary()) : gpaColor);
        g2.drawString(gpaText, textX, textY);

        // Mini progress bar under text
        int barW = Math.min(width - 24, 50);
        int barH = 3;
        int barX = (width - barW) / 2;
        int barY = textY + 4;

        // Bar background
        g2.setColor(Theme.border());
        g2.fill(new RoundRectangle2D.Float(barX, barY, barW, barH, 2, 2));

        // Bar filled portion (0.0 to 4.0)
        double ratio = Math.max(0.0, Math.min(1.0, gpa / 4.0));
        int filledW = (int) (barW * ratio);
        g2.setColor(gpaColor);
        g2.fill(new RoundRectangle2D.Float(barX, barY, filledW, barH, 2, 2));

        g2.dispose();
    }
}
