package org.pucar.dristi.payments.calculator.internal.payment.calculator.service;

import org.pucar.dristi.payments.calculator.internal.payment.calculator.config.Configuration;
import org.pucar.dristi.payments.calculator.internal.payment.calculator.enrichment.PostalHubEnrichment;
import org.pucar.dristi.common.kafka.Producer;
import org.pucar.dristi.payments.calculator.internal.payment.calculator.repository.PostalHubRepository;
import org.pucar.dristi.payments.calculator.internal.payment.calculator.validator.PostalHubValidator;
import org.pucar.dristi.payments.calculator.internal.payment.calculator.web.models.HubSearchRequest;
import org.pucar.dristi.payments.calculator.internal.payment.calculator.web.models.PostalHub;
import org.pucar.dristi.payments.calculator.internal.payment.calculator.web.models.PostalHubRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PostalHubService {

    private final PostalHubRepository repository;
    private final PostalHubValidator validator;
    private final PostalHubEnrichment enrichment;
    private final Producer producer;
    private final Configuration config;

    @Autowired
    public PostalHubService(PostalHubRepository repository, PostalHubValidator validator, PostalHubEnrichment enrichment, Producer producer, Configuration config) {
        this.repository = repository;
        this.validator = validator;
        this.enrichment = enrichment;
        this.producer = producer;
        this.config = config;
    }


    public List<PostalHub> create(PostalHubRequest request) {
        log.info("operation = create, result= IN_PROGRESS, hubs={}", request.getPostalHubs());

        validator.validateCreateHubRequest(request);

        enrichment.enrichPostalHubRequest(request);

        producer.push(config.getPostalHubCreateTopic(), request);

        log.info("operation = create, result= SUCCESS, hubs={}", request.getPostalHubs());
        return request.getPostalHubs();
    }

    public List<PostalHub> search(HubSearchRequest searchRequest) {
        return repository.getPostalHub(searchRequest.getCriteria());
    }

    public List<PostalHub> update(PostalHubRequest request) {
        log.info("operation = update, result= IN_PROGRESS, hubs={}", request.getPostalHubs());

        validator.validateExistingPostalHubRequest(request);

        enrichment.enrichExistingPostalHubRequest(request);

        producer.push(config.getPostalHubUpdateTopic(), request);

        log.info("operation = update, result= SUCCESS, hubs={}", request.getPostalHubs());
        return request.getPostalHubs();
    }

}
