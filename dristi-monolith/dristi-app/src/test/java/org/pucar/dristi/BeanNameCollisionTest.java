package org.pucar.dristi;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Catches Rule 41 violations (subdomain Spring-managed classes whose
 * simple name repeats across subdomains without an explicit
 * {@code @Component("...")} qualifier) without booting a Spring
 * context.
 *
 * <p>The motivating failure: at {@code DristiApplication} startup,
 * Spring's {@code ConfigurationClassPostProcessor} calls
 * {@code ClassPathBeanDefinitionScanner.checkCandidate} which throws
 * {@code ConflictingBeanDefinitionException} the moment two stereotype-
 * annotated classes resolve to the same default bean name. Booting the
 * full context to detect that needs Postgres / Kafka / MDMS, which
 * isn't viable in unit tests. This test reaches the same conclusion
 * by re-running the bean-name computation against the classpath.
 *
 * <p>If this test fails, fix per Rule 41: qualify each colliding
 * class's annotation with an explicit name, e.g.
 * {@code @Component("calculatorTaskUtil")}.
 */
class BeanNameCollisionTest {

    private static final String BASE_PACKAGE = "org.pucar.dristi";

    @Test
    void noTwoSpringManagedClassesShareABeanName() {
        // Default filters include @Component (and meta-annotations
        // @Service, @Repository, @Controller, @RestController,
        // @Configuration). Don't include filterless candidates.
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(true);

        Set<BeanDefinition> candidates = scanner.findCandidateComponents(BASE_PACKAGE);

        AnnotationBeanNameGenerator nameGenerator = new AnnotationBeanNameGenerator();
        DefaultListableBeanFactory dummyRegistry = new DefaultListableBeanFactory();

        Map<String, List<String>> byBeanName = candidates.stream()
                .collect(Collectors.groupingBy(
                        bd -> nameGenerator.generateBeanName(bd, dummyRegistry),
                        TreeMap::new,
                        Collectors.mapping(BeanDefinition::getBeanClassName, Collectors.toList())));

        Map<String, List<String>> collisions = byBeanName.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, TreeMap::new));

        assertTrue(collisions.isEmpty(),
                "Bean-name collisions detected — would fail DristiApplication boot. "
                        + "Qualify each class's stereotype annotation per Rule 41:\n"
                        + collisions.entrySet().stream()
                                .map(e -> "  '" + e.getKey() + "' is generated for "
                                        + e.getValue().size() + " classes:\n    "
                                        + String.join("\n    ", e.getValue()))
                                .collect(Collectors.joining("\n")));
    }
}
