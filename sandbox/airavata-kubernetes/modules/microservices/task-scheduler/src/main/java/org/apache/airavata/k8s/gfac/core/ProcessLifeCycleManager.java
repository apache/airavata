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
package org.apache.airavata.k8s.gfac.core;

import org.apache.airavata.k8s.api.resources.process.ProcessStatusResource;
import org.apache.airavata.k8s.api.resources.task.TaskResource;
import org.apache.airavata.k8s.api.resources.task.TaskStatusResource;
import org.apache.airavata.k8s.gfac.messaging.KafkaSender;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ProcessLifeCycleManager {

    private long processId;
    private List<TaskResource> taskDag;
    private Map<Long, Integer> taskPoint;
    private KafkaSender kafkaSender;

    // Todo abstract out these parameters to reusable class
    private final RestTemplate restTemplate;
    private String apiServerUrl;

    public ProcessLifeCycleManager(long processId, List<TaskResource> tasks,
                                   KafkaSender kafkaSender,
                                   RestTemplate restTemplate, String apiServerUrl) {
        this.processId = processId;
        this.taskDag = tasks;
        this.kafkaSender = kafkaSender;
        this.restTemplate = restTemplate;
        this.apiServerUrl = apiServerUrl;
    }

    public void init() {
        taskDag.sort(Comparator.comparing(TaskResource::getOrder));
        taskPoint = new HashMap<>();
        for (int i = 0; i < taskDag.size(); i++) {
            taskPoint.put(taskDag.get(i).getId(), i);
        }
        updateProcessStatus(ProcessStatusResource.State.EXECUTING);
    }

    public synchronized void onTaskStateChanged(long taskId, int state) {
        switch (state) {
            case TaskStatusResource.State.COMPLETED:
                System.out.println("Task " + taskId + " was completed");
                Optional.ofNullable(this.taskPoint.get(taskId)).ifPresent(point -> {
                    if (point + 1 < taskDag.size()) {
                        TaskResource resource = taskDag.get(point + 1);
                        submitTaskToQueue(resource);
                    } else {
                        updateProcessStatus(ProcessStatusResource.State.COMPLETED);
                    }
                });
                break;
            case TaskStatusResource.State.FAILED:
                updateProcessStatus(ProcessStatusResource.State.FAILED);
                break;
        }
    }

    public void submitTaskToQueue(TaskResource taskResource) {

        switch (taskResource.getTaskType()) {
            case TaskResource.TaskTypes.EGRESS_DATA_STAGING :
                System.out.println("Submitting task " + taskResource.getId() + " to egress data staging queue");
                updateProcessStatus(ProcessStatusResource.State.OUTPUT_DATA_STAGING);
                this.kafkaSender.send("airavata-task-egress-staging", taskResource.getId() + "");
                break;
            case TaskResource.TaskTypes.INGRESS_DATA_STAGING :
                System.out.println("Submitting task " + taskResource.getId() + " to ingress data staging queue");
                updateProcessStatus(ProcessStatusResource.State.INPUT_DATA_STAGING);
                this.kafkaSender.send("airavata-task-ingress-staging", taskResource.getId() + "");
                break;
            case TaskResource.TaskTypes.ENV_SETUP :
                System.out.println("Submitting task " + taskResource.getId() + " to env setup queue");
                updateProcessStatus(ProcessStatusResource.State.PRE_PROCESSING);
                this.kafkaSender.send("airavata-task-env-setup", taskResource.getId() + "");
                break;
            case TaskResource.TaskTypes.ENV_CLEANUP :
                System.out.println("Submitting task " + taskResource.getId() + " to env cleanup queue");
                updateProcessStatus(ProcessStatusResource.State.POST_PROCESSING);
                this.kafkaSender.send("airavata-task-env-cleanup", taskResource.getId() + "");
                break;
            case TaskResource.TaskTypes.JOB_SUBMISSION :
                System.out.println("Submitting task " + taskResource.getId() + " to job submission queue");
                updateProcessStatus(ProcessStatusResource.State.EXECUTING);
                this.kafkaSender.send("airavata-task-job-submission", taskResource.getId() + "");
                break;
        }
    }

    private void updateProcessStatus(int state) {
        this.restTemplate.postForObject("http://" + apiServerUrl + "/process/" + this.processId + "/status",
                new ProcessStatusResource()
                        .setState(state)
                        .setTimeOfStateChange(System.currentTimeMillis()),
                Long.class);
    }

}
