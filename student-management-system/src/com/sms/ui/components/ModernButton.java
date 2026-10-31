package com.sms.ui.components;

import com.sms.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern rounded button with hover animations and distinct visual styles.
 */
public class ModernButton extends JButton {

    public enum ButtonStyle {
        PRIMARY,
        SECONDARY,
        DANGER,
        SUCCESS,
        OUTLINE,
        GHOST
    }

    private ButtonStyle style = ButtonStyle.PRIMARY;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private int cornerRadius = 10;

    public ModernButton(String text) {
        this(text, ButtonStyle.PRIMARY);
    }

    public ModernButton(String text, ButtonStyle style) {
        super(text);
        this.style = style;
        setFont(Theme.FONT_BOLD);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(8, 16, 8, 16));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });

        Theme.addListener(isDark -> repaint());
    }

    public void setButtonStyle(ButtonStyle style) {
        this.style = style;
        repaint();
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.enableQualityRendering(g2);

        int width = getWidth();
        int height = getHeight();

        Color baseBg;
        Color hoverBg;
        Color textCol;
        Color borderCol = null;

        switch (style) {
            case PRIMARY:
                baseBg = Theme.ACCENT_PRIMARY;
                hoverBg = Theme.ACCENT_PRIMARY_HOVER;
                textCol = Color.WHITE;
                break;
            case SECONDARY:
                baseBg = Theme.ACCENT_SECONDARY;
                hoverBg = new Color(2, 132, 199);
                textCol = Color.WHITE;
                break;
            case DANGER:
                baseBg = Theme.COLOR_DANGER;
                hoverBg = new Color(220, 38, 38);
                textCol = Color.WHITE;
                break;
            case SUCCESS:
                baseBg = Theme.COLOR_SUCCESS;
                hoverBg = new Color(5, 150, 105);
                textCol = Color.WHITE;
                break;
            case OUTLINE:
                baseBg = new Color(0, 0, 0, 0);
                hoverBg = Theme.bgHover();
                textCol = Theme.textPrimary();
                borderCol = Theme.border();
                break;
            case GHOST:
            default:
                baseBg = new Color(0, 0, 0, 0);
                hoverBg = Theme.bgHover();
                textCol = Theme.textPrimary();
                break;
        }

        if (!isEnabled()) {
            baseBg = Theme.border();
            textCol = Theme.textMuted();
        }

        Color currentBg = isPressed ? hoverBg.darker() : (isHovered ? hoverBg : baseBg);

        // Fill button shape
        if (currentBg.getAlpha() > 0) {
            g2.setColor(currentBg);
            g2.fill(new RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius));
        }

        // Draw border if needed
        if (borderCol != null && isEnabled()) {
            g2.setColor(borderCol);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, width - 1, height - 1, cornerRadius, cornerRadius));
        }

        // Text
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int stringWidth = fm.stringWidth(getText());
        int stringHeight = fm.getAscent();
        int x = (width - stringWidth) / 2;
        int y = (height + stringHeight) / 2 - 2;

        g2.setColor(textCol);
        g2.drawString(getText(), x, y);

        g2.dispose();
    }
}
