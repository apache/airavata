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
package org.apache.airavata.k8s.gfac.messaging;

import org.apache.airavata.k8s.gfac.service.WorkerService;
import org.springframework.kafka.annotation.KafkaListener;

import javax.annotation.Resource;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class KafkaReceiver {

    @Resource
    private WorkerService workerService;

    @KafkaListener(topics = "${scheduler.topic.name}", containerFactory = "kafkaListenerContainerFactory")
    public void receiveProcesses(String payload) {
        System.out.println("received process=" + payload);
        workerService.launchProcess(Long.parseLong(payload));
    }

    @KafkaListener(topics = "${task.event.topic.name}", containerFactory = "kafkaEventListenerContainerFactory")
    public void receiveTaskEvent(String payload) {
        System.out.println("received event=" + payload);
        String[] eventParts = payload.split(",");
        long processId = Long.parseLong(eventParts[0]);
        long taskId = Long.parseLong(eventParts[1]);
        int state = Integer.parseInt(eventParts[2]);
        workerService.onTaskStateEvent(processId, taskId, state);
    }
}
