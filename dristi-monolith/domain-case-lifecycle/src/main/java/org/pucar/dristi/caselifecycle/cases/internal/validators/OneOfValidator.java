package org.pucar.dristi.caselifecycle.cases.internal.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.pucar.dristi.caselifecycle.cases.internal.annotation.OneOf;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchCriteria;


public class OneOfValidator implements ConstraintValidator<OneOf, CaseSearchCriteria> {


    @Override
    public boolean isValid(CaseSearchCriteria criteria, ConstraintValidatorContext constraintValidatorContext) {
        return (criteria.getCaseId() != null && !criteria.getCaseId().isEmpty()) ||
                (criteria.getFilingNumber() != null && !criteria.getFilingNumber().isEmpty()) ||
                (criteria.getCnrNumber() != null && !criteria.getCnrNumber().isEmpty());
    }
}
