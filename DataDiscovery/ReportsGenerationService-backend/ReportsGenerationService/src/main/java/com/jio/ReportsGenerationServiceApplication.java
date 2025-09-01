package com.jio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point for the Reports Generation Service Spring Boot application.
 * <p>
 * This class extends {@link SpringBootServletInitializer} to allow deployment
 * as a traditional WAR on external servlet containers (e.g., Tomcat).
 * It also includes a main method to support running the application as a standalone JAR.
 * </p>
 *
 * @author Ganesh Songala
 * @since 2025-07-17
 */
@SpringBootApplication
public class ReportsGenerationServiceApplication extends SpringBootServletInitializer {

    /**
     * Configures the application when deployed to an external servlet container.
     *
     * @param builder the {@link SpringApplicationBuilder} used to configure the application
     * @return the configured {@code SpringApplicationBuilder}
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ReportsGenerationServiceApplication.class);
    }

    /**
     * Main method used to launch the Spring Boot application from the command line or an IDE.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(ReportsGenerationServiceApplication.class, args);
    }
}
