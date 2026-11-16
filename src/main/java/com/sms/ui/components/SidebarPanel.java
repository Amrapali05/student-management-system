package com.sms.ui.components;

import com.sms.config.AppConfig;
import com.sms.ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise Left Sidebar Panel containing logo brand, primary view navigation,
 * active status indicators, and theme controls.
 */
public class SidebarPanel extends JPanel {

    public interface NavSelectionListener {
        void onNavSelected(String viewKey);
    }

    private final List<NavSelectionListener> listeners = new ArrayList<>();
    private final List<NavButton> navButtons = new ArrayList<>();

    private NavButton dashboardBtn;
    private NavButton directoryBtn;
    private NavButton analyticsBtn;
    private NavButton settingsBtn;

    private JLabel statusPill;
    private ModernButton themeBtn;

    public SidebarPanel() {
        setLayout(new BorderLayout(0, 0));
        setPreferredSize(new Dimension(245, 0));
        setMinimumSize(new Dimension(220, 0));

        initUI();
        Theme.addListener(isDark -> {
            setBackground(Theme.bgSidebar());
            if (themeBtn != null) {
                themeBtn.setText(isDark ? "☀️ Light Mode" : "🌙 Dark Mode");
            }
            repaint();
        });
    }

    public void addNavListener(NavSelectionListener listener) {
        listeners.add(listener);
    }

    public void selectView(String viewKey) {
        dashboardBtn.setActive("DASHBOARD".equals(viewKey));
        directoryBtn.setActive("DIRECTORY".equals(viewKey));
        analyticsBtn.setActive("ANALYTICS".equals(viewKey));
        settingsBtn.setActive("SETTINGS".equals(viewKey));

        for (NavSelectionListener l : listeners) {
            l.onNavSelected(viewKey);
        }
    }

    public void updateStorageStatus(String statusText, boolean isDatabase) {
        if (statusPill != null) {
            statusPill.setText("● " + statusText);
            statusPill.setForeground(isDatabase ? Theme.COLOR_PURPLE : Theme.COLOR_SUCCESS);
            statusPill.repaint();
        }
    }

    private void initUI() {
        setBackground(Theme.bgSidebar());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.border()));

        // --- TOP BRAND HEADER ---
        JPanel brandPanel = new JPanel(new BorderLayout(0, 8));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(22, 20, 18, 20));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoRow.setOpaque(false);

        JLabel logoIcon = new JLabel("🎓");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        logoRow.add(logoIcon);

        JPanel brandTextPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        brandTextPanel.setOpaque(false);

        JLabel brandTitle = new JLabel("EduCore");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brandTitle.setForeground(Theme.ACCENT_PRIMARY);

        JLabel brandSubtitle = new JLabel("Student Management");
        brandSubtitle.setFont(Theme.FONT_SMALL);
        brandSubtitle.setForeground(Theme.textSecondary());

        brandTextPanel.add(brandTitle);
        brandTextPanel.add(brandSubtitle);
        logoRow.add(brandTextPanel);

        brandPanel.add(logoRow, BorderLayout.CENTER);

        // Version badge pill
        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeRow.setOpaque(false);
        JLabel versionBadge = new JLabel("  v2.0 Enterprise • Java 21  ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.enableQualityRendering(g2);
                g2.setColor(Theme.bgCardAlt());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(Theme.border());
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        versionBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        versionBadge.setForeground(Theme.textSecondary());
        versionBadge.setBorder(new EmptyBorder(3, 8, 3, 8));
        badgeRow.add(versionBadge);
        brandPanel.add(badgeRow, BorderLayout.SOUTH);

        add(brandPanel, BorderLayout.NORTH);

        // --- CENTER NAV BUTTONS ---
        JPanel navList = new JPanel();
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
        navList.setOpaque(false);
        navList.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel menuSectionLbl = new JLabel("  MAIN MENU");
        menuSectionLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        menuSectionLbl.setForeground(Theme.textMuted());
        menuSectionLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        navList.add(menuSectionLbl);
        navList.add(Box.createVerticalStrut(8));

        dashboardBtn = new NavButton("📊", "Dashboard");
        directoryBtn = new NavButton("👥", "Student Directory");
        analyticsBtn = new NavButton("📈", "Analytics & Reports");

        navButtons.add(dashboardBtn);
        navButtons.add(directoryBtn);
        navButtons.add(analyticsBtn);

        dashboardBtn.addActionListener(e -> selectView("DASHBOARD"));
        directoryBtn.addActionListener(e -> selectView("DIRECTORY"));
        analyticsBtn.addActionListener(e -> selectView("ANALYTICS"));

        for (NavButton btn : navButtons) {
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            navList.add(btn);
            navList.add(Box.createVerticalStrut(4));
        }

        navList.add(Box.createVerticalStrut(16));
        JLabel systemSectionLbl = new JLabel("  SYSTEM & PERSISTENCE");
        systemSectionLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        systemSectionLbl.setForeground(Theme.textMuted());
        systemSectionLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        navList.add(systemSectionLbl);
        navList.add(Box.createVerticalStrut(8));

        settingsBtn = new NavButton("⚙️", "Database & Settings");
        settingsBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsBtn.addActionListener(e -> selectView("SETTINGS"));
        navButtons.add(settingsBtn);
        navList.add(settingsBtn);

        dashboardBtn.setActive(true);

        add(navList, BorderLayout.CENTER);

        // --- BOTTOM STATUS & PROFILE ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.border()),
                new EmptyBorder(16, 16, 16, 16)
        ));

        // Theme toggle button
        themeBtn = new ModernButton(Theme.isDark() ? "☀️ Light Mode" : "🌙 Dark Mode", ModernButton.ButtonVariant.OUTLINE);
        themeBtn.setMaximumSize(new Dimension(210, 36));
        themeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        themeBtn.addActionListener(e -> Theme.toggleTheme());
        bottomPanel.add(themeBtn);
        bottomPanel.add(Box.createVerticalStrut(12));

        // Persistence status pill
        boolean isDb = AppConfig.getInstance().getStorageType() == AppConfig.StorageType.DATABASE;
        statusPill = new JLabel(isDb ? "● Database Storage Active" : "● Thread-Safe JSON Active");
        statusPill.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusPill.setForeground(isDb ? Theme.COLOR_PURPLE : Theme.COLOR_SUCCESS);
        statusPill.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomPanel.add(statusPill);

        bottomPanel.add(Box.createVerticalStrut(10));

        // Profile pill
        JPanel profileRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        profileRow.setOpaque(false);
        profileRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userAvatar = new JLabel("👤");
        userAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        profileRow.add(userAvatar);

        JPanel userText = new JPanel(new GridLayout(2, 1, 0, 0));
        userText.setOpaque(false);
        JLabel userName = new JLabel("Administrator");
        userName.setFont(Theme.FONT_BOLD);
        userName.setForeground(Theme.textPrimary());

        JLabel userRole = new JLabel("Enterprise Console");
        userRole.setFont(Theme.FONT_SMALL);
        userRole.setForeground(Theme.textMuted());

        userText.add(userName);
        userText.add(userRole);
        profileRow.add(userText);

        bottomPanel.add(profileRow);

        add(bottomPanel, BorderLayout.SOUTH);
    }
}
