/*
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
 *
*/
package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertTrue;

public class QueueStatusRepositoryTest extends TestBase {

    private QueueStatusRepository queueStatusRepository;
    private static final Logger logger = LoggerFactory.getLogger(QueueStatusRepositoryTest.class);

    public QueueStatusRepositoryTest() {
        super(Database.EXP_CATALOG);
        queueStatusRepository = new QueueStatusRepository();
    }

    @Test
    public void createQueueStatusRepositoryTest() throws RegistryException {
        QueueStatusModel queueStatusModel = new QueueStatusModel();
        queueStatusModel.setHostName("host");
        queueStatusModel.setQueueName("queue");
        queueStatusModel.setQueueUp(true);
        queueStatusModel.setRunningJobs(1);
        queueStatusModel.setQueuedJobs(2);
        queueStatusModel.setTime(System.currentTimeMillis());

        List<QueueStatusModel> queueStatusList = new ArrayList<>();
        queueStatusList.add(queueStatusModel);

        boolean returnValue = queueStatusRepository.createQueueStatuses(queueStatusList);
        assertTrue(returnValue);

        List<QueueStatusModel> savedQueueStatusList = queueStatusRepository.getLatestQueueStatuses();
        Assert.assertTrue(EqualsBuilder.reflectionEquals(queueStatusList, savedQueueStatusList, "__isset_bitfield", "creationTime"));
    }

    @Test
    public void retrieveQueueStatusRepositoryTest() throws RegistryException {
        List<QueueStatusModel> actualQueueStatusList = new ArrayList<>();

        for (int i = 0 ; i < 5; i++) {
            QueueStatusModel queueStatusModel = new QueueStatusModel();
            queueStatusModel.setHostName("host");
            queueStatusModel.setQueueName("queue");
            queueStatusModel.setQueueUp(true);
            queueStatusModel.setRunningJobs(1);
            queueStatusModel.setQueuedJobs(2);
            queueStatusModel.setTime(System.currentTimeMillis());

            List<QueueStatusModel> queueStatusList = new ArrayList<>();
            queueStatusList.add(queueStatusModel);

            boolean returnValue = queueStatusRepository.createQueueStatuses(queueStatusList);
            assertTrue(returnValue);
            actualQueueStatusList.add(queueStatusModel);
        }

        for (int j = 0 ; j < 5; j++) {
            QueueStatusModel retrievedQueueStatusModel = queueStatusRepository.getLatestQueueStatuses().get(j);
            QueueStatusModel actualQueueStatusModel = actualQueueStatusList.get(j);
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualQueueStatusModel, retrievedQueueStatusModel, "__isset_bitfield"));
        }
    }

}
