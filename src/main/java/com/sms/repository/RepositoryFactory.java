package com.sms.repository;

import com.sms.config.AppConfig;
import com.sms.repository.impl.DatabaseStudentRepository;
import com.sms.repository.impl.JsonStudentRepository;

import java.util.logging.Logger;

/**
 * Enterprise repository factory for switching between File and Database persistence.
 */
public class RepositoryFactory {
    private static final Logger LOGGER = Logger.getLogger(RepositoryFactory.class.getName());

    public static StudentRepository createRepository() {
        AppConfig config = AppConfig.getInstance();
        if (config.getStorageType() == AppConfig.StorageType.DATABASE) {
            LOGGER.info("Configured storage type: DATABASE. Initializing DatabaseStudentRepository...");
            try {
                return new DatabaseStudentRepository();
            } catch (Exception e) {
                LOGGER.warning("Database initialization failed: " + e.getMessage() + ". Falling back to JSON file repository.");
                return new JsonStudentRepository(config.getDataDirectory());
            }
        } else {
            LOGGER.info("Configured storage type: FILE_JSON. Initializing JsonStudentRepository...");
            return new JsonStudentRepository(config.getDataDirectory());
        }
    }

    public static StudentRepository createJsonRepository(String dataDirectory) {
        return new JsonStudentRepository(dataDirectory);
    }
}
