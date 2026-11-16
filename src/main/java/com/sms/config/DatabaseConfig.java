package com.sms.config;

/**
 * Encapsulates database connection properties for enterprise database integration.
 */
public class DatabaseConfig {
    private String jdbcUrl = "jdbc:sqlite:data/students.db";
    private String username = "";
    private String password = "";
    private String driverClass = "org.sqlite.JDBC";
    private int connectionTimeoutSeconds = 5;

    public DatabaseConfig() {}

    public DatabaseConfig(String jdbcUrl, String username, String password, String driverClass) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.driverClass = driverClass;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public int getConnectionTimeoutSeconds() {
        return connectionTimeoutSeconds;
    }

    public void setConnectionTimeoutSeconds(int connectionTimeoutSeconds) {
        this.connectionTimeoutSeconds = connectionTimeoutSeconds;
    }
}
