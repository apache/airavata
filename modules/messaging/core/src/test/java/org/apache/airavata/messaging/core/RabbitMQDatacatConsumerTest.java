package org.apache.airavata.messaging.core;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.messaging.core.impl.BetterRabbitMQDatacatConsumer;
import org.apache.airavata.messaging.core.impl.RabbitMQDatacatConsumer;
import org.apache.airavata.messaging.core.impl.RabbitMQDatacatPublisher;
import org.apache.airavata.model.messaging.event.ExperimentOutputCreatedEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class RabbitMQDatacatConsumerTest {
    private static Logger log = LoggerFactory.getLogger(RabbitMQDatacatPublisherTest.class);

    private BetterRabbitMQDatacatConsumer rabbitMQDatacatConsumer;

    @Before
    public void setup() {
        try {
            rabbitMQDatacatConsumer = new BetterRabbitMQDatacatConsumer();
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    @Test
    public void testDatacatConsumer() throws AiravataException, InterruptedException {
        rabbitMQDatacatConsumer.startBroker();
        Thread.sleep(20000);
        rabbitMQDatacatConsumer.stopBroker();
    }
}
