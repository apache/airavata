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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.DataProductModel;
import org.apache.airavata.common.model.DataProductType;
import org.apache.airavata.common.model.DataReplicaLocationModel;
import org.apache.airavata.common.model.ReplicaLocationCategory;
import org.apache.airavata.common.model.ReplicaPersistentType;
import org.apache.airavata.registry.entities.replicacatalog.DataProductMetadataEntity;
import org.apache.airavata.registry.exception.ReplicaCatalogException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.DataProductService;
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
            org.apache.airavata.config.AiravataPropertiesConfiguration.class,
            DataProductRepositoryTest.TestConfiguration.class
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
public class DataProductRepositoryTest extends TestBase {

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

    private String gatewayId = "testGateway";
    private String userId = "testUser";
    private String productName = "testProduct";

    public DataProductRepositoryTest(DataProductService dataProductService) {
        super(Database.REPLICA_CATALOG);
        this.dataProductService = dataProductService;
    }

    @Test
    public void dataProductServiceTest() throws ReplicaCatalogException {
        DataProductModel testDataProductModel1 = new DataProductModel();
        testDataProductModel1.setGatewayId(gatewayId);
        testDataProductModel1.setOwnerName(userId);
        testDataProductModel1.setDataProductType(DataProductType.COLLECTION);
        testDataProductModel1.setProductName(productName);

        String productUri1 = dataProductService.registerDataProduct(testDataProductModel1);
        assertTrue(dataProductService.isDataProductExists(productUri1));

        DataProductModel retrievedDataProductModel1 = dataProductService.getDataProduct(productUri1);
        assertEquals(retrievedDataProductModel1.getProductUri(), productUri1);

        DataProductModel testDataProductModel2 = new DataProductModel();
        testDataProductModel2.setGatewayId(gatewayId);
        testDataProductModel2.setOwnerName(userId);
        testDataProductModel2.setDataProductType(DataProductType.FILE);
        testDataProductModel2.setProductName(productName);

        String productUri2 = dataProductService.registerDataProduct(testDataProductModel2);
        assertTrue(dataProductService.isDataProductExists(productUri2));

        DataProductMetadataEntity dataProductMetadataEntity = new DataProductMetadataEntity();
        dataProductMetadataEntity.setProductUri(productUri2);
        dataProductMetadataEntity.setMetadataKey("dataKey");
        dataProductMetadataEntity.setMetadataValue("dataValue");

        Map<String, String> dataProductMetadataEntityMap = new HashMap<>();
        dataProductMetadataEntityMap.put(
                dataProductMetadataEntity.getMetadataKey(), dataProductMetadataEntity.getMetadataValue());
        testDataProductModel2.setProductMetadata(dataProductMetadataEntityMap);
        testDataProductModel2.setParentProductUri(productUri1);
        assertTrue(dataProductService.updateDataProduct(testDataProductModel2));

        DataProductModel retrievedDataProductModel2 = dataProductService.getDataProduct(productUri2);
        assertTrue(retrievedDataProductModel2.getProductMetadata().size() == 1);

        DataProductModel retrievedParentDataProductModel = dataProductService.getParentDataProduct(productUri2);
        assertEquals(retrievedParentDataProductModel.getProductUri(), productUri1);

        List<DataProductModel> childDataProductList = dataProductService.getChildDataProducts(productUri1);
        assertTrue(childDataProductList.size() == 1);

        List<DataProductModel> dataProductModelList =
                dataProductService.searchDataProductsByName(gatewayId, userId, productName, -1, 0);
        assertTrue(dataProductModelList.size() == 2);

        dataProductService.removeDataProduct(productUri1);
        assertFalse(dataProductService.isDataProductExists(productUri1));

        dataProductService.removeDataProduct(productUri2);
    }

    @Test
    public void testDataProductWithReplicaLocation() throws ReplicaCatalogException {
        DataProductModel testDataProductModel1 = new DataProductModel();
        testDataProductModel1.setGatewayId(gatewayId);
        testDataProductModel1.setOwnerName(userId);
        testDataProductModel1.setDataProductType(DataProductType.FILE);
        testDataProductModel1.setProductName(productName);

        DataReplicaLocationModel replicaLocationModel1 = new DataReplicaLocationModel();
        replicaLocationModel1.setFilePath("/path/to/file.dat");
        replicaLocationModel1.setReplicaDescription("Description of replica");
        replicaLocationModel1.setReplicaLocationCategory(ReplicaLocationCategory.GATEWAY_DATA_STORE);
        replicaLocationModel1.setReplicaName("file.dat");
        replicaLocationModel1.setStorageResourceId("storage_resource_id");
        replicaLocationModel1.setReplicaPersistentType(ReplicaPersistentType.PERSISTENT);

        testDataProductModel1.getReplicaLocations().add(replicaLocationModel1);

        String productUri1 = dataProductService.registerDataProduct(testDataProductModel1);
        assertTrue(dataProductService.isDataProductExists(productUri1));

        DataProductModel retrievedDataProductModel1 = dataProductService.getDataProduct(productUri1);
        assertEquals(productUri1, retrievedDataProductModel1.getProductUri());

        assertEquals(1, retrievedDataProductModel1.getReplicaLocations().size());
        DataReplicaLocationModel retrievedReplicaLocationModel1 =
                retrievedDataProductModel1.getReplicaLocations().get(0);
        assertEquals(productUri1, retrievedReplicaLocationModel1.getProductUri());
        // validUntilTime has a default value
        assertEquals(0, retrievedReplicaLocationModel1.getValidUntilTime());

        dataProductService.removeDataProduct(productUri1);
        assertFalse(dataProductService.isDataProductExists(productUri1));
    }
}
