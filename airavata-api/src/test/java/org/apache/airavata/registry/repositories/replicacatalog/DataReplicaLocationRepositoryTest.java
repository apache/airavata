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
package org.apache.airavata.registry.repositories.replicacatalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.DataProductModel;
import org.apache.airavata.common.model.DataProductType;
import org.apache.airavata.common.model.DataReplicaLocationModel;
import org.apache.airavata.common.model.ReplicaPersistentType;
import org.apache.airavata.registry.entities.replicacatalog.DataReplicaMetadataEntity;
import org.apache.airavata.registry.exception.ReplicaCatalogException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.DataProductService;
import org.apache.airavata.registry.services.DataReplicaLocationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, DataReplicaLocationRepositoryTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.allow-circular-references=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            // Infrastructure components (including SecurityManagerConfig) excluded via @ComponentScan excludeFilters -
            // no property flags needed
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
public class DataReplicaLocationRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.mappers",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils"
            },
            useDefaultFilters = false,
            includeFilters = {
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ANNOTATION,
                        classes = {
                            org.springframework.stereotype.Component.class,
                            org.springframework.stereotype.Service.class,
                            org.springframework.stereotype.Repository.class,
                            org.springframework.context.annotation.Configuration.class
                        })
            },
            excludeFilters = {
                // Exclude infrastructure components - use DI instead of property flags
                // Helix components
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.helix.adaptor.SSHJAgentAdaptor.class,
                            org.apache.airavata.helix.adaptor.SSHJStorageAdaptor.class,
                            org.apache.airavata.helix.agent.ssh.SshAgentAdaptor.class,
                            org.apache.airavata.helix.agent.storage.StorageResourceAdaptorImpl.class,
                            org.apache.airavata.helix.core.support.TaskHelperImpl.class,
                            org.apache.airavata.helix.core.support.adaptor.AdaptorSupportImpl.class,
                            org.apache.airavata.helix.impl.controller.HelixController.class,
                            org.apache.airavata.helix.impl.participant.GlobalParticipant.class,
                            org.apache.airavata.helix.impl.task.AWSTaskFactory.class,
                            org.apache.airavata.helix.impl.task.AiravataTask.class,
                            org.apache.airavata.helix.impl.task.SlurmTaskFactory.class,
                            org.apache.airavata.helix.impl.task.TaskFactory.class,
                            org.apache.airavata.helix.impl.task.aws.utils.AWSTaskUtil.class,
                            org.apache.airavata.helix.impl.task.submission.config.GroovyMapBuilder.class,
                            org.apache.airavata.helix.impl.workflow.ParserWorkflowManager.class,
                            org.apache.airavata.helix.impl.workflow.PostWorkflowManager.class,
                            org.apache.airavata.helix.impl.workflow.PreWorkflowManager.class
                        }),
                // Monitor components
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.monitor.AbstractMonitor.class,
                            org.apache.airavata.monitor.cluster.ClusterStatusMonitorJob.class,
                            org.apache.airavata.monitor.compute.ComputationalResourceMonitoringService.class,
                            org.apache.airavata.monitor.email.EmailBasedMonitor.class,
                            org.apache.airavata.monitor.realtime.RealtimeMonitor.class
                        }),
                // DB Event Manager components
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.manager.dbevent.DBEventManagerRunner.class,
                            org.apache.airavata.manager.dbevent.messaging.DBEventManagerMessagingFactory.class,
                            org.apache.airavata.manager.dbevent.messaging.impl.DBEventMessageHandler.class
                        }),
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {org.apache.airavata.config.BackgroundServicesLauncher.class}),
                // Orchestrator components
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.orchestrator.impl.SimpleOrchestratorImpl.class,
                            org.apache.airavata.orchestrator.utils.OrchestratorUtils.class,
                            org.apache.airavata.orchestrator.validation.impl.ValidationServiceImpl.class,
                            org.apache.airavata.orchestrator.validator.BatchQueueValidator.class,
                            org.apache.airavata.orchestrator.validator.GroupResourceProfileValidator.class
                        })
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import({
        org.apache.airavata.config.AiravataPropertiesConfiguration.class,
    })
    static class TestConfiguration {}

    private final DataProductService dataProductService;
    private final DataReplicaLocationService dataReplicaLocationService;

    private String gatewayId = "testGateway";

    public DataReplicaLocationRepositoryTest(
            DataProductService dataProductService, DataReplicaLocationService dataReplicaLocationService) {
        super(Database.REPLICA_CATALOG);
        this.dataProductService = dataProductService;
        this.dataReplicaLocationService = dataReplicaLocationService;
    }

    @Test
    public void dataReplicaLocationServiceTest() throws ReplicaCatalogException {
        DataProductModel testDataProductModel = new DataProductModel();
        testDataProductModel.setGatewayId(gatewayId);
        testDataProductModel.setOwnerName("testUser");
        testDataProductModel.setDataProductType(DataProductType.COLLECTION);
        testDataProductModel.setProductName("productName");
        String productUri = dataProductService.registerDataProduct(testDataProductModel);
        assertTrue(dataProductService.isDataProductExists(productUri));

        DataReplicaLocationModel testDataReplicaLocationModel1 = new DataReplicaLocationModel();
        testDataReplicaLocationModel1.setReplicaName("replicaName1");
        testDataReplicaLocationModel1.setProductUri(productUri);
        String replicaId1 = dataReplicaLocationService.registerReplicaLocation(testDataReplicaLocationModel1);

        DataReplicaLocationModel testDataReplicaLocationModel2 = new DataReplicaLocationModel();
        testDataReplicaLocationModel2.setReplicaName("replicaName2");
        testDataReplicaLocationModel2.setProductUri(productUri);
        String replicaId2 = dataReplicaLocationService.registerReplicaLocation(testDataReplicaLocationModel2);

        DataReplicaMetadataEntity dataReplicaMetadataEntity1 = new DataReplicaMetadataEntity();
        dataReplicaMetadataEntity1.setReplicaId(replicaId1);
        dataReplicaMetadataEntity1.setMetadataKey("dataKey1");
        dataReplicaMetadataEntity1.setMetadataValue("dataValue1");

        DataReplicaMetadataEntity dataReplicaMetadataEntity2 = new DataReplicaMetadataEntity();
        dataReplicaMetadataEntity2.setReplicaId(replicaId1);
        dataReplicaMetadataEntity2.setMetadataKey("dataKey2");
        dataReplicaMetadataEntity2.setMetadataValue("dataValue2");

        Map<String, String> dataReplicaMetadataEntityMap = new HashMap<>();
        dataReplicaMetadataEntityMap.put(
                dataReplicaMetadataEntity1.getMetadataKey(), dataReplicaMetadataEntity1.getMetadataValue());
        dataReplicaMetadataEntityMap.put(
                dataReplicaMetadataEntity2.getMetadataKey(), dataReplicaMetadataEntity2.getMetadataValue());
        testDataReplicaLocationModel1.setReplicaMetadata(dataReplicaMetadataEntityMap);
        testDataReplicaLocationModel1.setReplicaPersistentType(ReplicaPersistentType.TRANSIENT);
        assertTrue(dataReplicaLocationService.updateReplicaLocation(testDataReplicaLocationModel1));

        DataReplicaLocationModel retrievedDataReplicaLocationModel =
                dataReplicaLocationService.getReplicaLocation(replicaId1);
        assertTrue(retrievedDataReplicaLocationModel.getReplicaMetadata().size() == 2);
        assertEquals(
                retrievedDataReplicaLocationModel.getReplicaPersistentType(),
                testDataReplicaLocationModel1.getReplicaPersistentType());
        // validUntilTime has a default value
        assertEquals(0, retrievedDataReplicaLocationModel.getValidUntilTime());

        testDataProductModel.setReplicaLocations(
                Arrays.asList(testDataReplicaLocationModel1, testDataReplicaLocationModel2));
        dataProductService.updateDataProduct(testDataProductModel);
        assertTrue(dataProductService
                        .getDataProduct(productUri)
                        .getReplicaLocations()
                        .size()
                == 2);

        List<DataReplicaLocationModel> dataReplicaLocationModelList =
                dataReplicaLocationService.getAllReplicaLocations(productUri);
        assertTrue(dataReplicaLocationModelList.size() == 2);

        dataReplicaLocationService.removeReplicaLocation(replicaId1);
        dataReplicaLocationService.removeReplicaLocation(replicaId2);
        dataProductService.removeDataProduct(productUri);
    }
}
