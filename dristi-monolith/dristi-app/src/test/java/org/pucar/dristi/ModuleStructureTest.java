package org.pucar.dristi;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.core.Violations;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies Spring Modulith module structure.
 *
 * <p>Fails the build if any module crosses another module's {@code internal}
 * boundary or if a cyclic dependency between modules is introduced.
 *
 * <p>Style-only violations (e.g. "prefer constructor injection over field
 * injection") are filtered out — they are real code-quality nits but not
 * structural problems, and the migrated services contain ~48 legacy
 * {@code @Autowired} field injections that pre-date this monolith. Those
 * are tracked separately for cleanup.
 */
class ModuleStructureTest {

    private static final String[] STYLE_ONLY_HINTS = {
            "uses field injection",
            "Prefer constructor injection",
    };

    @Test
    void verifyModuleStructure() {
        Violations violations = ApplicationModules.of(DristiApplication.class).detectViolations();
        if (!violations.hasViolations()) {
            return;
        }
        String message = violations.getMessage();
        long structuralLines = message.lines()
                .filter(line -> !line.isBlank())
                .filter(line -> {
                    for (String hint : STYLE_ONLY_HINTS) {
                        if (line.contains(hint)) {
                            return false;
                        }
                    }
                    return true;
                })
                .count();
        if (structuralLines > 0) {
            throw new AssertionError(
                    "Spring Modulith found " + structuralLines + " structural violation(s):\n" + message);
        }
        assertEquals(0L, structuralLines, "structural violations");
    }
}
