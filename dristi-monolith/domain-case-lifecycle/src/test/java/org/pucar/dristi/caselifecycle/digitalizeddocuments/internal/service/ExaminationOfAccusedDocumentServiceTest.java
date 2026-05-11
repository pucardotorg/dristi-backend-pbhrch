package org.pucar.dristi.caselifecycle.digitalizeddocuments.internal.service;

import org.pucar.dristi.caselifecycle.digitalizeddocuments.internal.config.Configuration;
import org.pucar.dristi.caselifecycle.digitalizeddocuments.internal.enrichment.DigitalizedDocumentEnrichment;
import org.pucar.dristi.caselifecycle.digitalizeddocuments.internal.enrichment.ExaminationOfAccusedEnrichment;
import org.pucar.dristi.common.kafka.Producer;
import org.pucar.dristi.common.util.FileStoreUtil;
import org.pucar.dristi.caselifecycle.digitalizeddocuments.internal.validators.ExaminationOfAccusedValidator;
import org.pucar.dristi.caselifecycle.digitalizeddocuments.internal.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.pucar.dristi.common.models.workflow.WorkflowObject;
import org.pucar.dristi.common.models.Document;
class ExaminationOfAccusedDocumentServiceTest {

    @Mock private ExaminationOfAccusedValidator validator;
    @Mock private DigitalizedDocumentEnrichment enrichment;
    @Mock private ExaminationOfAccusedEnrichment eoaaEnrichment;
    @Mock private WorkflowService workflowService;
    @Mock private Producer producer;
    @Mock private Configuration config;
    @Mock private FileStoreUtil fileStoreUtil;

    @InjectMocks private ExaminationOfAccusedDocumentService service;

    private DigitalizedDocumentRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        WorkflowObject workflowObject = new WorkflowObject();
        workflowObject.setAction("CREATE");
        DigitalizedDocument document = DigitalizedDocument.builder()
                .type(TypeEnum.EXAMINATION_OF_ACCUSED)
                .caseId("C1")
                .caseFilingNumber("CF1")
                .tenantId("t1")
                .courtId("court")
                .workflow(workflowObject)
                .build();
        request = DigitalizedDocumentRequest.builder()
                .requestInfo(RequestInfo.builder().userInfo(User.builder().uuid("u1").build()).build())
                .digitalizedDocument(document)
                .build();
    }

    @Test
    void createDocument_Success_FlowsAndPush() {
        when(config.getExaminationOfAccusedCreateTopic()).thenReturn("topic.create");

        DigitalizedDocument result = service.createDocument(request);

        assertNotNull(result);
        verify(validator).validateDigitalizedDocument(eq(request));
        verify(enrichment).enrichDigitalizedDocument(eq(request));
        verify(eoaaEnrichment).enrichDocumentOnCreation(eq(request));
        verify(workflowService).updateWorkflowStatus(eq(request));
        verify(producer).push(eq("topic.create"), eq(request));
    }

    @Test
    void updateDocument_DeletesRemovedFilesAndPushes() {
        // existing doc contains A and B
        DigitalizedDocument existing = DigitalizedDocument.builder()
                .documents(List.of(
                        Document.builder().fileStore("A").build(),
                        Document.builder().fileStore("B").build()
                ))
                .build();
        when(validator.checkDigitalizedDocumentExists(any(DigitalizedDocument.class))).thenReturn(existing);
        when(config.getExaminationOfAccusedUpdateTopic()).thenReturn("topic.update");

        // updated doc has B and C => delete A
        DigitalizedDocument updated = request.getDigitalizedDocument();
        updated.setDocuments(new ArrayList<>());
        updated.getDocuments().add(Document.builder().fileStore("B").build());
        updated.getDocuments().add(Document.builder().fileStore("C").build());

        DigitalizedDocument result = service.updateDocument(request);

        assertNotNull(result);
        verify(validator).validateDigitalizedDocument(eq(request));
        verify(eoaaEnrichment, times(1)).enrichDocumentOnUpdate(eq(request));
        verify(workflowService).updateWorkflowStatus(eq(request));
        verify(fileStoreUtil).deleteFilesByFileStore(eq(List.of("A")), eq("t1"));
        verify(producer).push(eq("topic.update"), eq(request));
    }
}
