package org.apache.airavata.k8s.task.job.service;

import org.apache.airavata.k8s.api.resources.compute.ComputeResource;
import org.apache.airavata.k8s.api.resources.task.TaskParamResource;
import org.apache.airavata.k8s.api.resources.task.TaskResource;
import org.apache.airavata.k8s.api.resources.task.TaskStatusResource;
import org.apache.airavata.k8s.compute.api.ComputeOperations;
import org.apache.airavata.k8s.compute.api.ExecutionResult;
import org.apache.airavata.k8s.compute.impl.MockComputeOperation;
import org.apache.airavata.k8s.compute.impl.SSHComputeOperations;
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

        try {
            Optional<TaskParamResource> commandParam = taskResource.getTaskParams()
                    .stream()
                    .filter(taskParamResource -> "command".equals(taskParamResource.getKey()))
                    .findFirst();
            Optional<TaskParamResource> argumentsParam = taskResource.getTaskParams()
                    .stream()
                    .filter(taskParamResource -> "arguments".equals(taskParamResource.getKey()))
                    .findFirst();
            Optional<TaskParamResource> computeId = taskResource.getTaskParams()
                    .stream()
                    .filter(taskParamResource -> "compute-id".equals(taskParamResource.getKey()))
                    .findFirst();
            Optional<TaskParamResource> experimentDataDir = taskResource.getTaskParams()
                    .stream()
                    .filter(taskParamResource -> "exp-data-dir".equals(taskParamResource.getKey()))
                    .findFirst();

            String processDataDirectory = experimentDataDir
                    .orElseThrow(() -> new Exception("exp-data-dir param can not be found the tas params of task " +
                            taskResource.getId())).getValue() + "/" + taskResource.getParentProcessId();


            commandParam.ifPresent(taskParamResource -> {
                try {
                    String command = taskParamResource.getValue();
                    command = command.replace("{process-data-dir}", processDataDirectory);
                    System.out.println("Executing command " + command);

                    argumentsParam.ifPresent(taskArgParamResource -> {
                        taskArgParamResource.setValue(taskArgParamResource.getValue()
                                .replace("{process-data-dir}", processDataDirectory));
                        System.out.println("With arguments " + taskArgParamResource.getValue());
                    });

                    publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(), TaskStatusResource.State.EXECUTING);

                    ComputeResource computeResource = this.restTemplate.getForObject("http://" + this.apiServerUrl
                            + "/compute/" + Long.parseLong(computeId.get().getValue()), ComputeResource.class);

                    ComputeOperations operations;
                    if ("SSH".equals(computeResource.getCommunicationType())) {
                        operations = new SSHComputeOperations(computeResource.getHost(), computeResource.getUserName(), computeResource.getPassword());
                    } else if ("Mock".equals(computeResource.getCommunicationType())) {
                        operations = new MockComputeOperation(computeResource.getHost());
                    } else {
                        throw new Exception("No compatible communication method {" + computeResource.getCommunicationType() + "} not found for compute resource " + computeResource.getName());
                    }

                    ExecutionResult executionResult = operations.executeCommand(command +
                            (argumentsParam.isPresent() ? argumentsParam.get().getValue() : ""));

                    if (executionResult.getExitStatus() == 0) {
                        publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(), TaskStatusResource.State.COMPLETED);
                    } else if (executionResult.getExitStatus() == -1) {
                        publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(), TaskStatusResource.State.FAILED, "Process didn't exit successfully");
                    } else {
                        publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(), TaskStatusResource.State.FAILED, "Process exited with error status " + executionResult.getExitStatus());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(), TaskStatusResource.State.FAILED, e.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(), TaskStatusResource.State.FAILED, e.getMessage());
        }
    }

    public void publishTaskStatus(long processId, long taskId, int status) {
        publishTaskStatus(processId, taskId, status, "");
    }

    public void publishTaskStatus(long processId, long taskId, int status, String reason) {
        this.kafkaSender.send(this.taskEventPublishTopic, processId + "-" + taskId,
                processId + "," + taskId + "," + status + "," + reason);
    }
}
