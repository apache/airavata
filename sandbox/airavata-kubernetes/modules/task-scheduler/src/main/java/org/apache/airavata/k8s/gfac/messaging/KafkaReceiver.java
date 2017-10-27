package org.apache.airavata.k8s.gfac.messaging;

import org.apache.airavata.k8s.gfac.service.WorkerService;
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
    private WorkerService workerService;

    @KafkaListener(topics = "${scheduler.topic.name}", containerFactory = "kafkaListenerContainerFactory")
    public void receiveProcesses(String payload) {
        System.out.println("received process=" + payload);
        workerService.launchProcess(Long.parseLong(payload));
    }

    @KafkaListener(topics = "${task.event.topic.name}", containerFactory = "kafkaEventListenerContainerFactory")
    public void receiveTaskEvent(String payload) {
        System.out.println("received event=" + payload);
        String[] eventParts = payload.split(",");
        long processId = Long.parseLong(eventParts[0]);
        long taskId = Long.parseLong(eventParts[1]);
        int state = Integer.parseInt(eventParts[2]);
        workerService.onTaskStateEvent(processId, taskId, state);
    }
}
