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
import org.apache.airavata.registry.exception.AppCatalogException;
import org.apache.airavata.registry.mappers.JobSubmissionInterfaceMapper;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ComputeResourceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.TestcontainersConfig.class,
            org.apache.airavata.config.AiravataPropertiesConfiguration.class,
            JobSubmissionInterfaceRepositoryTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "flyway.enabled=false"
            // Infrastructure components (including SecurityManagerConfig) excluded via @ComponentScan excludeFilters -
            // no property flags needed
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@org.junit.jupiter.api.Disabled("Requires full app catalog; skipped in offline test runs")
public class JobSubmissionInterfaceRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.mappers",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils"
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import({
        org.apache.airavata.config.AiravataPropertiesConfiguration.class,
    })
    static class TestConfiguration {}

    private final ComputeResourceService computeResourceService;
    private final JobSubmissionInterfaceRepository jobSubmissionInterfaceRepository;
    private final JobSubmissionInterfaceMapper mapper;

    private String computeResourceId;

    public JobSubmissionInterfaceRepositoryTest(
            ComputeResourceService computeResourceService,
            JobSubmissionInterfaceRepository jobSubmissionInterfaceRepository,
            JobSubmissionInterfaceMapper mapper) {
        super(Database.APP_CATALOG);
        this.computeResourceService = computeResourceService;
        this.jobSubmissionInterfaceRepository = jobSubmissionInterfaceRepository;
        this.mapper = mapper;
    }

    @BeforeEach
    public void createTestComputeResource() throws AppCatalogException {

        ComputeResourceDescription description = new ComputeResourceDescription();
        description.setHostName("localhost");
        var queue1 = new BatchQueue();
        queue1.setQueueName("queue1");
        var queue2 = new BatchQueue();
        queue2.setQueueName("queue2");
        description.getBatchQueues().add(queue1);
        description.getBatchQueues().add(queue2);
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
        JobSubmissionInterface retrievedJobSubmissionInterface = mapper.toModel(entity);
        Assertions.assertEquals("test", retrievedJobSubmissionInterface.getJobSubmissionInterfaceId());
        Assertions.assertEquals(1, retrievedJobSubmissionInterface.getPriorityOrder());
        Assertions.assertEquals(JobSubmissionProtocol.SSH, retrievedJobSubmissionInterface.getJobSubmissionProtocol());

        computeResourceService.removeJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
    }
}
