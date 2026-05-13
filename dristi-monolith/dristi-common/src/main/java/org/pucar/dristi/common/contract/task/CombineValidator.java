// HAND-CURATED — lifted by C2 alongside CombineRequiredFields.
package org.pucar.dristi.common.contract.task;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public class CombineValidator implements ConstraintValidator<CombineRequiredFields, Object> {
    private String[] fields;

    @Override
    public void initialize(CombineRequiredFields constraintAnnotation) {
        this.fields = constraintAnnotation.fields();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (fields == null || fields.length == 0) {
            return true;
        }
        try {
            boolean anyFieldPresent = false;
            boolean allFieldsPresent = true;
            for (String fieldName : fields) {
                Field field = object.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(object);
                if (value != null) {
                    anyFieldPresent = true;
                } else {
                    allFieldsPresent = false;
                }
            }
            return !anyFieldPresent || allFieldsPresent;
        } catch (Exception e) {
            log.info("Something went wrong at validation");
            return false;
        }
    }
}
