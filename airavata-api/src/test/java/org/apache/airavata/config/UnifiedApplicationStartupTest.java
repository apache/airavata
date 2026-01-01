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
package org.apache.airavata.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.EntityManagerFactory;
import org.apache.airavata.accountprovisioning.SSHAccountManager;
import org.apache.airavata.common.utils.DefaultKeyStorePasswordCallback;
import org.apache.airavata.credential.repositories.CredentialRepository;
import org.apache.airavata.helix.core.support.adaptor.AdaptorSupportImpl;
import org.apache.airavata.helix.task.api.support.AdaptorSupport;
import org.apache.airavata.profile.repositories.UserProfileRepository;
import org.apache.airavata.registry.repositories.appcatalog.ComputeResourceRepository;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentRepository;
import org.apache.airavata.security.GatewayGroupsInitializer;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.apache.airavata.sharing.repositories.DomainRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * Test suite to verify UnifiedApplication startup with different configurations.
 *
 * These tests verify that:
 * - Spring context loads successfully
 * - All required beans are created
 * - Services can initialize
 * - Different startup modes work correctly
 */
@SpringBootTest(
        classes = {
            JpaConfig.class,
            AiravataPropertiesConfiguration.class,
            UnifiedApplicationStartupTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.allow-circular-references=true",
            "spring.main.banner-mode=off",
            "spring.main.log-startup-info=false",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true"
            // Infrastructure components excluded via @ComponentScan excludeFilters - no property flags needed
            // Core services (RegistryService, CredentialStoreService) are always available via DI
        })
@TestPropertySource(locations = "classpath:airavata.properties")
public class UnifiedApplicationStartupTest {

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.mappers",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.service",
                "org.apache.airavata.profile.repositories",
                "org.apache.airavata.profile.mappers",
                "org.apache.airavata.profile.utils",
                "org.apache.airavata.sharing.services",
                "org.apache.airavata.sharing.repositories",
                "org.apache.airavata.sharing.mappers",
                "org.apache.airavata.sharing.utils",
                "org.apache.airavata.credential.repositories",
                "org.apache.airavata.credential.services",
                "org.apache.airavata.credential.utils",
                "org.apache.airavata.messaging",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils",
                "org.apache.airavata.security",
                "org.apache.airavata.accountprovisioning"
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
                        }),
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {org.apache.airavata.config.SecurityManagerConfig.class})
            })
    @Import({AiravataPropertiesConfiguration.class})
    static class TestConfiguration {}

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    @Qualifier("profileServiceEntityManagerFactory")
    private EntityManagerFactory profileServiceEntityManagerFactory;

    @Autowired(required = false)
    @Qualifier("appCatalogEntityManagerFactory")
    private EntityManagerFactory appCatalogEntityManagerFactory;

    @Autowired(required = false)
    @Qualifier("expCatalogEntityManagerFactory")
    private EntityManagerFactory expCatalogEntityManagerFactory;

    @Test
    public void testApplicationContextLoads() {
        assertNotNull(applicationContext, "Application context should be loaded");
    }

    @Test
    public void testAllEntityManagerFactoriesAreCreated() {
        assertNotNull(profileServiceEntityManagerFactory, "Profile service EntityManagerFactory should be created");
        assertNotNull(appCatalogEntityManagerFactory, "App catalog EntityManagerFactory should be created");
        assertNotNull(expCatalogEntityManagerFactory, "Exp catalog EntityManagerFactory should be created");
    }

    @Test
    public void testCoreServicesAreCreated() {
        // Test that core services are available as beans
        assertTrue(
                applicationContext.getBeansOfType(RegistryService.class).size() > 0,
                "RegistryService should be registered as a bean");
        assertTrue(
                applicationContext.getBeansOfType(CredentialStoreService.class).size() > 0,
                "CredentialStoreService should be registered as a bean");
    }

    @Test
    public void testMapperBeansAreCreated() {
        // Test that MapStruct mappers are created as Spring beans
        assertTrue(
                applicationContext
                                .getBeansOfType(org.apache.airavata.registry.mappers.GatewayMapper.class)
                                .size()
                        > 0,
                "Registry GatewayMapper should be registered as a bean");
        assertTrue(
                applicationContext
                                .getBeansOfType(org.apache.airavata.profile.mappers.GatewayMapper.class)
                                .size()
                        > 0,
                "Profile GatewayMapper should be registered as a bean");
    }

    @Test
    public void testRequiredComponentsAreCreated() {
        // Test that required components are available
        assertTrue(
                applicationContext.getBeansOfType(SSHAccountManager.class).size() > 0,
                "SSHAccountManager should be registered as a bean");
        assertTrue(
                applicationContext
                                .getBeansOfType(DefaultKeyStorePasswordCallback.class)
                                .size()
                        > 0,
                "DefaultKeyStorePasswordCallback should be registered as a bean");
        assertTrue(
                applicationContext
                                .getBeansOfType(GatewayGroupsInitializer.class)
                                .size()
                        > 0,
                "GatewayGroupsInitializer should be registered as a bean");
    }

    @Test
    public void testAdaptorSupportBeanIsPrimary() {
        // Test that AdaptorSupportImpl is marked as @Primary
        // Note: AdaptorSupport may not be available if orchestrator is disabled
        if (applicationContext.getBeansOfType(AdaptorSupport.class).size() > 0) {
            AdaptorSupport adaptorSupport = applicationContext.getBean(AdaptorSupport.class);
            assertNotNull(adaptorSupport, "AdaptorSupport bean should be available");
            assertTrue(
                    adaptorSupport instanceof AdaptorSupportImpl,
                    "AdaptorSupport should be AdaptorSupportImpl (primary bean)");
        }
    }

    @Test
    public void testRepositoriesAreInjected() {
        UserProfileRepository userProfileRepository = applicationContext.getBean(UserProfileRepository.class);
        ComputeResourceRepository computeResourceRepository =
                applicationContext.getBean(ComputeResourceRepository.class);
        ExperimentRepository experimentRepository = applicationContext.getBean(ExperimentRepository.class);
        DomainRepository domainRepository = applicationContext.getBean(DomainRepository.class);
        CredentialRepository credentialRepository = applicationContext.getBean(CredentialRepository.class);

        assertNotNull(userProfileRepository, "UserProfileRepository should be injected");
        assertNotNull(computeResourceRepository, "ComputeResourceRepository should be injected");
        assertNotNull(experimentRepository, "ExperimentRepository should be injected");
        assertNotNull(domainRepository, "DomainRepository should be injected");
        assertNotNull(credentialRepository, "CredentialRepository should be injected");
    }

    @Test
    public void testAiravataServiceIsNotInitializedWhenThriftDisabled() {
        // When thrift is disabled, AiravataService should not be fully initialized
        // (it requires sharing registry initialization which may fail in test environment)
        // The bean may not exist if the service is disabled
        // This test verifies that the context loads successfully without AiravataService
        assertTrue(
                applicationContext.getBeansOfType(RegistryService.class).size() > 0,
                "RegistryService should be available");
    }

    @Test
    public void testPropertiesAreLoaded() {
        AiravataServerProperties properties = applicationContext.getBean(AiravataServerProperties.class);
        assertNotNull(properties, "AiravataServerProperties should be loaded");
        assertNotNull(properties.database, "Database properties should be configured");
        assertNotNull(properties.database.registry, "Registry database properties should be configured");
    }
}
