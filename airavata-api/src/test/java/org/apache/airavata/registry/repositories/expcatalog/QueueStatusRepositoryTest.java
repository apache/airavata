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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.QueueStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {org.apache.airavata.config.JpaConfig.class})
@TestPropertySource(locations = "classpath:airavata.properties")
public class QueueStatusRepositoryTest extends TestBase {

    private final QueueStatusService queueStatusService;

    public QueueStatusRepositoryTest(QueueStatusService queueStatusService) {
        super(Database.EXP_CATALOG);
        this.queueStatusService = queueStatusService;
    }

    @Test
    public void testQueueStatusRepository() throws RegistryException {
        QueueStatusModel queueStatusModel = new QueueStatusModel();
        queueStatusModel.setHostName("host");
        queueStatusModel.setQueueName("queue");
        queueStatusModel.setQueueUp(true);
        queueStatusModel.setRunningJobs(1);
        queueStatusModel.setQueuedJobs(2);
        queueStatusModel.setTime(System.currentTimeMillis());

        boolean returnValue = queueStatusService.createQueueStatuses(Arrays.asList(queueStatusModel));
        assertTrue(returnValue);

        List<QueueStatusModel> queueStatusModelList = queueStatusService.getLatestQueueStatuses();
        assertTrue(queueStatusModelList.size() == 1);
        assertEquals(queueStatusModel.getHostName(), queueStatusModelList.get(0).getHostName());
    }
}
