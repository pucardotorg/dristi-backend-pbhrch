package org.pucar.dristi.caselifecycle.ordermanagement.internal.config;

import lombok.*;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.util.List;

import org.pucar.dristi.common.models.individual.Individual;
import org.pucar.dristi.common.contract.ordermanagement.Order;
@Component("ordermanagementConfiguration")
@Data
@Import({TracerConfiguration.class})
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Configuration {

    // Filestore Config
    @Value("${dristi.filestore.host}")
    private String fileStoreHost;

    @Value("${dristi.filestore.save.endpoint}")
    private String fileStoreSaveEndPoint;

    // ESign Config (signature dimensions only — host/endpoint reached via EsignApi)
    @Value("${dristi.esign.signature.width:250}")
    private int esignSignatureWidth;

    @Value("${dristi.esign.signature.height:50}")
    private int esignSignatureHeight;

    //Hearing config — URI strings still drive HearingUtil.createOrUpdateHearing
    // discriminator; refactor to enum/method-pointer is a Rule 38 follow-up.
    @Value("${dristi.hearing.host}")
    private String HearingHost;

    @Value("${dristi.hearing.update.endpoint}")
    private String HearingUpdateEndPoint;

        @Value("${dristi.hearing.summary.update.endpoint}")
    private String updateHearingSummaryEndPoint;

    @Value("${dristi.hearing.create.endpoint}")
    private String HearingCreateEndPoint;

    // Inbox Config
    @Value("${dristi.inbox.host}")
    private String inboxHost;

    @Value("${dristi.inbox.index.search.endpoint}")
    private String indexSearchEndPoint;

    // Analytics Config
    @Value("${dristi.analytics.host}")
    private String analyticsHost;

    @Value("${dristi.analytics.create.pendingtask}")
    private String createPendingTaskEndPoint;

    // Scheduler Config
    @Value("${dristi.scheduler.host}")
    private String schedulerHost;

    @Value("${dristi.scheduler.reschedule.endpoint}")
    private String rescheduleEndPoint;

    // Individual Config

    @Value("${dristi.individual.host}")
    private String individualHost;

    @Value("${dristi.individual.search.endpoint}")
    private String individualSearchEndPoint;


    @Value("${spring.redis.timeout}")
    private Long redisTimeout;

    //SMSNotification
    @Value("${dristi.sms.notification.topic}")
    private String smsNotificationTopic;

    // zone id
    @Value("${app.zone.id}")
    private String zoneId;

    @Value("${file.max.size}")
    private long maxFileSize;

    @Value("${allowed.content.types}")
    private String[] allowedContentTypes;

    //Localization
    @Value("${egov.localization.host}")
    private String localizationHost;

    @Value("${egov.localization.search.endpoint}")
    private String localizationSearchEndpoint;

    @Value("${egov.sms.notification.process.fee.payment.template.id}")
    private String smsNotificationProcessFeePaymentTemplateId;

    @Value("${egov.sms.notification.rpad.submission.template.id}")
    private String smsNotificationRpadSubmissionTemplateId;

    @Value("${egov.sms.notification.process.fee.payment.pending.template.id}")
    private String smsNotificationProcessFeePaymentPendingTemplateId;

    @Value("${egov.sms.notification.rpad.submission.pending.template.id}")
    private String smsNotificationRpadSubmissionPendingTemplateId;

    @Value("${egov.sms.notification.payment.link.template.id}")
    private String smsNotificationPaymentLinkTemplateId;

    // Tenant Id
    @Value("${egov.statelevel.tenantId}")
    private  String stateLevelTenantId;


    @Value("${task.upfront.create.topic}")
    private String taskUpFrontCreateTopic;

    @Value("${task.management.action.category}")
    private String taskManagementActionCategory;

    @Value("${task.management.assigned.role}")
    private List<String> taskManagementAssignedRole;

    //Elastic search
    @Value("${egov.indexer.es.username}")
    private String esUsername;

    @Value("${egov.indexer.es.password}")
    private String esPassword;

    @Value("${egov.bulk.index}")
    private String index;

    @Value("${egov.infra.indexer.host}")
    private String esHostUrl;

    @Value("${egov.bulk.index.path}")
    private String bulkPath;

    @Value("${dristi.nature.of.complainant}")
    private String natureOfComplainant;

    //PDF Service Config
    @Value("${egov.pdf.service.host}")
    private String pdfServiceHost;

    @Value("${egov.pdf.service.create.endpoint}")
    private String pdfServiceEndpoint;

    @Value("${egov.pdf.digitisation.mediation.template.key}")
    private String pdfDigitisationMediationTemplateKey;

    @Value("${dristi.court.name}")
    private String courtName;

    @Value("${dristi.place}")
    private String place;

    @Value("${dristi.state}")
    private String state;

    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.v2.search.endpoint}")
    private String mdmsV2EndPoint;

    @Value("${sla.envelope.sla.value}")
    private Long envelopeSlaValue;
}
