package org.pucar.dristi;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Verifies Spring Modulith module structure.
 * Fails the build if any module crosses another module's `internal` boundary
 * or if a cyclic dependency between modules is introduced.
 */
class ModuleStructureTest {

    @Test
    void verifyModuleStructure() {
        ApplicationModules.of(DristiApplication.class).verify();
    }
}
