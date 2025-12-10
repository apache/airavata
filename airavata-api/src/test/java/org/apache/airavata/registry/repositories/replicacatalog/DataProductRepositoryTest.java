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
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataProductType;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.model.data.replica.ReplicaLocationCategory;
import org.apache.airavata.model.data.replica.ReplicaPersistentType;
import org.apache.airavata.registry.entities.replicacatalog.DataProductMetadataEntity;
import org.apache.airavata.registry.exceptions.ReplicaCatalogException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.DataProductService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestConstructor;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, DataProductRepositoryTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class DataProductRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {"org.apache.airavata.service", "org.apache.airavata.registry", "org.apache.airavata.config"},
            excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.config.BackgroundServicesLauncher.class,
                            org.apache.airavata.config.ThriftServerLauncher.class,
                            org.apache.airavata.monitor.realtime.RealtimeMonitor.class,
                            org.apache.airavata.monitor.email.EmailBasedMonitor.class,
                            org.apache.airavata.monitor.cluster.ClusterStatusMonitorJob.class,
                            org.apache.airavata.monitor.AbstractMonitor.class,
                            org.apache.airavata.helix.impl.controller.HelixController.class,
                            org.apache.airavata.helix.impl.participant.GlobalParticipant.class,
                            org.apache.airavata.helix.impl.workflow.PreWorkflowManager.class,
                            org.apache.airavata.helix.impl.workflow.PostWorkflowManager.class,
                            org.apache.airavata.helix.impl.workflow.ParserWorkflowManager.class
                        }),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.apache\\.airavata\\.monitor\\..*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.apache\\.airavata\\.helix\\..*")
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import(org.apache.airavata.config.AiravataPropertiesConfiguration.class)
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

        testDataProductModel1.addToReplicaLocations(replicaLocationModel1);

        String productUri1 = dataProductService.registerDataProduct(testDataProductModel1);
        assertTrue(dataProductService.isDataProductExists(productUri1));

        DataProductModel retrievedDataProductModel1 = dataProductService.getDataProduct(productUri1);
        assertEquals(productUri1, retrievedDataProductModel1.getProductUri());

        assertEquals(1, retrievedDataProductModel1.getReplicaLocationsSize());
        DataReplicaLocationModel retrievedReplicaLocationModel1 =
                retrievedDataProductModel1.getReplicaLocations().get(0);
        assertEquals(productUri1, retrievedReplicaLocationModel1.getProductUri());
        // validUntilTime has a default value
        assertEquals(0, retrievedReplicaLocationModel1.getValidUntilTime());

        dataProductService.removeDataProduct(productUri1);
        assertFalse(dataProductService.isDataProductExists(productUri1));
    }
}
