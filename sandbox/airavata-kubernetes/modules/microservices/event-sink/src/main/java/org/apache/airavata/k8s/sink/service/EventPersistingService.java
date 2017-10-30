package org.apache.airavata.k8s.sink.service;

import org.apache.airavata.k8s.api.resources.task.TaskStatusResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Service
public class EventPersistingService {

    private final RestTemplate restTemplate;

    @Value("${api.server.url}")
    private String apiServerUrl;

    public EventPersistingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void persistTaskState(long processId, long taskId, int state, String reason) {
        System.out.println("Persisting task state event for process " + processId + ", task " + taskId + ", state "
                + state + ", reason " + reason);
        TaskStatusResource statusResource = new TaskStatusResource();
        statusResource.setTaskId(taskId);
        statusResource.setTimeOfStateChange(System.currentTimeMillis());
        statusResource.setState(state);
        statusResource.setReason(reason);
        this.restTemplate.postForObject("http://" + this.apiServerUrl + "/task/" + taskId + "/status", statusResource,
                Long.class);
    }
}
