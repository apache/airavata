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

import com.github.dozermapper.core.Mapper;
import org.apache.airavata.model.appcatalog.computeresource.BatchQueue;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.registry.entities.appcatalog.JobSubmissionInterfaceEntity;
import org.apache.airavata.registry.entities.appcatalog.JobSubmissionInterfacePK;
import org.apache.airavata.registry.exceptions.AppCatalogException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ComputeResourceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {org.apache.airavata.config.JpaConfig.class})
@TestPropertySource(locations = "classpath:airavata.properties")
public class JobSubmissionInterfaceRepositoryTest extends TestBase {

    private final ComputeResourceService computeResourceService;
    private final JobSubmissionInterfaceRepository jobSubmissionInterfaceRepository;
    private final Mapper mapper;

    private String computeResourceId;

    public JobSubmissionInterfaceRepositoryTest(
            ComputeResourceService computeResourceService,
            JobSubmissionInterfaceRepository jobSubmissionInterfaceRepository,
            Mapper mapper) {
        super(Database.APP_CATALOG);
        this.computeResourceService = computeResourceService;
        this.jobSubmissionInterfaceRepository = jobSubmissionInterfaceRepository;
        this.mapper = mapper;
    }

    @BeforeEach
    public void createTestComputeResource() throws AppCatalogException {

        ComputeResourceDescription description = new ComputeResourceDescription();
        description.setHostName("localhost");
        description.addToBatchQueues(new BatchQueue("queue1"));
        description.addToBatchQueues(new BatchQueue("queue2"));
        computeResourceId = computeResourceService.addComputeResource(description);
    }

    @AfterEach
    public void removeTestComputeResource() throws AppCatalogException {

        computeResourceService.removeComputeResource(computeResourceId);
    }

    @Test
    public void testAddJobSubmissionInterface() throws AppCatalogException {

        JobSubmissionInterface jobSubmissionInterface = new JobSubmissionInterface();
        jobSubmissionInterface.setJobSubmissionInterfaceId("test");
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
        JobSubmissionInterface retrievedJobSubmissionInterface = mapper.map(entity, JobSubmissionInterface.class);
        Assertions.assertEquals("test", retrievedJobSubmissionInterface.getJobSubmissionInterfaceId());
        Assertions.assertEquals(1, retrievedJobSubmissionInterface.getPriorityOrder());
        Assertions.assertEquals(JobSubmissionProtocol.SSH, retrievedJobSubmissionInterface.getJobSubmissionProtocol());

        computeResourceService.removeJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
    }
}
