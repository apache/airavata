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
package org.apache.airavata.compute.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.apache.airavata.interfaces.RegistryException;
import org.apache.airavata.model.status.proto.QueueStatusModel;
import org.apache.airavata.util.TestBase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueStatusRepositoryTest extends TestBase {

    QueueStatusRepository queueStatusRepository;
    private static final Logger logger = LoggerFactory.getLogger(QueueStatusRepositoryTest.class);

    public QueueStatusRepositoryTest() {
        super();
        queueStatusRepository = new QueueStatusRepository();
    }

    @Test
    public void QueueStatusRepositoryTest() throws RegistryException {
        QueueStatusModel queueStatusModel =
                QueueStatusModel.newBuilder().setHostName("host").build();
        queueStatusModel = queueStatusModel.toBuilder().setQueueName("queue").build();
        queueStatusModel = queueStatusModel.toBuilder().setQueueUp(true).build();
        queueStatusModel = queueStatusModel.toBuilder().setRunningJobs(1).build();
        queueStatusModel = queueStatusModel.toBuilder().setQueuedJobs(2).build();
        queueStatusModel =
                queueStatusModel.toBuilder().setTime(System.currentTimeMillis()).build();

        boolean returnValue = queueStatusRepository.createQueueStatuses(Arrays.asList(queueStatusModel));
        assertTrue(returnValue);

        List<QueueStatusModel> queueStatusModelList = queueStatusRepository.getLatestQueueStatuses();
        assertTrue(queueStatusModelList.size() == 1);
        assertEquals(queueStatusModel.getHostName(), queueStatusModelList.get(0).getHostName());
    }
}
