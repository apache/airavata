package org.apache.airavata.k8s.orchestrator.messaging;

import org.apache.airavata.k8s.orchestrator.service.ExperimentLaunchService;
import org.springframework.kafka.annotation.KafkaListener;

import javax.annotation.Resource;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class KafkaReceiver {

    @Resource
    private ExperimentLaunchService experimentLaunchService;

    @KafkaListener(topics = "${launch.topic.name}")
    public void receive(String payload) {
        System.out.println("received payload=" + payload);
        if (payload.startsWith("exp-")) {
            long experimentId = Long.parseLong(payload.substring(4));
            this.experimentLaunchService.launch(experimentId);
        }
    }
}
