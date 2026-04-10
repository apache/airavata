/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.compute.service;

import java.util.List;
import java.util.Optional;
import org.apache.airavata.compute.repository.QueueStatusRepository;
import org.apache.airavata.interfaces.QueueStatusRegistry;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.status.proto.QueueStatusModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class QueueStatusRegistryHandler implements QueueStatusRegistry {
    private static final Logger logger = LoggerFactory.getLogger(QueueStatusRegistryHandler.class);

    private final QueueStatusRepository queueStatusRepository = new QueueStatusRepository();

    @Override
    public QueueStatusModel getQueueStatus(String hostName, String queueName) throws Exception {
        try {
            Optional<QueueStatusModel> optionalQueueStatusModel =
                    queueStatusRepository.getQueueStatus(hostName, queueName);
            logger.info("Executed and present " + optionalQueueStatusModel.isPresent());
            if (optionalQueueStatusModel.isPresent()) {
                return optionalQueueStatusModel.get();
            } else {
                return QueueStatusModel.newBuilder()
                        .setHostName(hostName)
                        .setQueueName(queueName)
                        .setQueueUp(false)
                        .setRunningJobs(0)
                        .setQueuedJobs(0)
                        .setTime(0)
                        .build();
            }
        } catch (RegistryException e) {
            logger.error("Error while retrieving queue status models....", e);
            throw new RegistryException("Error while retrieving queue status models.... : " + e.getMessage());
        }
    }

    @Override
    public void registerQueueStatuses(List<QueueStatusModel> queueStatuses) throws Exception {
        try {
            queueStatusRepository.createQueueStatuses(queueStatuses);
        } catch (RegistryException e) {
            logger.error("Error while storing queue status models....", e);
            throw new RegistryException("Error while storing queue status models.... : " + e.getMessage());
        }
    }

    @Override
    public List<QueueStatusModel> getLatestQueueStatuses() throws Exception {
        try {
            return queueStatusRepository.getLatestQueueStatuses();
        } catch (RegistryException e) {
            throw new RegistryException("Error while reading queue status models.... : " + e.getMessage());
        }
    }
}
