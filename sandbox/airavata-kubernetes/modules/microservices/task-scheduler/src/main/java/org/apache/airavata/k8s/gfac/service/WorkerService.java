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
            if (taskResource.getTaskStatus() != null && taskResource.getTaskStatus().size() > 0) {
                // Already partially completed process. This happens when the task scheduler is killed while processing a process
                TaskStatusResource lastStatusResource = taskResource.getTaskStatus().get(taskResource.getTaskStatus().size() - 1);
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
