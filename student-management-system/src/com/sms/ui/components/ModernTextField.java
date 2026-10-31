package com.sms.ui.components;

import com.sms.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern rounded text input field with placeholder and focus glow effects.
 */
public class ModernTextField extends JTextField {

    private String placeholder = "";
    private boolean isFocused = false;
    private int cornerRadius = 10;

    public ModernTextField() {
        this("");
    }

    public ModernTextField(String placeholder) {
        this.placeholder = placeholder;
        setFont(Theme.FONT_REGULAR);
        setOpaque(false);
        setBorder(new EmptyBorder(8, 14, 8, 14));
        setCaretColor(Theme.textPrimary());

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });

        Theme.addListener(isDark -> {
            setCaretColor(Theme.textPrimary());
            repaint();
        });
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.enableQualityRendering(g2);

        int width = getWidth();
        int height = getHeight();

        // Background
        g2.setColor(Theme.inputBg());
        g2.fill(new RoundRectangle2D.Float(1, 1, width - 2, height - 2, cornerRadius, cornerRadius));

        // Border / Glow
        if (isFocused) {
            g2.setColor(Theme.ACCENT_PRIMARY);
            g2.setStroke(new BasicStroke(1.8f));
        } else {
            g2.setColor(Theme.border());
            g2.setStroke(new BasicStroke(1.0f));
        }
        g2.draw(new RoundRectangle2D.Float(1, 1, width - 2, height - 2, cornerRadius, cornerRadius));

        // Paint super text
        setForeground(Theme.textPrimary());
        super.paintComponent(g);

        // Placeholder
        if (getText().isEmpty() && !placeholder.isEmpty() && !isFocused) {
            g2.setColor(Theme.textMuted());
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int y = (height + fm.getAscent() - fm.getDescent()) / 2;
            Insets insets = getInsets();
            g2.drawString(placeholder, insets.left + 2, y);
        }

        g2.dispose();
    }
}
