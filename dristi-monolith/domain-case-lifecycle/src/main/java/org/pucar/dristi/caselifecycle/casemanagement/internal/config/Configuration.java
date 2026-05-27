package org.pucar.dristi.caselifecycle.casemanagement.internal.config;

import jakarta.annotation.PostConstruct;
import lombok.*;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pucar.dristi.common.contract.casemanagement.Task;
import org.pucar.dristi.common.contract.casemanagement.Application;
@Component("casemanagementConfiguration")
@Data
@Import({TracerConfiguration.class})
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Configuration {


	@Value("${spring.data.redis.timeout}")
	private Long redisTimeout;

	//MDMS
	@Value("${egov.mdms.host}")
	private String mdmsHost;

	@Value("${egov.mdms.v2.search.endpoint}")
	private String mdmsEndPoint;

	@Value("${egov.mdms.schema.search.endpoint}")
	private String mdmsSchemaEndPoint;

	@Value("${schemacode.state.master}")
	private String stateMasterSchema;

	@Value("${schemacode.casebundle.section.order}")
	private String caseBundleSectionOrderSchema;

	@Value("${schemacode.casebundle.master}")
	private String caseBundleMasterSchema;


	@Value("${egov.filestore.host}")
	private String fileStoreHost;

	@Value("${dristi.file.search.path}")
	private String fileStorePath;

	@Value("${egov.pdf.create}")
	private String generatePdfUrl;

	@Value("${egov.pdf.host}")
	private String generatePdfHost;

	@Value("${egov.credential.host}")
	private String credentialHost;

	@Value("${egov.credential.url}")
	private String credentialUrl;

	@Value("${egov.dristi.pdf.host}")
	private String caseBundlePdfHost;

	@Value("${egov.dristi.pdf.bundle}")
	private String caseBundlePdfPath;

	@Value("${egov.dristi.pdf.process.bundle}")
	private String processCaseBundlePdfPath;

	//ElasticSearch Config
	@Value("${egov.infra.indexer.host}")
	private String esHostUrl;

	@Value("${egov.indexer.es.username}")
	private String esUsername;

	@Value("${egov.indexer.es.password}")
	private String esPassword;

	@Value("${dristi.case.index}")
	private String caseIndex;

	@Value("${dristi.bundle.index}")
	private String caseBundleIndex;

	@Value("${dristi.hearing.index}")
	private String hearingIndex;

	@Value("${dristi.witness.index}")
	private String witnessIndex;

	@Value("${dristi.order.index}")
	private String orderIndex;

	@Value("${dristi.task.index}")
	private String taskIndex;

	@Value("${dristi.application.index}")
	private String applicationIndex;

	@Value("${dristi.artifact.index}")
	private String artifactIndex;

	@Value("${dristi.search.index.path}")
	private String searchPath;

	//Kafka
	@Value("${casemanagement.kafka.vc.create.topic}")
	private String createVc;

	@Value("${casemanagement.kafka.bundle.create.topic}")
	private String bundleCreateTopic;

	@Value("${casemanagement.kafka.update.casebundles.topic}")
	private String updateCaseBundlesTopic;

	@Value("${generate.vc.code}")
	private String vcCode;

	@Value("${case.allowed.status}")
	private String caseAllowedStatuses;
	private List<String> caseAllowedStatusesList = new ArrayList<>();

	// CTC Service Config — write-side only; reads via CtcApi (Rule 35
	// keeps cross-subdomain writes on REST).
	@Value("${dristi.ctc.host}")
	private String ctcHost;

	@Value("${dristi.ctc.update.endpoint}")
	private String ctcUpdateEndpoint;

	@PostConstruct
	public void init() {
		caseAllowedStatusesList = Arrays.asList(caseAllowedStatuses.split(","));
	}

}
