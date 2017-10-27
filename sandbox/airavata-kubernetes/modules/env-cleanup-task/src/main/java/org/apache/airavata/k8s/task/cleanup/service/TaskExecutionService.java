package org.apache.airavata.k8s.task.cleanup.service;

import org.apache.airavata.k8s.api.resources.task.TaskParamResource;
import org.apache.airavata.k8s.api.resources.task.TaskResource;
import org.apache.airavata.k8s.api.resources.task.TaskStatusResource;
import org.apache.airavata.k8s.task.cleanup.messaging.KafkaSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Service
public class TaskExecutionService {
    private final RestTemplate restTemplate;
    private final KafkaSender kafkaSender;

    @Value("${api.server.url}")
    private String apiServerUrl;

    @Value("${task.event.topic.name}")
    private String taskEventPublishTopic;

    public TaskExecutionService(RestTemplate restTemplate, KafkaSender kafkaSender) {
        this.restTemplate = restTemplate;
        this.kafkaSender = kafkaSender;
    }

    public void executeTask(long taskId) {
        System.out.println("Executing task " + taskId + " as env cleanup task");
        TaskResource taskResource = this.restTemplate.getForObject("http://" + apiServerUrl + "/task/" + taskId, TaskResource.class);

        publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(), TaskStatusResource.State.SCHEDULED);

        Optional<TaskParamResource> commandParam = taskResource.getTaskParams()
                .stream()
                .filter(taskParamResource -> "command".equals(taskParamResource.getKey()))
                .findFirst();

        commandParam.ifPresent(taskParamResource -> System.out.println("Executing command " + taskParamResource.getValue()));

        publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(), TaskStatusResource.State.EXECUTING);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(), TaskStatusResource.State.COMPLETED);

    }

    public void publishTaskStatus(long processId, long taskId, int status) {
        this.kafkaSender.send(this.taskEventPublishTopic, processId + "," +taskId + "," + status);
    }
}
