package com.sms.config;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized enterprise configuration management for storage type, directories,
 * database connectivity, and concurrency settings.
 */
public class AppConfig {
    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final AppConfig INSTANCE = new AppConfig();

    public enum StorageType {
        FILE_JSON,
        DATABASE
    }

    private StorageType storageType = StorageType.FILE_JSON;
    private String dataDirectory = "data";
    private int threadPoolSize = Math.max(4, Runtime.getRuntime().availableProcessors());
    private boolean autoSaveEnabled = true;
    private int autoSaveIntervalSeconds = 60;
    private DatabaseConfig databaseConfig = new DatabaseConfig();

    private AppConfig() {
        loadProperties();
    }

    public static AppConfig getInstance() {
        return INSTANCE;
    }

    public void loadProperties() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                
                String typeStr = props.getProperty("app.storage.type", "FILE_JSON");
                try {
                    this.storageType = StorageType.valueOf(typeStr.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    LOGGER.log(Level.WARNING, "Unknown storage type {0}, defaulting to FILE_JSON", typeStr);
                }

                this.dataDirectory = props.getProperty("app.data.dir", "data");
                this.threadPoolSize = Integer.parseInt(props.getProperty("app.concurrency.pool-size", 
                        String.valueOf(this.threadPoolSize)));
                this.autoSaveEnabled = Boolean.parseBoolean(props.getProperty("app.autosave.enabled", "true"));
                this.autoSaveIntervalSeconds = Integer.parseInt(props.getProperty("app.autosave.interval-seconds", "60"));

                databaseConfig.setJdbcUrl(props.getProperty("db.url", "jdbc:sqlite:data/students.db"));
                databaseConfig.setUsername(props.getProperty("db.username", "sa"));
                databaseConfig.setPassword(props.getProperty("db.password", ""));
                databaseConfig.setDriverClass(props.getProperty("db.driver-class", "org.sqlite.JDBC"));
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Using default config: {0}", e.getMessage());
        }

        // Ensure data directory exists on disk
        File dir = new File(dataDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public Path getDataPath() {
        return Paths.get(dataDirectory);
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public boolean isAutoSaveEnabled() {
        return autoSaveEnabled;
    }

    public int getAutoSaveIntervalSeconds() {
        return autoSaveIntervalSeconds;
    }

    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }
}
