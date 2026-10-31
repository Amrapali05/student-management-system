package com.sms.ui.dialogs;

import com.sms.model.Department;
import com.sms.model.Student;
import com.sms.model.StudentStatus;
import com.sms.service.StudentService;
import com.sms.ui.Theme;
import com.sms.ui.components.ModernButton;
import com.sms.ui.components.ModernTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Modal dialog for creating and editing student records with form validation.
 */
public class StudentFormDialog extends JDialog {

    private final StudentService studentService;
    private final Student existingStudent;
    private boolean saved = false;

    private ModernTextField idField;
    private ModernTextField firstNameField;
    private ModernTextField lastNameField;
    private ModernTextField emailField;
    private ModernTextField phoneField;
    private ModernTextField dobField;
    private JComboBox<String> genderCombo;
    private JComboBox<Department> deptCombo;
    private JComboBox<String> yearCombo;
    private ModernTextField gpaField;
    private JComboBox<StudentStatus> statusCombo;
    private ModernTextField enrollDateField;
    private ModernTextField addressField;
    private JLabel errorLabel;

    public StudentFormDialog(Frame parent, StudentService service, Student student) {
        super(parent, student == null ? "Add New Student" : "Edit Student Profile", true);
        this.studentService = service;
        this.existingStudent = student;

        initUI();
        if (student != null) {
            populateFields(student);
        } else {
            idField.setText(studentService.generateNextId());
            idField.setEditable(false);
            dobField.setText("2004-01-15");
            enrollDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            gpaField.setText("3.50");
        }

        setSize(580, 680);
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    public boolean isSaved() {
        return saved;
    }

    private void initUI() {
        JPanel contentPane = new JPanel(new BorderLayout(0, 16));
        contentPane.setBackground(Theme.bgCard());
        contentPane.setBorder(new EmptyBorder(24, 28, 24, 28));
        setContentPane(contentPane);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(existingStudent == null ? "✨ Add New Student" : "✏ Edit Student Profile");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.textPrimary());

        JLabel subLabel = new JLabel(existingStudent == null ? 
                "Fill in the academic and personal details below to register a student." :
                "Update the student's personal information, major, or academic metrics.");
        subLabel.setFont(Theme.FONT_REGULAR);
        subLabel.setForeground(Theme.textSecondary());

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Form Fields Grid
        JPanel formGrid = new JPanel(new GridLayout(0, 2, 16, 14));
        formGrid.setOpaque(false);

        idField = new ModernTextField("Student ID (e.g. STU-1001)");
        firstNameField = new ModernTextField("First Name");
        lastNameField = new ModernTextField("Last Name");
        emailField = new ModernTextField("student@university.edu");
        phoneField = new ModernTextField("+1 (555) 000-0000");
        dobField = new ModernTextField("YYYY-MM-DD");

        genderCombo = createComboBox(new String[]{"Female", "Male", "Non-binary", "Other"});
        deptCombo = createComboBox(Department.values());
        yearCombo = createComboBox(new String[]{
                "1 - Freshman (Year 1)",
                "2 - Sophomore (Year 2)",
                "3 - Junior (Year 3)",
                "4 - Senior (Year 4)"
        });
        gpaField = new ModernTextField("0.00 - 4.00");
        statusCombo = createComboBox(StudentStatus.values());
        enrollDateField = new ModernTextField("YYYY-MM-DD");
        addressField = new ModernTextField("City, State or Full Address");

        formGrid.add(createFieldGroup("Student ID *", idField));
        formGrid.add(createFieldGroup("Academic Status *", statusCombo));

        formGrid.add(createFieldGroup("First Name *", firstNameField));
        formGrid.add(createFieldGroup("Last Name *", lastNameField));

        formGrid.add(createFieldGroup("Email Address *", emailField));
        formGrid.add(createFieldGroup("Phone Number", phoneField));

        formGrid.add(createFieldGroup("Department / Major *", deptCombo));
        formGrid.add(createFieldGroup("Academic Year *", yearCombo));

        formGrid.add(createFieldGroup("Cumulative GPA (0.00-4.00) *", gpaField));
        formGrid.add(createFieldGroup("Date of Birth (YYYY-MM-DD)", dobField));

        formGrid.add(createFieldGroup("Gender", genderCombo));
        formGrid.add(createFieldGroup("Enrollment Date", enrollDateField));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(formGrid, BorderLayout.CENTER);

        // Address spans full width
        JPanel addressPanel = createFieldGroup("Residential Address / Campus Dorm", addressField);
        centerPanel.add(addressPanel, BorderLayout.SOUTH);

        contentPane.add(centerPanel, BorderLayout.CENTER);

        // Footer Actions
        JPanel footerPanel = new JPanel(new BorderLayout(10, 0));
        footerPanel.setOpaque(false);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.COLOR_DANGER);
        footerPanel.add(errorLabel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        ModernButton cancelBtn = new ModernButton("Cancel", ModernButton.ButtonStyle.OUTLINE);
        cancelBtn.addActionListener(e -> dispose());

        ModernButton saveBtn = new ModernButton(existingStudent == null ? "Create Student" : "Save Changes", ModernButton.ButtonStyle.PRIMARY);
        saveBtn.addActionListener(e -> onSave());

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        footerPanel.add(btnPanel, BorderLayout.EAST);

        contentPane.add(footerPanel, BorderLayout.SOUTH);
    }

