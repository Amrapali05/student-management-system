# EduCore - Modern Student Management System (Java 21)

A feature-rich, desktop-based **Student Management System** developed in pure **Java 21 LTS** with modern UI aesthetics, real-time analytics, persistent file storage, and zero external runtime dependencies.

---

## 🌟 Key Features

### 1. Modern Desktop GUI
- **Dynamic Theming:** Seamless toggle between **Dark Mode** (Deep slate `#0F172A` / `#1E293B`) and **Light Mode** (`#F8FAFC` / `#FFFFFF`).
- **Typography & Anti-Aliasing:** High-DPI font rendering using modern sans-serif typography (`Segoe UI`).
- **Custom UI Components:**
  - **KPI Metric Cards:** Real-time summary cards for Total Students, Active Rate, Average GPA, and Top Department.
  - **Status Pill Badges:** Visual indicator pills with colored status dots for `Active`, `Inactive`, `Suspended`, and `Graduated`.
  - **GPA Progress Badges:** Inline numeric grade with proportional color-coded progress bars.
  - **Modern Table:** Alternating striped rows, custom headers, cell padding, and row selection highlighting.

### 2. Comprehensive Student Administration (CRUD)
- **Create:** Register new students with automatic ID generation (`STU-1001`, `STU-1002`, etc.) and validation.
- **Read & View:** Interactive list and detailed modal profile card with student avatar, academic standing, and contact info.
- **Update:** Edit student demographics, majors, academic year, and cumulative GPA.
- **Delete:** Safe removal with confirmation safeguards.

### 3. Real-Time Search & Multi-Criteria Filtering
- **Live Search:** Instant filtering across Name, Student ID, Email, Phone, and Address as you type.
- **Department Filter:** Filter by major (Computer Science, Data Science, Electrical, Mechanical, Business, Biomedical, Civil).
- **Status Filter:** Filter by enrollment status (Active, Inactive, Suspended, Graduated).
- **Year Filter:** Filter by academic level (Freshman, Sophomore, Junior, Senior).
- **Reset Filters:** One-click clear to restore full directory view.

### 4. Interactive Java 2D Analytics
- **Department Donut Chart:** Visualizes distribution across academic majors with custom color palette and percentages.
- **GPA Distribution Bar Chart:** Visualizes student distribution across performance tiers:
  - `3.5 - 4.0` (Honor / Dean's List)
  - `3.0 - 3.4` (Good Standing)
  - `2.0 - 2.9` (Satisfactory)
  - `< 2.0` (Academic Probation)

### 5. Persistent Storage & Interoperability
- **JSON File Persistence:** Automatically saves all records to `data/students.json` with zero third-party dependencies.
- **Pre-populated Dataset:** Ships with 12 diverse sample student records across various disciplines.
- **CSV Import & Export:** Export filtered or full directories to CSV, and import existing institutional rosters.
- **Reset Option:** Easily restore the sample database from the top navigation bar.

---

## 📂 Project Structure

```
student-management-system/
├── run.bat                         # Double-click to compile and run on Windows
├── build.bat                       # Packages standalone executable JAR
├── test.bat                        # Runs automated verification tests
├── run.ps1                         # PowerShell runner script
├── student-management-system.jar   # Pre-built executable JAR (80 KB)
├── README.md                       # Complete documentation
├── data/
│   └── students.json               # Auto-saved JSON database file
├── bin/                            # Compiled Java bytecode
└── src/
    └── com/
        └── sms/
            ├── Main.java           # Application entry point & Look & Feel
            ├── model/
            │   ├── Student.java          # Core entity model
            │   ├── Department.java       # Academic departments enum
            │   └── StudentStatus.java    # Enrollment status enum
            ├── repository/
            │   ├── StudentRepository.java # Thread-safe data repository
            │   ├── JsonStorage.java       # Zero-dependency JSON engine
            │   └── CsvExporter.java       # CSV export and import engine
            ├── service/
            │   ├── StudentService.java    # Business logic & validations
            │   └── StudentStatistics.java # Aggregated analytical calculations
            ├── ui/
            │   ├── Theme.java             # Color palettes, typography & themes
            │   ├── MainFrame.java         # Main dashboard window
            │   ├── components/
            │   │   ├── ModernButton.java      # Rounded styled buttons
            │   │   ├── ModernTextField.java   # Rounded text input with glow
            │   │   ├── ModernTable.java       # Custom styled JTable
            │   │   ├── StatCard.java          # KPI metric card component
            │   │   ├── StatusBadgeRenderer.java # Status pill renderer
            │   │   ├── GpaBadgeRenderer.java    # GPA inline progress bar
            │   │   ├── DepartmentPieChart.java  # Java 2D Donut Chart
            │   │   └── GpaBarChart.java         # Java 2D Bar Chart
            │   └── dialogs/
            │       ├── StudentFormDialog.java   # Add / Edit modal
            │       └── StudentDetailDialog.java # Student profile view modal
            └── test/
                └── StudentSystemTest.java       # Automated test suite
```

---

## 🚀 How to Run the Project

### Option 1: Double-Click (Easiest)
Simply double-click **`run.bat`** in the project folder. It will compile any changed files and launch the GUI immediately.

### Option 2: Run the Executable JAR
```bash
java -jar student-management-system.jar
```

### Option 3: PowerShell
```powershell
.\run.ps1
```

### Option 4: Manual Command Line
```bash
# Compile
javac -encoding UTF-8 -d bin src\com\sms\model\*.java src\com\sms\repository\*.java src\com\sms\service\*.java src\com\sms\ui\components\*.java src\com\sms\ui\dialogs\*.java src\com\sms\ui\*.java src\com\sms\Main.java

# Launch
java -cp bin com.sms.Main
```

---

## 🧪 Running Automated Tests

Run the built-in test suite:
```bash
test.bat
# or
java -cp bin com.sms.test.StudentSystemTest
```

The test suite validates:
1. Repository initialization and auto-population.
2. JSON persistence round-trip serialization.
3. Student registration and unique ID generation.
4. Input validation (email patterns, GPA ranges 0.00-4.00, year constraints).
5. Search filtering by keywords.
6. Department and status filtering.
7. Real-time statistical analytics computation.
8. CSV export and import integrity.
9. Deletion and cache synchronization.

---

## ⚙ System Requirements
- **Java:** JDK 21 or higher
- **Operating System:** Windows, macOS, or Linux
- **Dependencies:** None (100% Pure Standard Java SE)
