package org.apache.airavata.agent.connection.service.services;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.model.error.AiravataClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiravataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiravataService.class);
    private static final int TIMEOUT = 100000;

    @Value("${airavata.server.url}, scigap02.sciencegateways.iu.edu")
    private String serverUrl;

    @Value("${airavata.server.port}, 9930")
    private int port;

    @Value("${airavata.server.truststore.path}")
    private String trustStorePath;

    public Airavata.Client airavata() {
        try {
            LOGGER.debug("Creating Airavata client with the TrustStore URL - " + trustStorePath);
            return AiravataClientFactory.createAiravataSecureClient(serverUrl, port, trustStorePath, "airavata", TIMEOUT);

        } catch (AiravataClientException e) {
            LOGGER.error("Error while creating Airavata client", e);
            throw new RuntimeException("Error while creating Airavata client", e);
        }
    }
}
