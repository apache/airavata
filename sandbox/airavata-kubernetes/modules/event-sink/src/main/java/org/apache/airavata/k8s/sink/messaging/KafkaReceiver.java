package org.apache.airavata.k8s.sink.messaging;

import org.apache.airavata.k8s.sink.service.EventPersistingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

import javax.annotation.Resource;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class KafkaReceiver {

    @Resource
    private EventPersistingService eventPersistingService;

    @KafkaListener(topics = "${task.event.topic.name}")
    public void receiveTasks(String payload, Acknowledgment ack) {
        System.out.println("received task status=" + payload);
        String[] eventParts = payload.split(",");
        long processId = Long.parseLong(eventParts[0]);
        long taskId = Long.parseLong(eventParts[1]);
        int state = Integer.parseInt(eventParts[2]);
        eventPersistingService.persistTaskState(processId, taskId, state);
        ack.acknowledge();
    }
}
