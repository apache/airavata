package org.apache.airavata.service.messaging;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.ExperimentSubmitEvent;
import org.apache.airavata.model.messaging.event.ExperimentIntermediateOutputsEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.status.ExperimentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

    private final Publisher statusPublisher;
    private final Publisher experimentPublisher;

    public EventPublisher(Publisher statusPublisher, Publisher experimentPublisher) {
        this.statusPublisher = statusPublisher;
        this.experimentPublisher = experimentPublisher;
    }

    public void publishExperimentStatus(String experimentId, String gatewayId, ExperimentState state) {
        if (statusPublisher == null) return;
        try {
            ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent(state, experimentId, gatewayId);
            String messageId = AiravataUtils.getId("EXPERIMENT");
            MessageContext messageContext = new MessageContext(event, MessageType.EXPERIMENT, messageId, gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            statusPublisher.publish(messageContext);
        } catch (AiravataException e) {
            logger.error("Failed to publish experiment status event for {}", experimentId, e);
        }
    }

    public void publishExperimentLaunch(String experimentId, String gatewayId) {
        if (experimentPublisher == null) return;
        try {
            ExperimentSubmitEvent event = new ExperimentSubmitEvent(experimentId, gatewayId);
            MessageContext messageContext = new MessageContext(
                    event, MessageType.EXPERIMENT, "LAUNCH.EXP-" + UUID.randomUUID(), gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            experimentPublisher.publish(messageContext);
        } catch (AiravataException e) {
            logger.error("Failed to publish experiment launch event for {}", experimentId, e);
        }
    }

    public void publishExperimentCancel(String experimentId, String gatewayId) {
        if (experimentPublisher == null) return;
        try {
            ExperimentSubmitEvent event = new ExperimentSubmitEvent(experimentId, gatewayId);
            MessageContext messageContext = new MessageContext(
                    event, MessageType.EXPERIMENT_CANCEL, "CANCEL.EXP-" + UUID.randomUUID(), gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            experimentPublisher.publish(messageContext);
        } catch (AiravataException e) {
            logger.error("Failed to publish experiment cancel event for {}", experimentId, e);
        }
    }

    public void publishIntermediateOutputs(String experimentId, String gatewayId, List<String> outputNames) {
        if (experimentPublisher == null) return;
        try {
            ExperimentIntermediateOutputsEvent event =
                    new ExperimentIntermediateOutputsEvent(experimentId, gatewayId, outputNames);
            MessageContext messageContext = new MessageContext(
                    event, MessageType.INTERMEDIATE_OUTPUTS,
                    "INTERMEDIATE_OUTPUTS.EXP-" + UUID.randomUUID(), gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            experimentPublisher.publish(messageContext);
        } catch (AiravataException e) {
            logger.error("Failed to publish intermediate outputs event for {}", experimentId, e);
        }
    }
}
