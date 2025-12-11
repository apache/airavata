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
package org.apache.airavata.registry.repositories.expcatalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.UUID;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.GatewayService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, GatewayRepositoryTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "services.background.enabled=false",
            "services.thrift.enabled=false",
            "services.helix.enabled=false",
            "services.airavata.enabled=false",
            "services.userprofile.enabled=false",
            "services.groupmanager.enabled=false",
            "services.iam.enabled=false",
            "services.orchestrator.enabled=false",
            "security.manager.enabled=false"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class GatewayRepositoryTest extends TestBase {

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
                        pattern = "org\\.apache\\.airavata\\.(monitor|helix|sharing\\.migrator|credential|profile|security|accountprovisioning|registry\\.messaging)\\..*"),
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern = "org\\.apache\\.airavata\\.service\\..*")
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import({org.apache.airavata.config.AiravataPropertiesConfiguration.class, org.apache.airavata.config.DozerMapperConfig.class})
    static class TestConfiguration {}


    private final GatewayService gatewayService;
    private final AiravataServerProperties properties;

    public GatewayRepositoryTest(GatewayService gatewayService, AiravataServerProperties properties) {
        super(Database.EXP_CATALOG);
        this.gatewayService = gatewayService;
        this.properties = properties;
    }

    @Test
    public void gatewayRepositoryTest() throws ApplicationSettingsException, RegistryException {
        // Create default gateway if it doesn't exist
        String defaultGatewayId = properties.services.default_.gateway;
        if (!gatewayService.isGatewayExist(defaultGatewayId)) {
            Gateway defaultGateway = new Gateway();
            defaultGateway.setGatewayId(defaultGatewayId);
            defaultGateway.setGatewayApprovalStatus(GatewayApprovalStatus.APPROVED);
            defaultGateway.setOauthClientId(properties.security.iam.oauthClientId);
            defaultGateway.setOauthClientSecret(properties.security.iam.oauthClientSecret);
            gatewayService.addGateway(defaultGateway);
        }
        
        // Verify that default Gateway is already created
        List<Gateway> defaultGatewayList = gatewayService.getAllGateways();
        assertEquals(1, defaultGatewayList.size());
        assertEquals(
                properties.services.default_.gateway, defaultGatewayList.get(0).getGatewayId());

        // Generate unique test gateway ID for this test run
        String testGatewayId = "testGateway-" + UUID.randomUUID().toString().substring(0, 8);
        
        Gateway gateway = new Gateway();
        gateway.setGatewayId(testGatewayId);
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("abc@d.com");
        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.APPROVED);
        gateway.setOauthClientId("pga");
        gateway.setOauthClientSecret("9580cafa-7c1e-434f-bfe9-595f63907a43");

        String gatewayId = gatewayService.addGateway(gateway);
        assertEquals(testGatewayId, gatewayId);

        gateway.setGatewayAdminFirstName("ABC");
        gatewayService.updateGateway(testGatewayId, gateway);

        Gateway retrievedGateway = gatewayService.getGateway(gatewayId);
        assertEquals(gateway.getGatewayAdminFirstName(), retrievedGateway.getGatewayAdminFirstName());
        assertEquals(GatewayApprovalStatus.APPROVED, gateway.getGatewayApprovalStatus());
        assertEquals(gateway.getOauthClientId(), retrievedGateway.getOauthClientId());
        assertEquals(gateway.getOauthClientSecret(), retrievedGateway.getOauthClientSecret());

        assertEquals(2, gatewayService.getAllGateways().size(), "should be 2 gateways (1 default plus 1 just added)");

        gatewayService.removeGateway(gatewayId);
        assertFalse(gatewayService.isGatewayExist(gatewayId));
    }
}
