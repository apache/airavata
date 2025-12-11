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
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataProductType;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.model.data.replica.ReplicaPersistentType;
import org.apache.airavata.registry.entities.replicacatalog.DataReplicaMetadataEntity;
import org.apache.airavata.registry.exceptions.ReplicaCatalogException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.DataProductService;
import org.apache.airavata.registry.services.DataReplicaLocationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, DataReplicaLocationRepositoryTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "services.background.enabled=false",
            "services.thrift.enabled=false",
            "services.helix.enabled=false"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class DataReplicaLocationRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {"org.apache.airavata.service", "org.apache.airavata.registry", "org.apache.airavata.config"
            },
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
