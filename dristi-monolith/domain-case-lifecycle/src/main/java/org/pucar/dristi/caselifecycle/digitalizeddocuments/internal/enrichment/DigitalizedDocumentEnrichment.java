package org.pucar.dristi.caselifecycle.digitalizeddocuments.internal.enrichment;

import org.pucar.dristi.caselifecycle.digitalizeddocuments.internal.config.Configuration;
import org.pucar.dristi.common.util.IdgenUtil;
import org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocument;
import org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocumentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import org.pucar.dristi.common.models.Document;
@Component
@Slf4j
public class DigitalizedDocumentEnrichment {

    private final Configuration configuration;

    private final IdgenUtil idgenUtil;

    public DigitalizedDocumentEnrichment(Configuration configuration, IdgenUtil idgenUtil) {
        this.configuration = configuration;
        this.idgenUtil = idgenUtil;
    }

    public void enrichDigitalizedDocument(DigitalizedDocumentRequest digitalizedDocumentRequest) {

        DigitalizedDocument digitalizedDocument = digitalizedDocumentRequest.getDigitalizedDocument();

        digitalizedDocument.setId(String.valueOf(UUID.randomUUID()));

        String idName = configuration.getDigitalizedDocumentIdGenConfig();
        String idFormat = configuration.getDigitalizedDocumentIdGenFormat();
        String tenantId = digitalizedDocumentRequest.getDigitalizedDocument().getCaseFilingNumber().replace("-","");

        List<String> idList = idgenUtil.getIdList(digitalizedDocumentRequest.getRequestInfo(), tenantId, idName, idFormat, 1, false);
        log.info("Digitalized Document ID List: {}", idList);

        String documentNumber = digitalizedDocument.getCaseFilingNumber() + "-" + idList.get(0);
        digitalizedDocument.setDocumentNumber(documentNumber);

    }

}
