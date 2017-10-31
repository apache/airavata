/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apacher.airavata.k8s.task.egress.service;

import org.apache.airavata.k8s.api.resources.compute.ComputeResource;
import org.apache.airavata.k8s.api.resources.task.TaskParamResource;
import org.apache.airavata.k8s.api.resources.task.TaskResource;
import org.apache.airavata.k8s.api.resources.task.TaskStatusResource;
import org.apache.airavata.k8s.compute.api.ComputeOperations;
import org.apache.airavata.k8s.compute.impl.MockComputeOperation;
import org.apache.airavata.k8s.compute.impl.SSHComputeOperations;
import org.apacher.airavata.k8s.task.egress.messaging.KafkaSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;
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

        System.out.println("Executing task " + taskId + " as egress staging task");
        TaskResource taskResource = this.restTemplate.getForObject("http://" + apiServerUrl + "/task/" + taskId, TaskResource.class);

        publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(), TaskStatusResource.State.SCHEDULED);

        this.executorService.execute(new Runnable() {
            @Override
            public void run() {
                executeTask(taskResource);
            }
        });
    }

    public void executeTask(TaskResource taskResource) {
        try {
            Optional<TaskParamResource> sourceParam = taskResource.getTaskParams()
                    .stream()
                    .filter(taskParamResource -> "source".equals(taskParamResource.getKey()))
                    .findFirst();

            Optional<TaskParamResource> targetParam = taskResource.getTaskParams()
                    .stream()
                    .filter(taskParamResource -> "target".equals(taskParamResource.getKey()))
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
                    .orElseThrow(() -> new Exception("exp-data-dir param can not be found in the params of task " +
                            taskResource.getId())).getValue() + "/" + taskResource.getParentProcessId();


            if (sourceParam.isPresent()) {
                sourceParam.get().setValue(sourceParam.get().getValue().replace("{process-data-dir}", processDataDirectory));
                if (targetParam.isPresent()) {
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

                    try {

                        String temporaryFile = "/tmp/" + UUID.randomUUID().toString();
                        System.out.println("Downloading " + sourceParam.get().getValue() + " to " + temporaryFile +
                                " from compute resource " + computeResource.getName());
                        operations.transferDataOut(sourceParam.get().getValue(), temporaryFile, "SCP");

                        RestTemplate template = new RestTemplate();
                        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
                        map.add("file", new FileSystemResource(temporaryFile));
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

                        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<LinkedMultiValueMap<String, Object>>(
                                map, headers);

                        System.out.println("Uploading data file with task id " + taskResource.getId()
                                + " and experiment output id " + targetParam.get().getValue() + " to data store");

                        ResponseEntity<Long> result = template.exchange(
                                "http://" + apiServerUrl + "/data/" + taskResource.getId()+ "/"
                                        + targetParam.get().getValue() +"/upload", HttpMethod.POST, requestEntity, Long.class);

                        publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(),
                                TaskStatusResource.State.COMPLETED);

                    } catch (Exception e) {

                        e.printStackTrace();
                        publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(),
                                TaskStatusResource.State.FAILED, e.getMessage());
                    }
                } else {
                    System.out.println("Target can not be null for task " + taskResource.getId());
                    publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(),
                            TaskStatusResource.State.FAILED, "Target can not be null for task " + taskResource.getId());
                }
            } else {
                System.out.println("Source can not be null for task " + taskResource.getId());
                publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(),
                        TaskStatusResource.State.FAILED, "Source can not be null for task " + taskResource.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(),
                    TaskStatusResource.State.FAILED, e.getMessage());
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
