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
package org.apache.airavata.k8s.task.ingress.service;

import org.apache.airavata.k8s.api.resources.task.TaskParamResource;
import org.apache.airavata.k8s.api.resources.task.TaskResource;
import org.apache.airavata.k8s.api.resources.task.TaskStatusResource;
import org.apache.airavata.k8s.compute.api.ComputeOperations;
import org.apache.airavata.k8s.compute.impl.MockComputeOperation;
import org.apache.airavata.k8s.task.ingress.messaging.KafkaSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
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

        System.out.println("Executing task " + taskId + " as ingress task");
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

        Optional<TaskParamResource> sourceParam = taskResource.getTaskParams()
                .stream()
                .filter(taskParamResource -> "source".equals(taskParamResource.getKey()))
                .findFirst();

        Optional<TaskParamResource> targetParam = taskResource.getTaskParams()
                .stream()
                .filter(taskParamResource -> "target".equals(taskParamResource.getKey()))
                .findFirst();

        Optional<TaskParamResource> computeName = taskResource.getTaskParams()
                .stream()
                .filter(taskParamResource -> "compute-name".equals(taskParamResource.getKey()))
                .findFirst();

        if (sourceParam.isPresent()) {
            if (targetParam.isPresent()) {
                publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(), TaskStatusResource.State.EXECUTING);
                ComputeOperations computeOperations = new MockComputeOperation(computeName.get().getValue());

                try {
                    computeOperations.transferDataIn(sourceParam.get().getValue(), targetParam.get().getValue(), "SCP");
                    publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(), TaskStatusResource.State.COMPLETED);

                } catch (Exception e) {

                    e.printStackTrace();
                    publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(), TaskStatusResource.State.FAILED);
                }
            } else {
                System.out.println("Source can not be null for task " + taskResource.getId());
                publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(), TaskStatusResource.State.FAILED);
            }
        } else {
            System.out.println("Source can not be null for task " + taskResource.getId());
            publishTaskStatus(taskResource.getParentProcessId(), taskResource.getId(), TaskStatusResource.State.FAILED);
        }
    }

    public void publishTaskStatus(long processId, long taskId, int status) {
        this.kafkaSender.send(this.taskEventPublishTopic, processId + "-" + taskId,
                processId + "," + taskId + "," + status);
    }
}
