package com.sms.ui.dialogs;

import com.sms.exception.DuplicateStudentException;
import com.sms.exception.StorageException;
import com.sms.exception.StudentNotFoundException;
import com.sms.exception.ValidationException;
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
 * Modal dialog for creating and editing student records with multi-tier validation
 * and custom domain exception handling.
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
        JLabel titleLabel = new JLabel(existingStudent == null ? "Register New Student" : "Edit Student Information");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.textPrimary());

        JLabel subtitleLabel = new JLabel(existingStudent == null ?
                "Fill in the academic and demographic details to create a record." :
                "Update academic standing, contact details, or status for ID: " + existingStudent.getId());
        subtitleLabel.setFont(Theme.FONT_SMALL);
        subtitleLabel.setForeground(Theme.textSecondary());

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Form Fields Grid
        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 0.5;

        // ID & Status
        idField = new ModernTextField("Student ID (e.g. STU-1001)");
        statusCombo = new JComboBox<>(StudentStatus.values());
        statusCombo.setFont(Theme.FONT_BODY);

        addFormField(formGrid, gbc, 0, 0, "Student ID", idField);
        addFormField(formGrid, gbc, 1, 0, "Enrollment Status", statusCombo);

        // First & Last Name
        firstNameField = new ModernTextField("First Name");
        lastNameField = new ModernTextField("Last Name");
        addFormField(formGrid, gbc, 0, 1, "First Name *", firstNameField);
        addFormField(formGrid, gbc, 1, 1, "Last Name *", lastNameField);

        // Email & Phone
        emailField = new ModernTextField("university.email@university.edu");
        phoneField = new ModernTextField("+1 (555) 000-0000");
        addFormField(formGrid, gbc, 0, 2, "Email Address *", emailField);
        addFormField(formGrid, gbc, 1, 2, "Phone Number", phoneField);

        // Date of Birth & Gender
        dobField = new ModernTextField("YYYY-MM-DD");
        genderCombo = new JComboBox<>(new String[]{"Female", "Male", "Other"});
        genderCombo.setFont(Theme.FONT_BODY);
        addFormField(formGrid, gbc, 0, 3, "Date of Birth (YYYY-MM-DD)", dobField);
        addFormField(formGrid, gbc, 1, 3, "Gender", genderCombo);

        // Department & Year
        deptCombo = new JComboBox<>(Department.values());
        deptCombo.setFont(Theme.FONT_BODY);
        yearCombo = new JComboBox<>(new String[]{"Year 1 (Freshman)", "Year 2 (Sophomore)", "Year 3 (Junior)", "Year 4 (Senior)"});
        yearCombo.setFont(Theme.FONT_BODY);
        addFormField(formGrid, gbc, 0, 4, "Academic Department *", deptCombo);
        addFormField(formGrid, gbc, 1, 4, "Academic Year *", yearCombo);

        // GPA & Enrollment Date
        gpaField = new ModernTextField("0.00 - 4.00");
        enrollDateField = new ModernTextField("YYYY-MM-DD");
        addFormField(formGrid, gbc, 0, 5, "Cumulative GPA (0.00 - 4.00) *", gpaField);
        addFormField(formGrid, gbc, 1, 5, "Enrollment Date", enrollDateField);

        // Address (Full width)
        addressField = new ModernTextField("Campus / Residential Address");
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        JPanel addrPanel = new JPanel(new BorderLayout(0, 4));
        addrPanel.setOpaque(false);
        JLabel addrLbl = new JLabel("Home Address");
        addrLbl.setFont(Theme.FONT_SMALL);
        addrLbl.setForeground(Theme.textSecondary());
        addrPanel.add(addrLbl, BorderLayout.NORTH);
        addrPanel.add(addressField, BorderLayout.CENTER);
        formGrid.add(addrPanel, gbc);

        contentPane.add(formGrid, BorderLayout.CENTER);

        // Bottom Action Controls
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.COLOR_DANGER);
        bottomPanel.add(errorLabel, BorderLayout.NORTH);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btnBar.setOpaque(false);

        ModernButton cancelBtn = new ModernButton("Cancel", ModernButton.ButtonVariant.OUTLINE);
        cancelBtn.addActionListener(e -> dispose());

        ModernButton saveBtn = new ModernButton(existingStudent == null ? "Save Student" : "Update Profile", ModernButton.ButtonVariant.PRIMARY);
        saveBtn.addActionListener(e -> onSave());

        btnBar.add(cancelBtn);
        btnBar.add(saveBtn);
        bottomPanel.add(btnBar, BorderLayout.SOUTH);

        contentPane.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel parent, GridBagConstraints gbc, int col, int row, String label, JComponent comp) {
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.gridwidth = 1;

        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.textSecondary());
        p.add(lbl, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);

        parent.add(p, gbc);
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
        } catch (ValidationException ex) {
            errorLabel.setText("Validation: " + ex.getFirstError());
        } catch (DuplicateStudentException ex) {
            errorLabel.setText("Duplicate " + ex.getConflictField() + ": " + ex.getMessage());
        } catch (StudentNotFoundException ex) {
            errorLabel.setText("Not Found: " + ex.getMessage());
        } catch (StorageException ex) {
            errorLabel.setText("Storage Error: " + ex.getMessage());
        } catch (Exception ex) {
            errorLabel.setText("Error: " + ex.getMessage());
        }
    }
}
