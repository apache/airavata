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
