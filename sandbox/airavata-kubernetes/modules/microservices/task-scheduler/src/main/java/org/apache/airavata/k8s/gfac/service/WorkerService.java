package org.apache.airavata.k8s.gfac.service;

import org.apache.airavata.k8s.api.resources.process.ProcessResource;
import org.apache.airavata.k8s.api.resources.task.TaskResource;
import org.apache.airavata.k8s.api.resources.task.TaskStatusResource;
import org.apache.airavata.k8s.gfac.core.ProcessLifeCycleManager;
import org.apache.airavata.k8s.gfac.messaging.KafkaSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Service
public class WorkerService {

    private final RestTemplate restTemplate;
    private final KafkaSender kafkaSender;
    private Map<Long, ProcessLifeCycleManager> processLifecycleStore = new HashMap<>();

    @Value("${api.server.url}")
    private String apiServerUrl;

    public WorkerService(RestTemplate restTemplate, KafkaSender kafkaSender) {
        this.restTemplate = restTemplate;
        this.kafkaSender = kafkaSender;
    }

    public void launchProcess(long processId) {
        System.out.println("Launching process " + processId);
        ProcessResource processResource = this.restTemplate.getForObject("http://" + apiServerUrl + "/process/" + processId,
                ProcessResource.class);
        List<TaskResource> taskResources = processResource.getTasks();
        boolean freshProcess = true;
        for (TaskResource taskResource : taskResources) {
            if (taskResource.getTaskStatusIds() != null && taskResource.getTaskStatusIds().size() > 0) {
                // Already partially completed process. This happens when the task scheduler is killed while processing a process
                long lastStatusId = taskResource.getTaskStatusIds().get(taskResource.getTaskStatusIds().size() - 1);
                TaskStatusResource statusResource = restTemplate.getForObject("http://" + apiServerUrl + "/task/status/" + lastStatusId, TaskStatusResource.class);
                // TODO continue from last state
                freshProcess = false;
            } else {
                // Fresh task

            }
        }

        if (freshProcess) {
            System.out.println("Starting to execute process " + processId);
            ProcessLifeCycleManager manager = new ProcessLifeCycleManager(processId, taskResources, kafkaSender, restTemplate, apiServerUrl);
            manager.init();
            manager.submitTaskToQueue(taskResources.get(0));
            processLifecycleStore.put(processId, manager);
        }
    }

    public void onTaskStateEvent(long processId, long taskId, int state) {
        Optional.ofNullable(processLifecycleStore.get(processId))
                .ifPresent(manager -> manager.onTaskStateChanged(taskId, state));
    }
}
