package com.sms.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern design system providing color palettes, typography, and theme switching.
 */
public class Theme {

    public interface ThemeChangeListener {
        void onThemeChanged(boolean isDark);
    }

    private static boolean darkTheme = true;
    private static final List<ThemeChangeListener> listeners = new ArrayList<>();

    // Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_METRIC = new Font("Segoe UI", Font.BOLD, 26);

    // Accent Colors
    public static final Color ACCENT_PRIMARY = new Color(99, 102, 241);     // Indigo
    public static final Color ACCENT_PRIMARY_HOVER = new Color(79, 70, 229);
    public static final Color ACCENT_SECONDARY = new Color(14, 165, 233);   // Sky Blue
    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);      // Emerald Green
    public static final Color COLOR_WARNING = new Color(245, 158, 11);      // Amber
    public static final Color COLOR_DANGER = new Color(239, 68, 68);        // Rose Red
    public static final Color COLOR_INFO = new Color(59, 130, 246);         // Royal Blue
    public static final Color COLOR_PURPLE = new Color(168, 85, 247);       // Violet

    public static boolean isDark() {
        return darkTheme;
    }

    public static void setDarkTheme(boolean dark) {
        darkTheme = dark;
        for (ThemeChangeListener l : listeners) {
            l.onThemeChanged(dark);
        }
    }

    public static void toggleTheme() {
        setDarkTheme(!darkTheme);
    }

    public static void addListener(ThemeChangeListener l) {
        listeners.add(l);
    }

    // Dynamic Colors based on active theme
    public static Color bgMain() {
        return darkTheme ? new Color(15, 23, 42) : new Color(248, 250, 252); // #0F172A vs #F8FAFC
    }

    public static Color bgHeader() {
        return darkTheme ? new Color(30, 41, 59) : new Color(255, 255, 255); // #1E293B vs #FFFFFF
    }

    public static Color bgCard() {
        return darkTheme ? new Color(30, 41, 59) : new Color(255, 255, 255);
    }

    public static Color bgCardAlt() {
        return darkTheme ? new Color(24, 33, 47) : new Color(241, 245, 249);
    }

    public static Color bgHover() {
        return darkTheme ? new Color(51, 65, 85) : new Color(226, 232, 240);
    }

    public static Color border() {
        return darkTheme ? new Color(51, 65, 85) : new Color(226, 232, 240);
    }

    public static Color borderLight() {
        return darkTheme ? new Color(71, 85, 105) : new Color(203, 213, 225);
    }

    public static Color textPrimary() {
        return darkTheme ? new Color(248, 250, 252) : new Color(15, 23, 42);
    }

    public static Color textSecondary() {
        return darkTheme ? new Color(148, 163, 184) : new Color(71, 85, 105);
    }

    public static Color textMuted() {
        return darkTheme ? new Color(100, 116, 139) : new Color(148, 163, 184);
    }

    public static Color inputBg() {
        return darkTheme ? new Color(15, 23, 42) : new Color(255, 255, 255);
    }

    public static Color tableRowEven() {
        return darkTheme ? new Color(30, 41, 59) : new Color(255, 255, 255);
    }

    public static Color tableRowOdd() {
        return darkTheme ? new Color(23, 32, 48) : new Color(248, 250, 252);
    }

    public static Color tableSelectionBg() {
        return darkTheme ? new Color(55, 48, 163, 180) : new Color(224, 231, 255);
    }

    public static Color tableSelectionFg() {
        return darkTheme ? Color.WHITE : new Color(49, 46, 129);
    }

    /**
     * Enables anti-aliasing on Graphics2D for ultra-crisp modern rendering.
     */
    public static void enableQualityRendering(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }
}