    private <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(Theme.FONT_REGULAR);
        combo.setBackground(Theme.inputBg());
        combo.setForeground(Theme.textPrimary());
        combo.setBorder(BorderFactory.createLineBorder(Theme.border(), 1));
        return combo;
    }

    private JPanel createFieldGroup(String labelText, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(Theme.FONT_SUBHEADER);
        lbl.setForeground(Theme.textSecondary());
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void populateFields(Student s) {
        idField.setText(s.getId());
        idField.setEditable(false);
        firstNameField.setText(s.getFirstName());
        lastNameField.setText(s.getLastName());
        emailField.setText(s.getEmail());
        phoneField.setText(s.getPhone() != null ? s.getPhone() : "");
        dobField.setText(s.getDateOfBirth() != null ? s.getDateOfBirth() : "");
        genderCombo.setSelectedItem(s.getGender());
        deptCombo.setSelectedItem(s.getDepartment());
        
        int yearIdx = Math.max(0, Math.min(3, s.getAcademicYear() - 1));
        yearCombo.setSelectedIndex(yearIdx);

        gpaField.setText(String.format(java.util.Locale.US, "%.2f", s.getGpa()));
        statusCombo.setSelectedItem(s.getStatus());
        enrollDateField.setText(s.getEnrollmentDate() != null ? s.getEnrollmentDate() : "");
        addressField.setText(s.getAddress() != null ? s.getAddress() : "");
    }

    private void onSave() {
        errorLabel.setText(" ");
        String id = idField.getText().trim();
        String fName = firstNameField.getText().trim();
        String lName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String dob = dobField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        Department dept = (Department) deptCombo.getSelectedItem();
        int year = yearCombo.getSelectedIndex() + 1;
        String gpaStr = gpaField.getText().trim();
        StudentStatus status = (StudentStatus) statusCombo.getSelectedItem();
        String enrollDate = enrollDateField.getText().trim();
        String address = addressField.getText().trim();

        if (fName.isEmpty() || lName.isEmpty() || email.isEmpty()) {
            errorLabel.setText("First Name, Last Name, and Email are required fields.");
            return;
        }

        double gpa;
        try {
            gpa = Double.parseDouble(gpaStr);
            if (gpa < 0.0 || gpa > 4.0) {
                errorLabel.setText("GPA must be between 0.00 and 4.00.");
                return;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Please enter a valid numeric GPA (e.g. 3.75).");
            return;
        }

        Student s = new Student(id, fName, lName, email, phone, dob, gender, dept, year, gpa, status, enrollDate, address);

        try {
            if (existingStudent == null) {
                studentService.addStudent(s);
            } else {
                studentService.updateStudent(s);
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            errorLabel.setText("Error: " + ex.getMessage());
        }
    }
}
