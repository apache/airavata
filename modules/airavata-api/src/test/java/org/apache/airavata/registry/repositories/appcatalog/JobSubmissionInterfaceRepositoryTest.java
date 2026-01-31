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
package org.apache.airavata.registry.repositories.appcatalog;

import org.apache.airavata.common.model.BatchQueue;
import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.common.model.JobSubmissionInterface;
import org.apache.airavata.common.model.JobSubmissionProtocol;
import org.apache.airavata.registry.entities.appcatalog.JobSubmissionInterfaceEntity;
import org.apache.airavata.registry.entities.appcatalog.JobSubmissionInterfacePK;
import org.apache.airavata.registry.exception.RegistryExceptions.AppCatalogException;
import org.apache.airavata.registry.mappers.JobSubmissionInterfaceMapper;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ComputeResourceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

@org.springframework.test.context.ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class JobSubmissionInterfaceRepositoryTest extends TestBase {

    private final ComputeResourceService computeResourceService;
    private final JobSubmissionInterfaceRepository jobSubmissionInterfaceRepository;
    private final JobSubmissionInterfaceMapper mapper;

    private String computeResourceId;
    private String testSuffix;

    public JobSubmissionInterfaceRepositoryTest(
            ComputeResourceService computeResourceService,
            JobSubmissionInterfaceRepository jobSubmissionInterfaceRepository,
            JobSubmissionInterfaceMapper mapper) {
        this.computeResourceService = computeResourceService;
        this.jobSubmissionInterfaceRepository = jobSubmissionInterfaceRepository;
        this.mapper = mapper;
    }

    @BeforeEach
    public void createTestComputeResource() throws AppCatalogException {
        // Use unique suffix for test isolation
        testSuffix = java.util.UUID.randomUUID().toString().substring(0, 8);

        ComputeResourceDescription description = new ComputeResourceDescription();
        description.setHostName("jobsub-test-host-" + testSuffix);

        // Initialize batch queues list
        java.util.List<BatchQueue> batchQueues = new java.util.ArrayList<>();
        var queue1 = new BatchQueue();
        queue1.setQueueName("queue1");
        batchQueues.add(queue1);
        var queue2 = new BatchQueue();
        queue2.setQueueName("queue2");
        batchQueues.add(queue2);
        description.setBatchQueues(batchQueues);

        computeResourceId = computeResourceService.addComputeResource(description);
    }

    @AfterEach
    public void removeTestComputeResource() throws AppCatalogException {

        computeResourceService.removeComputeResource(computeResourceId);
    }

    @Test
    public void testAddJobSubmissionInterface() throws AppCatalogException {
        // Use unique ID for test isolation
        String uniqueInterfaceId = "test-jobsub-" + testSuffix;

        JobSubmissionInterface jobSubmissionInterface = new JobSubmissionInterface();
        jobSubmissionInterface.setJobSubmissionInterfaceId(uniqueInterfaceId);
        jobSubmissionInterface.setPriorityOrder(1);
        jobSubmissionInterface.setJobSubmissionProtocol(JobSubmissionProtocol.SSH);

        String jobSubmissionInterfaceId =
                computeResourceService.addJobSubmissionProtocol(computeResourceId, jobSubmissionInterface);

        JobSubmissionInterfacePK pk = new JobSubmissionInterfacePK();
        pk.setComputeResourceId(computeResourceId);
        pk.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);

        JobSubmissionInterfaceEntity entity =
                jobSubmissionInterfaceRepository.findById(pk).orElse(null);
        Assertions.assertNotNull(entity);
        JobSubmissionInterface retrievedJobSubmissionInterface = mapper.toModel(entity);
        Assertions.assertEquals(uniqueInterfaceId, retrievedJobSubmissionInterface.getJobSubmissionInterfaceId());
        Assertions.assertEquals(1, retrievedJobSubmissionInterface.getPriorityOrder());
        Assertions.assertEquals(JobSubmissionProtocol.SSH, retrievedJobSubmissionInterface.getJobSubmissionProtocol());

        computeResourceService.removeJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
    }
}
