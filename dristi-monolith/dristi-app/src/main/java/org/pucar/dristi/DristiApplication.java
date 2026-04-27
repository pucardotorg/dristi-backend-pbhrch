package org.pucar.dristi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

/**
 * Single entry-point for the DRISTI modular monolith.
 * <p>
 * The {@link Modulithic} annotation declares the application as a Spring Modulith
 * project so that module boundaries can be verified at build/test time.
 */
@SpringBootApplication
@Modulithic(systemName = "dristi", sharedModules = "common")
public class DristiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DristiApplication.class, args);
    }
}
