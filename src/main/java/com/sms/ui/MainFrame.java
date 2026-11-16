package com.sms.ui;

import com.sms.concurrent.ThreadManager;
import com.sms.model.Student;
import com.sms.service.StudentService;
import com.sms.service.StudentStatistics;
import com.sms.ui.components.ModernButton;
import com.sms.ui.components.ModernTextField;
import com.sms.ui.components.SidebarPanel;
import com.sms.ui.views.AnalyticsView;
import com.sms.ui.views.DashboardView;
import com.sms.ui.views.DirectoryView;
import com.sms.ui.views.SettingsView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enterprise Modern Desktop Frame featuring a left navigation sidebar,
 * top global action bar, multi-card workspace, and high-concurrency background operations.
 */
public class MainFrame extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getName());

    private final StudentService service;

    // Layout
    private SidebarPanel sidebarPanel;
    private CardLayout cardLayout;
    private JPanel contentCards;

    // Header Controls
    private JLabel sectionTitleLabel;
    private JLabel sectionSubtitleLabel;
    private ModernTextField globalSearchField;

    // Views
    private DashboardView dashboardView;
    private DirectoryView directoryView;
    private AnalyticsView analyticsView;
    private SettingsView settingsView;

    // Status Bar
    private JLabel statusLabel;

    public MainFrame(StudentService service) {
        this.service = service;

        setTitle("EduCore - Enterprise Student Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1380, 880);
        setMinimumSize(new Dimension(1150, 720));
        setLocationRelativeTo(null);

        initUI();
        refreshAllAsync();

        Theme.addListener(isDark -> applyTheme());
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout(0, 0));
        rootPanel.setBackground(Theme.bgMain());
        setContentPane(rootPanel);

        // 1. LEFT SIDEBAR NAVIGATION
        sidebarPanel = new SidebarPanel();
        sidebarPanel.addNavListener(this::switchView);
        rootPanel.add(sidebarPanel, BorderLayout.WEST);

        // 2. RIGHT / CENTER WORKSPACE
        JPanel workspacePanel = new JPanel(new BorderLayout(0, 0));
        workspacePanel.setOpaque(false);

        // Top Navigation Header
        workspacePanel.add(createTopHeaderBar(), BorderLayout.NORTH);

        // Center Card Panels
        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.setOpaque(false);

        dashboardView = new DashboardView(this, service, () -> {
            sidebarPanel.selectView("DIRECTORY");
        });
        directoryView = new DirectoryView(this, service, this::refreshAllAsync);
        analyticsView = new AnalyticsView();
        settingsView = new SettingsView(this, service, this::refreshAllAsync);

        contentCards.add(dashboardView, "DASHBOARD");
        contentCards.add(directoryView, "DIRECTORY");
        contentCards.add(analyticsView, "ANALYTICS");
        contentCards.add(settingsView, "SETTINGS");

        workspacePanel.add(contentCards, BorderLayout.CENTER);

        // Bottom Status Bar
        workspacePanel.add(createStatusBar(), BorderLayout.SOUTH);

        rootPanel.add(workspacePanel, BorderLayout.CENTER);
    }

    private JPanel createTopHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.bgHeader());
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.border()),
                new EmptyBorder(14, 28, 14, 28)
        ));

        // Left Section Title & Breadcrumb
        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);

        sectionTitleLabel = new JLabel("Executive Dashboard");
        sectionTitleLabel.setFont(Theme.FONT_TITLE);
        sectionTitleLabel.setForeground(Theme.textPrimary());

        sectionSubtitleLabel = new JLabel("Institutional Performance Metrics & Real-time Analytics");
        sectionSubtitleLabel.setFont(Theme.FONT_SMALL);
        sectionSubtitleLabel.setForeground(Theme.textSecondary());

        titleBox.add(sectionTitleLabel);
        titleBox.add(sectionSubtitleLabel);
        header.add(titleBox, BorderLayout.WEST);

        // Right Quick Actions
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightActions.setOpaque(false);

        globalSearchField = new ModernTextField("Quick Search students...");
        globalSearchField.setPreferredSize(new Dimension(220, 36));
        globalSearchField.addActionListener(e -> {
            String q = globalSearchField.getText().trim();
            if (!q.isEmpty()) {
                directoryView.setSearchQuery(q);
                sidebarPanel.selectView("DIRECTORY");
            }
        });

        ModernButton refreshBtn = new ModernButton("⚡ Refresh", ModernButton.ButtonVariant.OUTLINE);
        refreshBtn.addActionListener(e -> {
            refreshAllAsync();
            statusLabel.setText("● Roster and analytics refreshed.");
        });

        ModernButton addBtn = new ModernButton("+ Add Student", ModernButton.ButtonVariant.PRIMARY);
        addBtn.addActionListener(e -> directoryView.onAddStudent());

        rightActions.add(globalSearchField);
        rightActions.add(refreshBtn);
        rightActions.add(addBtn);

        header.add(rightActions, BorderLayout.EAST);
        return header;
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.bgHeader());
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.border()),
                new EmptyBorder(7, 28, 7, 28)
        ));

        statusLabel = new JLabel("● Persistence active: Thread-safe repository synchronized.");
        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setForeground(Theme.COLOR_SUCCESS);

        JLabel versionLabel = new JLabel("EduCore v2.0 Enterprise • Java 21 LTS");
        versionLabel.setFont(Theme.FONT_SMALL);
        versionLabel.setForeground(Theme.textMuted());

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(versionLabel, BorderLayout.EAST);
        return bar;
    }

    private void switchView(String viewKey) {
        cardLayout.show(contentCards, viewKey);

        switch (viewKey) {
            case "DASHBOARD":
                sectionTitleLabel.setText("Executive Dashboard");
                sectionSubtitleLabel.setText("Institutional Performance Metrics & Real-time Analytics");
                break;
            case "DIRECTORY":
                sectionTitleLabel.setText("Student Directory Roster");
                sectionSubtitleLabel.setText("Comprehensive Academic Administration & Record Filtering");
                directoryView.refreshTableAsync();
                break;
            case "ANALYTICS":
                sectionTitleLabel.setText("Academic Analytics & Intelligence");
                sectionSubtitleLabel.setText("Performance Distribution, Major Densities & Tier Breakdown");
                break;
            case "SETTINGS":
                sectionTitleLabel.setText("Database & Persistence Settings");
                sectionSubtitleLabel.setText("Relational Database Console, Disaster Recovery & CSV Data Hub");
                break;
        }
    }

    /**
     * Refreshes all data models and analytical charts concurrently without freezing the EDT.
     */
    public void refreshAllAsync() {
        service.calculateStatisticsAsync().thenAccept(stats -> {
            List<Student> all = service.getAllStudents();
            SwingUtilities.invokeLater(() -> {
                dashboardView.updateData(stats, all);
                analyticsView.updateData(stats);
                directoryView.refreshTableAsync();
            });
        }).exceptionally(ex -> {
            LOGGER.log(Level.SEVERE, "Failed to refresh dashboard data: {0}", ex.getMessage());
            return null;
        });
    }

    private void applyTheme() {
        getContentPane().setBackground(Theme.bgMain());
        SwingUtilities.updateComponentTreeUI(this);
        repaint();
    }
}
