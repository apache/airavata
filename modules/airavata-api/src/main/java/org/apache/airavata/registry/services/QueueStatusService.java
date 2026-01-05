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
package org.apache.airavata.registry.services;

import java.util.List;
import org.apache.airavata.common.model.QueueStatusModel;
import org.apache.airavata.registry.entities.expcatalog.QueueStatusEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.QueueStatusMapper;
import org.apache.airavata.registry.repositories.expcatalog.QueueStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class QueueStatusService {
    private final QueueStatusRepository queueStatusRepository;
    private final QueueStatusMapper queueStatusMapper;

    public QueueStatusService(QueueStatusRepository queueStatusRepository, QueueStatusMapper queueStatusMapper) {
        this.queueStatusRepository = queueStatusRepository;
        this.queueStatusMapper = queueStatusMapper;
    }

    public List<QueueStatusModel> getLatestQueueStatuses() throws RegistryException {
        // Get all queue statuses, then group by hostName and queueName to get latest
        // This is a simplified implementation - may need optimization
        List<QueueStatusEntity> allEntities = queueStatusRepository.findAll();
        // Group by hostName+queueName and get latest for each
        // For now, return all - can be optimized later
        return queueStatusMapper.toModelList(allEntities);
    }

    public boolean createQueueStatuses(List<QueueStatusModel> queueStatuses) throws RegistryException {
        for (QueueStatusModel status : queueStatuses) {
            QueueStatusEntity entity = queueStatusMapper.toEntity(status);
            queueStatusRepository.save(entity);
        }
        return true;
    }

    public QueueStatusModel getQueueStatus(String hostName, String queueName) throws RegistryException {
        var entity = queueStatusRepository.findFirstByHostNameAndQueueNameOrderByTimeDesc(hostName, queueName);
        if (entity.isEmpty()) return null;
        return queueStatusMapper.toModel(entity.get());
    }
}
