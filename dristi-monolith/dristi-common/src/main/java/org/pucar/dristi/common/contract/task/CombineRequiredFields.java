// HAND-CURATED — lifted by C2 to back contract DTOs (TaskExists, TaskCriteria) that were
// Phase-35-moved to dristi-common but kept stale imports into task internals.
package org.pucar.dristi.common.contract.task;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CombineValidator.class)
@Documented
public @interface CombineRequiredFields {

    String message() default "combination of field is required";

    Class<?>[] groups() default {};

    String[] fields() default {};

    Class<? extends Payload>[] payload() default {};
}
