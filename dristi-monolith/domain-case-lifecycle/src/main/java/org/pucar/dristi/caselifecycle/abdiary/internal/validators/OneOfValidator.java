package org.pucar.dristi.caselifecycle.abdiary.internal.validators;

import org.pucar.dristi.caselifecycle.abdiary.internal.annotation.OneOf;
import org.pucar.dristi.common.contract.abdiary.CaseDiarySearchCriteria;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OneOfValidator implements ConstraintValidator<OneOf, CaseDiarySearchCriteria> {

    @Override
    public boolean isValid(CaseDiarySearchCriteria searchCriteria, ConstraintValidatorContext constraintValidatorContext) {
        return ((searchCriteria.getDate() != null) || (searchCriteria.getCaseId() != null));
    }

}
