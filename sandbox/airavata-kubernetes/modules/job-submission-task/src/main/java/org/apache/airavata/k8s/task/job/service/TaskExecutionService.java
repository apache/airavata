package org.apache.airavata.k8s.task.job.service;

import org.apache.airavata.k8s.api.resources.task.TaskParamResource;
import org.apache.airavata.k8s.api.resources.task.TaskResource;
import org.apache.airavata.k8s.api.resources.task.TaskStatusResource;
import org.apache.airavata.k8s.compute.api.ComputeOperations;
import org.apache.airavata.k8s.compute.impl.MockComputeOperation;
import org.apache.airavata.k8s.task.job.messaging.KafkaSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Service
public class TaskExecutionService {

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

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

    public void executeTaskAsync(long taskId) {

        System.out.println("Executing task " + taskId + " as job submission task");
        TaskResource taskResource = this.restTemplate.getForObject("http://" + apiServerUrl + "/task/" + taskId, TaskResource.class);

        publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(), TaskStatusResource.State.SCHEDULED);

        this.executorService.execute(new Runnable() {
            @Override
            public void run() {
                executeTask(taskResource);
            }
        });
    }

    private void executeTask(TaskResource taskResource) {

        Optional<TaskParamResource> commandParam = taskResource.getTaskParams()
                .stream()
                .filter(taskParamResource -> "command".equals(taskParamResource.getKey()))
                .findFirst();
        Optional<TaskParamResource> argumentsParam = taskResource.getTaskParams()
                .stream()
                .filter(taskParamResource -> "arguments".equals(taskParamResource.getKey()))
                .findFirst();
        Optional<TaskParamResource> computeName = taskResource.getTaskParams()
                .stream()
                .filter(taskParamResource -> "compute-name".equals(taskParamResource.getKey()))
                .findFirst();

        commandParam.ifPresent(taskParamResource -> {
            System.out.println("Executing command " + taskParamResource.getValue());
            argumentsParam.ifPresent(taskArgParamResource -> System.out.println("With arguments " + taskArgParamResource.getValue()));

            publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(), TaskStatusResource.State.EXECUTING);
            ComputeOperations operations = new MockComputeOperation(computeName.get().getValue());

            try {
                operations.executeCommand(taskParamResource.getValue() +
                        (argumentsParam.isPresent() ? "" : argumentsParam.get().getValue()));
                publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(), TaskStatusResource.State.COMPLETED);

            } catch (Exception e) {
                e.printStackTrace();
                publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(), TaskStatusResource.State.FAILED);
            }
        });
    }

    public void publishTaskStatus(long processId, long taskId, int status) {
        this.kafkaSender.send(this.taskEventPublishTopic, processId + "-" + taskId,
                processId + "," + taskId + "," + status);
    }
}
