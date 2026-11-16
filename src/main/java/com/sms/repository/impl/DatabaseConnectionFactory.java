package com.sms.repository.impl;

import com.sms.config.AppConfig;
import com.sms.config.DatabaseConfig;
import com.sms.exception.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enterprise database connection factory and schema bootstrapper.
 * Connects to JDBC-compliant relational databases (SQLite, MySQL, PostgreSQL, H2).
 */
public class DatabaseConnectionFactory {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnectionFactory.class.getName());

    private static final String CREATE_TABLE_SQL = 
            "CREATE TABLE IF NOT EXISTS students (" +
            "  id VARCHAR(20) PRIMARY KEY," +
            "  first_name VARCHAR(50) NOT NULL," +
            "  last_name VARCHAR(50) NOT NULL," +
            "  email VARCHAR(100) NOT NULL UNIQUE," +
            "  phone VARCHAR(30)," +
            "  date_of_birth VARCHAR(20)," +
            "  gender VARCHAR(20)," +
            "  department VARCHAR(50) NOT NULL," +
            "  academic_year INT NOT NULL," +
            "  gpa DOUBLE NOT NULL," +
            "  status VARCHAR(30) NOT NULL," +
            "  enrollment_date VARCHAR(20)," +
            "  address VARCHAR(255)" +
            ");";

    private static final String CREATE_EMAIL_INDEX_SQL = 
            "CREATE UNIQUE INDEX IF NOT EXISTS idx_student_email ON students(email);";

    static {
        // Attempt driver loading
        DatabaseConfig config = AppConfig.getInstance().getDatabaseConfig();
        try {
            Class.forName(config.getDriverClass());
            LOGGER.info("Loaded JDBC driver: " + config.getDriverClass());
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.FINE, "JDBC driver class {0} not in classpath yet: {1}", 
                    new Object[]{config.getDriverClass(), e.getMessage()});
        }
    }

    public static Connection getConnection() throws SQLException {
        DatabaseConfig config = AppConfig.getInstance().getDatabaseConfig();
        if (config.getUsername() != null && !config.getUsername().isEmpty()) {
            return DriverManager.getConnection(config.getJdbcUrl(), config.getUsername(), config.getPassword());
        }
        return DriverManager.getConnection(config.getJdbcUrl());
    }

    public static void initializeSchema() throws DataAccessException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_TABLE_SQL);
            try {
                stmt.execute(CREATE_EMAIL_INDEX_SQL);
            } catch (SQLException ignored) {
                // Some databases do not support IF NOT EXISTS on indexes
            }
            LOGGER.info("Database schema verified and initialized.");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to initialize database schema: " + e.getMessage(), e);
        }
    }
}
