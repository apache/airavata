package org.apache.airavata.messaging.core;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.messaging.core.impl.RabbitMQDatacatPublisher;
import org.apache.airavata.messaging.core.impl.RabbitMQPublisher;
import org.apache.airavata.model.messaging.event.ExperimentOutputCreatedEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class RabbitMQDatacatPublisherTest {

    private static Logger log = LoggerFactory.getLogger(RabbitMQDatacatPublisherTest.class);

    private RabbitMQDatacatPublisher rabbitMQDatacatPublisher;

    @Before
    public void setup() {
        try {
            rabbitMQDatacatPublisher = new RabbitMQDatacatPublisher();
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    @Test
    public void testDatacatPublisher() throws AiravataException {
        String outputFile = "gauss.out";
        String outputPath = "/home/swithana";
        String messageId = "tesaeqwe";
        String gatewayID = "gridChem";
        String expID = "230u34jnr0813";

        ExperimentOutputCreatedEvent event = new ExperimentOutputCreatedEvent(expID,
                outputFile, outputPath+ File.separatorChar+outputFile);

        MessageContext messageContext = new MessageContext(event, MessageType.EXPERIMENT_OUTPUT
                , messageId, gatewayID);
        messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());

        rabbitMQDatacatPublisher.publish(messageContext);
    }
}
