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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.data.movement.GridFTPDataMovement;
import org.apache.airavata.model.data.movement.SCPDataMovement;
import org.apache.airavata.model.data.movement.SecurityProtocol;
import org.apache.airavata.registry.exceptions.AppCatalogException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ComputeResourceService;
import org.apache.airavata.registry.services.StorageResourceService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestConstructor;

/**
 * Created by skariyat on 3/13/18.
 */
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, StorageResourceRepositoryTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "services.background.enabled=false",
            "services.thrift.enabled=false",
            "services.helix.enabled=false",
            "services.airavata.enabled=false",
            "services.registryService.enabled=false",
            "services.userprofile.enabled=false",
            "services.groupmanager.enabled=false",
            "services.iam.enabled=false",
            "services.orchestrator.enabled=false",
            "security.manager.enabled=false"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class StorageResourceRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils"
            },
            useDefaultFilters = false,
            includeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ANNOTATION,
                        classes = {
                            org.springframework.stereotype.Component.class,
                            org.springframework.stereotype.Service.class,
                            org.springframework.stereotype.Repository.class,
                            org.springframework.context.annotation.Configuration.class
                        })
            },
            excludeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern = "org\\.apache\\.airavata\\.(monitor|helix|sharing\\.migrator|credential|profile|security|accountprovisioning)\\..*"),
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern = "org\\.apache\\.airavata\\.service\\..*")
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import({org.apache.airavata.config.AiravataPropertiesConfiguration.class, org.apache.airavata.config.DozerMapperConfig.class})
    static class TestConfiguration {}

    private static final Logger logger = LoggerFactory.getLogger(StorageResourceRepository.class);

    private final StorageResourceService storageResourceService;
    private final ComputeResourceService computeResourceService;

    public StorageResourceRepositoryTest(
            StorageResourceService storageResourceService, ComputeResourceService computeResourceService) {
        super(Database.APP_CATALOG);
        this.storageResourceService = storageResourceService;
        this.computeResourceService = computeResourceService;
    }

    @Test
    public void addStorageResource() throws AppCatalogException {

        StorageResourceDescription description = new StorageResourceDescription();

        description.setHostName("localhost");
        description.setEnabled(true);
        description.setStorageResourceDescription("testDescription");

        String scpDataMoveId = addSCPDataMovement();
        logger.info("**** SCP DataMoveId****** :{}", scpDataMoveId);
        String gridFTPDataMoveId = addGridFTPDataMovement();
        logger.info("**** grid FTP DataMoveId****** :{}", gridFTPDataMoveId);

        List<DataMovementInterface> dataMovementInterfaces = new ArrayList<DataMovementInterface>();
        DataMovementInterface scpInterface = new DataMovementInterface();
        scpInterface.setDataMovementInterfaceId(scpDataMoveId);
        scpInterface.setDataMovementProtocol(DataMovementProtocol.SCP);
        scpInterface.setPriorityOrder(1);

        DataMovementInterface gridFTPMv = new DataMovementInterface();
        gridFTPMv.setDataMovementInterfaceId(gridFTPDataMoveId);
        gridFTPMv.setDataMovementProtocol(DataMovementProtocol.GridFTP);
        gridFTPMv.setPriorityOrder(2);

        dataMovementInterfaces.add(scpInterface);
        dataMovementInterfaces.add(gridFTPMv);
        description.setDataMovementInterfaces(dataMovementInterfaces);

        String resourceId = storageResourceService.addStorageResource(description);
        StorageResourceDescription storageResourceDescription = null;

        if (storageResourceService.isStorageResourceExists(resourceId)) {
            storageResourceDescription = storageResourceService.getStorageResource(resourceId);
            assertTrue(storageResourceDescription.getHostName().equals("localhost"));
            assertTrue(
                    storageResourceDescription.getStorageResourceDescription().equals("testDescription"));
            List<DataMovementInterface> movementInterfaces = storageResourceDescription.getDataMovementInterfaces();
            if (movementInterfaces != null && !movementInterfaces.isEmpty()) {
                for (DataMovementInterface dataMovementInterface : movementInterfaces) {
                    logger.info("Data Movement Interface Id :{}", dataMovementInterface.getDataMovementInterfaceId());
                    logger.info(
                            "Data Movement Protocol :{}",
                            dataMovementInterface.getDataMovementProtocol().toString());
                    logger.info("Data Movement Priority Order: {}", dataMovementInterface.getPriorityOrder());
                }
            }
        } else {
            fail("Created Storage Resource not found");
        }

        description.setHostName("localhost2");
        storageResourceService.updateStorageResource(resourceId, description);
        if (storageResourceService.isStorageResourceExists(resourceId)) {
            storageResourceDescription = storageResourceService.getStorageResource(resourceId);
            logger.info("**********Updated Resource name ************* : {}", storageResourceDescription.getHostName());
            assertTrue(storageResourceDescription.getHostName().equals("localhost2"));
        }
        assertTrue(storageResourceDescription != null, "Storage resource save successfully");
    }

    public String addSCPDataMovement() {
        try {
            SCPDataMovement dataMovement = new SCPDataMovement();
            dataMovement.setSshPort(22);
            dataMovement.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
            return computeResourceService.addScpDataMovement(dataMovement);
        } catch (AppCatalogException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public String addGridFTPDataMovement() {
        try {
            GridFTPDataMovement dataMovement = new GridFTPDataMovement();
            dataMovement.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
            List<String> endPoints = new ArrayList<String>();
            endPoints.add("222.33.43.444");
            endPoints.add("23.344.44.454");
            dataMovement.setGridFTPEndPoints(endPoints);
            return computeResourceService.addGridFTPDataMovement(dataMovement);
        } catch (AppCatalogException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
