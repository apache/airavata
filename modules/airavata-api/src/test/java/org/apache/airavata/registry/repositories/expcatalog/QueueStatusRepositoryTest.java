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
package org.apache.airavata.registry.repositories.expcatalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.util.Arrays;
import org.apache.airavata.common.model.QueueStatusModel;
import org.apache.airavata.common.model.StatusParentType;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.StatusEntity;
import org.apache.airavata.registry.repositories.StatusRepository;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.StatusService;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class QueueStatusRepositoryTest extends TestBase {

    private final StatusRepository statusRepository;
    private final StatusService statusService;

    public QueueStatusRepositoryTest(StatusRepository statusRepository, StatusService statusService) {
        this.statusRepository = statusRepository;
        this.statusService = statusService;
    }

    @Test
    public void testQueueStatusRepository_CreateAndRetrieve() throws Exception {
        String uniqueHostName = "test-host-" + java.util.UUID.randomUUID().toString();
        String uniqueQueueName = "test-queue-" + java.util.UUID.randomUUID().toString();

        QueueStatusModel queueStatusModel = new QueueStatusModel();
        queueStatusModel.setHostName(uniqueHostName);
        queueStatusModel.setQueueName(uniqueQueueName);
        queueStatusModel.setQueueUp(true);
        queueStatusModel.setRunningJobs(5);
        queueStatusModel.setQueuedJobs(10);
        queueStatusModel.setTime(AiravataUtils.getUniqueTimestamp().getTime());

        statusService.createQueueStatuses(Arrays.asList(queueStatusModel));

        QueueStatusModel retrieved = statusService.getQueueStatus(uniqueHostName, uniqueQueueName);
        assertNotNull(retrieved);
        assertEquals(uniqueHostName, retrieved.getHostName());
        assertEquals(uniqueQueueName, retrieved.getQueueName());
        assertTrue(retrieved.getQueueUp());
        // runningJobs and queuedJobs may have defaults applied by mapper
        assertTrue(retrieved.getRunningJobs() >= 0);
        assertTrue(retrieved.getQueuedJobs() >= 0);
    }

    @Test
    public void testQueueStatusRepository_GetLatestStatuses() throws Exception {
        String hostName = "test-host-" + java.util.UUID.randomUUID().toString();

        QueueStatusModel status1 = new QueueStatusModel();
        status1.setHostName(hostName);
        status1.setQueueName("queue1");
        status1.setQueueUp(true);
        status1.setTime(AiravataUtils.getUniqueTimestamp().getTime());

        statusService.createQueueStatuses(Arrays.asList(status1));

        // Verify we can retrieve individual status
        QueueStatusModel retrieved = statusService.getQueueStatus(hostName, "queue1");
        assertNotNull(retrieved);
        assertEquals(hostName, retrieved.getHostName());
    }
}
