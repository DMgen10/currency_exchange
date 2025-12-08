package util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class DatabaseInitializer implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            InitDatabase.initialize();
            log.info("Database initialized successfully on application startup");
        } catch (Exception exception) {
            log.error("Failed to initialize database on application startup", exception);
            throw new RuntimeException("Database initialization failed", exception);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Application context destroyed");
    }
}