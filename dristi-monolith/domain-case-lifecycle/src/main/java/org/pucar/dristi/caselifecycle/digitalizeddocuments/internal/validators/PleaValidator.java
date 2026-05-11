package org.pucar.dristi.caselifecycle.digitalizeddocuments.internal.validators;

import org.pucar.dristi.caselifecycle.digitalizeddocuments.internal.repository.DigitalizedDocumentRepository;
import org.pucar.dristi.caselifecycle.digitalizeddocuments.internal.web.models.DigitalizedDocument;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import static org.pucar.dristi.caselifecycle.digitalizeddocuments.internal.config.ServiceConstants.VALIDATION_ERROR;


@Component
@Slf4j
public class PleaValidator {

    private final DigitalizedDocumentRepository digitalizedDocumentRepository;

    public PleaValidator(DigitalizedDocumentRepository digitalizedDocumentRepository) {
        this.digitalizedDocumentRepository = digitalizedDocumentRepository;
    }

    public DigitalizedDocument validateDigitalizedDocument(DigitalizedDocument document) {
        String documentNumber = document.getDocumentNumber();
        DigitalizedDocument existingDocument = digitalizedDocumentRepository.getDigitalizedDocumentByDocumentNumber(documentNumber, document.getTenantId());

        if(existingDocument == null){
            throw new CustomException(VALIDATION_ERROR, "Digitalized document with document number " + documentNumber + " does not exist");
        }

        return existingDocument;
    }

}