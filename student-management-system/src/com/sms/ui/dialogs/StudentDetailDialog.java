package com.sms.ui.dialogs;

import com.sms.model.Student;
import com.sms.model.StudentStatus;
import com.sms.service.StudentService;
import com.sms.ui.Theme;
import com.sms.ui.components.ModernButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modal profile view dialog presenting detailed student profile and quick actions.
 */
public class StudentDetailDialog extends JDialog {

    public enum ActionOutcome {
        NONE,
        EDIT,
        DELETED
    }

    private final Frame parentFrame;
    private final StudentService service;
    private Student student;
    private ActionOutcome outcome = ActionOutcome.NONE;

    public StudentDetailDialog(Frame parent, StudentService service, Student student) {
        super(parent, "Student Profile - " + student.getId(), true);
        this.parentFrame = parent;
        this.service = service;
        this.student = student;

        initUI();
        setSize(520, 580);
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    public ActionOutcome getOutcome() {
        return outcome;
    }

    public Student getStudent() {
        return student;
    }

    private void initUI() {
        JPanel contentPane = new JPanel(new BorderLayout(0, 16));
        contentPane.setBackground(Theme.bgCard());
        contentPane.setBorder(new EmptyBorder(24, 28, 24, 28));
        setContentPane(contentPane);

        // Header Profile Summary
        JPanel headerPanel = new JPanel(new BorderLayout(16, 0));
        headerPanel.setOpaque(false);

        // Initials avatar
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.enableQualityRendering(g2);
                int size = 64;
                g2.setColor(Theme.ACCENT_PRIMARY);
                g2.fillOval(0, 0, size, size);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                g2.setColor(Color.WHITE);
                String initials = "";
                if (student.getFirstName() != null && !student.getFirstName().isEmpty()) {
                    initials += student.getFirstName().charAt(0);
                }
                if (student.getLastName() != null && !student.getLastName().isEmpty()) {
                    initials += student.getLastName().charAt(0);
                }
                FontMetrics fm = g2.getFontMetrics();
                int x = (size - fm.stringWidth(initials)) / 2;
                int y = (size + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(initials, x, y);
                g2.dispose();
            }
        };
        avatarPanel.setPreferredSize(new Dimension(64, 64));
        avatarPanel.setOpaque(false);
        headerPanel.add(avatarPanel, BorderLayout.WEST);

        // Name, ID and Status
        JPanel nameInfoPanel = new JPanel(new GridLayout(3, 1, 0, 2));
        nameInfoPanel.setOpaque(false);

        JLabel nameLbl = new JLabel(student.getFullName());
        nameLbl.setFont(Theme.FONT_TITLE);
        nameLbl.setForeground(Theme.textPrimary());

        JLabel idLbl = new JLabel("ID: " + student.getId() + "  •  " + student.getDepartment().getFullName());
        idLbl.setFont(Theme.FONT_SUBHEADER);
        idLbl.setForeground(Theme.textSecondary());

        JLabel statusLbl = new JLabel("Status: " + student.getStatus().getDisplayName() + 
                "  •  " + student.getYearLabel());
        statusLbl.setFont(Theme.FONT_REGULAR);
        statusLbl.setForeground(getStatusColor(student.getStatus()));

        nameInfoPanel.add(nameLbl);
        nameInfoPanel.add(idLbl);
        nameInfoPanel.add(statusLbl);
        headerPanel.add(nameInfoPanel, BorderLayout.CENTER);

        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Details Grid in a stylized card
        JPanel cardPanel = new JPanel(new GridLayout(0, 2, 14, 14)) {
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
        cardPanel.setOpaque(false);
        cardPanel.setBorder(new EmptyBorder(16, 18, 16, 18));

        cardPanel.add(createDetailItem("Cumulative GPA", String.format(java.util.Locale.US, "%.2f", student.getGpa()) + " / 4.00"));
        cardPanel.add(createDetailItem("Academic Standing", getStanding(student.getGpa())));

        cardPanel.add(createDetailItem("Email Address", student.getEmail()));
        cardPanel.add(createDetailItem("Phone Number", student.getPhone() != null && !student.getPhone().isEmpty() ? student.getPhone() : "Not provided"));

        cardPanel.add(createDetailItem("Department / Major", student.getDepartment().getFullName()));
        cardPanel.add(createDetailItem("Academic Year", student.getYearLabel()));

        cardPanel.add(createDetailItem("Date of Birth", student.getDateOfBirth() != null && !student.getDateOfBirth().isEmpty() ? student.getDateOfBirth() : "Not specified"));
        cardPanel.add(createDetailItem("Gender", student.getGender()));

        cardPanel.add(createDetailItem("Enrollment Date", student.getEnrollmentDate() != null && !student.getEnrollmentDate().isEmpty() ? student.getEnrollmentDate() : "Not specified"));
        cardPanel.add(createDetailItem("Address", student.getAddress() != null && !student.getAddress().isEmpty() ? student.getAddress() : "Campus"));

        contentPane.add(cardPanel, BorderLayout.CENTER);

        // Footer Actions
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footerPanel.setOpaque(false);

        ModernButton deleteBtn = new ModernButton("Delete Student", ModernButton.ButtonStyle.DANGER);
        deleteBtn.addActionListener(e -> onDelete());

        ModernButton editBtn = new ModernButton("Edit Profile", ModernButton.ButtonStyle.PRIMARY);
        editBtn.addActionListener(e -> {
            outcome = ActionOutcome.EDIT;
            dispose();
        });

        ModernButton closeBtn = new ModernButton("Close", ModernButton.ButtonStyle.OUTLINE);
        closeBtn.addActionListener(e -> dispose());

        footerPanel.add(deleteBtn);
        footerPanel.add(editBtn);
        footerPanel.add(closeBtn);

        contentPane.add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createDetailItem(String label, String value) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.textMuted());

        JLabel val = new JLabel(value);
        val.setFont(Theme.FONT_SUBHEADER);
        val.setForeground(Theme.textPrimary());

        p.add(lbl);
        p.add(val);
        return p;
    }

    private Color getStatusColor(StudentStatus status) {
        switch (status) {
            case ACTIVE: return Theme.COLOR_SUCCESS;
            case INACTIVE: return Theme.COLOR_WARNING;
            case SUSPENDED: return Theme.COLOR_DANGER;
            case GRADUATED: default: return Theme.COLOR_INFO;
        }
    }

    private String getStanding(double gpa) {
        if (gpa >= 3.8) return "Summa Cum Laude";
        if (gpa >= 3.5) return "Dean's Honor List";
        if (gpa >= 3.0) return "Good Standing";
        if (gpa >= 2.0) return "Satisfactory";
        return "Academic Probation";
    }

    private void onDelete() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete student " + student.getFullName() + " (" + student.getId() + ")?\nThis action cannot be undone.",
                "Confirm Student Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.deleteStudent(student.getId());
                outcome = ActionOutcome.DELETED;
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
