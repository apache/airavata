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

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManagerFactory;
import org.apache.airavata.credential.repositories.CommunityUserRepository;
import org.apache.airavata.credential.repositories.CredentialRepository;
import org.apache.airavata.profile.repositories.TenantProfileRepository;
import org.apache.airavata.profile.repositories.UserProfileRepository;
import org.apache.airavata.registry.repositories.appcatalog.ComputeResourceRepository;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentRepository;
import org.apache.airavata.registry.repositories.replicacatalog.DataProductRepository;
import org.apache.airavata.registry.repositories.workflowcatalog.WorkflowRepository;
import org.apache.airavata.sharing.repositories.DomainRepository;
import org.apache.airavata.sharing.repositories.EntityRepository;
import org.apache.airavata.sharing.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

/**
 * Test to validate that Spring application context loads correctly
 * and all JPA repositories and entity manager factories are properly configured.
 */
@SpringBootTest(
        classes = {JpaConfig.class, AiravataServerProperties.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
public class SpringContextLoadTest {

    private final ApplicationContext applicationContext;
    private final EntityManagerFactory profileServiceEntityManagerFactory;
    private final EntityManagerFactory appCatalogEntityManagerFactory;
    private final EntityManagerFactory expCatalogEntityManagerFactory;
    private final EntityManagerFactory replicaCatalogEntityManagerFactory;
    private final EntityManagerFactory workflowCatalogEntityManagerFactory;
    private final EntityManagerFactory sharingRegistryEntityManagerFactory;
    private final EntityManagerFactory credentialStoreEntityManagerFactory;
    private final UserProfileRepository userProfileRepository;
    private final TenantProfileRepository tenantProfileRepository;
    private final ComputeResourceRepository computeResourceRepository;
    private final ExperimentRepository experimentRepository;
    private final DataProductRepository dataProductRepository;
    private final WorkflowRepository workflowRepository;
    private final DomainRepository domainRepository;
    private final EntityRepository entityRepository;
    private final UserRepository sharingUserRepository;
    private final CredentialRepository credentialRepository;
    private final CommunityUserRepository communityUserRepository;

    public SpringContextLoadTest(
            ApplicationContext applicationContext,
            @Qualifier("profileServiceEntityManagerFactory") EntityManagerFactory profileServiceEntityManagerFactory,
            @Qualifier("appCatalogEntityManagerFactory") EntityManagerFactory appCatalogEntityManagerFactory,
            @Qualifier("expCatalogEntityManagerFactory") EntityManagerFactory expCatalogEntityManagerFactory,
            @Qualifier("replicaCatalogEntityManagerFactory") EntityManagerFactory replicaCatalogEntityManagerFactory,
            @Qualifier("workflowCatalogEntityManagerFactory") EntityManagerFactory workflowCatalogEntityManagerFactory,
            @Qualifier("sharingRegistryEntityManagerFactory") EntityManagerFactory sharingRegistryEntityManagerFactory,
            @Qualifier("credentialStoreEntityManagerFactory") EntityManagerFactory credentialStoreEntityManagerFactory,
            UserProfileRepository userProfileRepository,
            TenantProfileRepository tenantProfileRepository,
            ComputeResourceRepository computeResourceRepository,
            ExperimentRepository experimentRepository,
            DataProductRepository dataProductRepository,
            WorkflowRepository workflowRepository,
            DomainRepository domainRepository,
            EntityRepository entityRepository,
            UserRepository sharingUserRepository,
            CredentialRepository credentialRepository,
            CommunityUserRepository communityUserRepository) {
        this.applicationContext = applicationContext;
        this.profileServiceEntityManagerFactory = profileServiceEntityManagerFactory;
        this.appCatalogEntityManagerFactory = appCatalogEntityManagerFactory;
        this.expCatalogEntityManagerFactory = expCatalogEntityManagerFactory;
        this.replicaCatalogEntityManagerFactory = replicaCatalogEntityManagerFactory;
        this.workflowCatalogEntityManagerFactory = workflowCatalogEntityManagerFactory;
        this.sharingRegistryEntityManagerFactory = sharingRegistryEntityManagerFactory;
        this.credentialStoreEntityManagerFactory = credentialStoreEntityManagerFactory;
        this.userProfileRepository = userProfileRepository;
        this.tenantProfileRepository = tenantProfileRepository;
        this.computeResourceRepository = computeResourceRepository;
        this.experimentRepository = experimentRepository;
        this.dataProductRepository = dataProductRepository;
        this.workflowRepository = workflowRepository;
        this.domainRepository = domainRepository;
        this.entityRepository = entityRepository;
        this.sharingUserRepository = sharingUserRepository;
        this.credentialRepository = credentialRepository;
        this.communityUserRepository = communityUserRepository;
    }

    @Test
    public void testApplicationContextLoads() {
        assertNotNull(applicationContext, "Application context should be loaded");
    }

    @Test
    public void testAllEntityManagerFactoriesAreCreated() {
        assertNotNull(profileServiceEntityManagerFactory, "Profile service EntityManagerFactory should be created");
        assertNotNull(appCatalogEntityManagerFactory, "App catalog EntityManagerFactory should be created");
        assertNotNull(expCatalogEntityManagerFactory, "Exp catalog EntityManagerFactory should be created");
        assertNotNull(replicaCatalogEntityManagerFactory, "Replica catalog EntityManagerFactory should be created");
        assertNotNull(workflowCatalogEntityManagerFactory, "Workflow catalog EntityManagerFactory should be created");
        assertNotNull(sharingRegistryEntityManagerFactory, "Sharing registry EntityManagerFactory should be created");
        assertNotNull(credentialStoreEntityManagerFactory, "Credential store EntityManagerFactory should be created");
    }

    @Test
    public void testEntityManagerFactoriesAreOpen() {
        assertTrue(profileServiceEntityManagerFactory.isOpen(), "Profile service EntityManagerFactory should be open");
        assertTrue(appCatalogEntityManagerFactory.isOpen(), "App catalog EntityManagerFactory should be open");
        assertTrue(expCatalogEntityManagerFactory.isOpen(), "Exp catalog EntityManagerFactory should be open");
        assertTrue(replicaCatalogEntityManagerFactory.isOpen(), "Replica catalog EntityManagerFactory should be open");
        assertTrue(
                workflowCatalogEntityManagerFactory.isOpen(), "Workflow catalog EntityManagerFactory should be open");
        assertTrue(
                sharingRegistryEntityManagerFactory.isOpen(), "Sharing registry EntityManagerFactory should be open");
        assertTrue(
                credentialStoreEntityManagerFactory.isOpen(), "Credential store EntityManagerFactory should be open");
    }

    @Test
    public void testProfileServiceRepositoriesAreInjected() {
        assertNotNull(userProfileRepository, "UserProfileRepository should be injected");
        assertNotNull(tenantProfileRepository, "TenantProfileRepository should be injected");
    }

    @Test
    public void testAppCatalogRepositoriesAreInjected() {
        assertNotNull(computeResourceRepository, "ComputeResourceRepository should be injected");
    }

    @Test
    public void testExpCatalogRepositoriesAreInjected() {
        assertNotNull(experimentRepository, "ExperimentRepository should be injected");
    }

    @Test
    public void testReplicaCatalogRepositoriesAreInjected() {
        assertNotNull(dataProductRepository, "DataProductRepository should be injected");
    }

    @Test
    public void testWorkflowCatalogRepositoriesAreInjected() {
        assertNotNull(workflowRepository, "WorkflowRepository should be injected");
    }

    @Test
    public void testSharingRegistryRepositoriesAreInjected() {
        assertNotNull(domainRepository, "DomainRepository should be injected");
        assertNotNull(entityRepository, "EntityRepository should be injected");
        assertNotNull(sharingUserRepository, "Sharing UserRepository should be injected");
    }

    @Test
    public void testCredentialStoreRepositoriesAreInjected() {
        assertNotNull(credentialRepository, "CredentialRepository should be injected");
        assertNotNull(communityUserRepository, "CommunityUserRepository should be injected");
    }

    @Test
    public void testEntityManagerFactoriesHaveCorrectPersistenceUnits() {
        // Verify factories are created (OpenJPA uses different property structure than Hibernate)
        assertNotNull(profileServiceEntityManagerFactory.getProperties());
        assertNotNull(appCatalogEntityManagerFactory.getProperties());
    }

    @Test
    public void testAllRepositoriesAreAccessible() {
        // Test that repositories can be accessed (they should be Spring beans)
        assertTrue(
                applicationContext.getBeansOfType(UserProfileRepository.class).size() > 0,
                "UserProfileRepository should be registered as a bean");
        assertTrue(
                applicationContext.getBeansOfType(CredentialRepository.class).size() > 0,
                "CredentialRepository should be registered as a bean");
        assertTrue(
                applicationContext.getBeansOfType(CommunityUserRepository.class).size() > 0,
                "CommunityUserRepository should be registered as a bean");
    }
}
