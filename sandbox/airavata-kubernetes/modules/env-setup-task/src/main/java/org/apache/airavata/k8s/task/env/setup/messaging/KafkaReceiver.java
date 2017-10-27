package org.apache.airavata.k8s.task.env.setup.messaging;

import org.apache.airavata.k8s.task.env.setup.service.TaskExecutionService;
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
    private TaskExecutionService taskExecutionService;

    @KafkaListener(topics = "${task.read.topic.name}")
    public void receiveTasks(String payload) {
        System.out.println("received task=" + payload);
        taskExecutionService.executeTask(Long.parseLong(payload));
    }
}
