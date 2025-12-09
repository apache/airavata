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

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.apache.airavata.common.utils.JPAUtils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration for multiple JPA persistence units.
 * Spring Boot typically expects a single persistence unit, so we need
 * custom configuration for each of the 6 persistence units.
 */
@Configuration
@EnableTransactionManagement
public class JpaConfig {

    @Autowired
    private AiravataServerProperties properties;

    @Bean
    public static OpenJpaEntityManagerFactoryPostProcessor openJpaEntityManagerFactoryPostProcessor() {
        return new OpenJpaEntityManagerFactoryPostProcessor();
    }

    /**
     * Custom JpaMetamodelMappingContext factory that handles OpenJPA enhancement errors gracefully.
     * This overrides the default Spring Data JPA factory which doesn't handle OpenJPA enhancement issues.
     */
    @Bean(name = "jpaMappingContext")
    public OpenJpaMetamodelMappingContextFactoryBean jpaMappingContext(
            @Qualifier("profileServiceEntityManagerFactory") EntityManagerFactory profileServiceEmf,
            @Qualifier("appCatalogEntityManagerFactory") EntityManagerFactory appCatalogEmf,
            @Qualifier("expCatalogEntityManagerFactory") EntityManagerFactory expCatalogEmf,
            @Qualifier("replicaCatalogEntityManagerFactory") EntityManagerFactory replicaCatalogEmf,
            @Qualifier("workflowCatalogEntityManagerFactory") EntityManagerFactory workflowCatalogEmf,
            @Qualifier("sharingRegistryEntityManagerFactory") EntityManagerFactory sharingRegistryEmf,
            @Qualifier("credentialStoreEntityManagerFactory") EntityManagerFactory credentialStoreEmf) {
        OpenJpaMetamodelMappingContextFactoryBean factory = new OpenJpaMetamodelMappingContextFactoryBean();
        factory.setEntityManagerFactories(java.util.Arrays.asList(
                profileServiceEmf, appCatalogEmf, expCatalogEmf, replicaCatalogEmf,
                workflowCatalogEmf, sharingRegistryEmf, credentialStoreEmf));
        return factory;
    }

    // Persistence unit names
    public static final String PROFILE_SERVICE_PU = "profile_service";
    public static final String APPCATALOG_PU = "appcatalog_data_new";
    public static final String EXPCATALOG_PU = "experiment_data_new";
    public static final String REPLICACATALOG_PU = "replicacatalog_data_new";
    public static final String WORKFLOWCATALOG_PU = "workflowcatalog_data_new";
    public static final String SHARING_REGISTRY_PU = "airavata-sharing-registry";
    public static final String CREDENTIAL_STORE_PU = "credential_store";

    @Bean(name = "profileServiceEntityManagerFactory")
    @Primary
    public EntityManagerFactory profileServiceEntityManagerFactory() {
        var db = properties.database.profile;
        if (db == null || db.url == null || db.url.isEmpty()) {
            throw new IllegalStateException(
                    "Database configuration for profile service is missing or invalid. Check airavata.properties for database.profile.url");
        }
        return JPAUtils.getEntityManagerFactory(
                PROFILE_SERVICE_PU, db.driver, db.url, db.user, db.password, db.validationQuery);
    }

    @Bean(name = "appCatalogEntityManagerFactory")
    public EntityManagerFactory appCatalogEntityManagerFactory() {
        var db = properties.database.catalog;
        if (db == null || db.url == null || db.url.isEmpty()) {
            throw new IllegalStateException(
                    "Database configuration for app catalog is missing or invalid. Check airavata.properties for database.catalog.url");
        }
        return JPAUtils.getEntityManagerFactory(
                APPCATALOG_PU, db.driver, db.url, db.user, db.password, db.validationQuery);
    }

    @Bean(name = "registryDataSource")
    public DataSource registryDataSource() {
        var db = properties.database.registry;
        if (db == null || db.url == null || db.url.isEmpty()) {
            throw new IllegalStateException(
                    "Database configuration for registry is missing or invalid. Check airavata.properties for database.registry.url");
        }
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(db.driver);
        dataSource.setUrl(db.url);
        dataSource.setUsername(db.user);
        dataSource.setPassword(db.password);
        dataSource.setValidationQuery(properties.database.validationQuery);
        dataSource.setTestOnBorrow(true);
        return dataSource;
    }

    @Bean(name = "expCatalogEntityManagerFactory")
    public EntityManagerFactory expCatalogEntityManagerFactory() {
        var db = properties.database.registry;
        if (db == null || db.url == null || db.url.isEmpty()) {
            throw new IllegalStateException(
                    "Database configuration for experiment catalog is missing or invalid. Check airavata.properties for database.registry.url");
        }
        return JPAUtils.getEntityManagerFactory(
                EXPCATALOG_PU, db.driver, db.url, db.user, db.password, properties.database.validationQuery);
    }

    @Bean(name = "replicaCatalogEntityManagerFactory")
    public EntityManagerFactory replicaCatalogEntityManagerFactory() {
        var db = properties.database.replica;
        if (db == null || db.url == null || db.url.isEmpty()) {
            throw new IllegalStateException(
                    "Database configuration for replica catalog is missing or invalid. Check airavata.properties for database.replica.url");
        }
        return JPAUtils.getEntityManagerFactory(
                REPLICACATALOG_PU, db.driver, db.url, db.user, db.password, db.validationQuery);
    }

    @Bean(name = "workflowCatalogEntityManagerFactory")
    public EntityManagerFactory workflowCatalogEntityManagerFactory() {
        var db = properties.database.workflow;
        if (db == null || db.url == null || db.url.isEmpty()) {
            throw new IllegalStateException(
                    "Database configuration for workflow catalog is missing or invalid. Check airavata.properties for database.workflow.url");
        }
        return JPAUtils.getEntityManagerFactory(
                WORKFLOWCATALOG_PU, db.driver, db.url, db.user, db.password, db.validationQuery);
    }

    @Bean(name = "sharingRegistryEntityManagerFactory")
    public EntityManagerFactory sharingRegistryEntityManagerFactory() {
        var db = properties.database.sharing;
        if (db == null || db.url == null || db.url.isEmpty()) {
            throw new IllegalStateException(
                    "Database configuration for sharing registry is missing or invalid. Check airavata.properties for database.sharing.url");
        }
        return JPAUtils.getEntityManagerFactory(
                SHARING_REGISTRY_PU, db.driver, db.url, db.user, db.password, db.validationQuery);
    }

    @Bean(name = "credentialStoreEntityManagerFactory")
    public EntityManagerFactory credentialStoreEntityManagerFactory() {
        var db = properties.database.vault;
        // Fallback to registry database if vault DB not configured
        String url = (db != null && db.url != null && !db.url.isEmpty()) ? db.url : properties.database.registry.url;
        String user =
                (db != null && db.user != null && !db.user.isEmpty()) ? db.user : properties.database.registry.user;
        String password = (db != null && db.password != null && !db.password.isEmpty())
                ? db.password
                : properties.database.registry.password;
        String driver = (db != null && db.driver != null && !db.driver.isEmpty())
                ? db.driver
                : properties.database.registry.driver;
        String validationQuery = (db != null && db.validationQuery != null && !db.validationQuery.isEmpty())
                ? db.validationQuery
                : properties.database.registry.validationQuery;

        if (url == null || url.isEmpty()) {
            throw new IllegalStateException(
                    "Database configuration for credential store is missing or invalid. Check airavata.properties for database.vault.url or database.registry.url");
        }
        return JPAUtils.getEntityManagerFactory(CREDENTIAL_STORE_PU, driver, url, user, password, validationQuery);
    }

    // Transaction managers for each persistence unit
    @Bean(name = "profileServiceTransactionManager")
    @Primary
    public PlatformTransactionManager profileServiceTransactionManager() {
        return new JpaTransactionManager(profileServiceEntityManagerFactory());
    }

    @Bean(name = "appCatalogTransactionManager")
    public PlatformTransactionManager appCatalogTransactionManager() {
        return new JpaTransactionManager(appCatalogEntityManagerFactory());
    }

    @Bean(name = "expCatalogTransactionManager")
    public PlatformTransactionManager expCatalogTransactionManager() {
        return new JpaTransactionManager(expCatalogEntityManagerFactory());
    }

    @Bean(name = "replicaCatalogTransactionManager")
    public PlatformTransactionManager replicaCatalogTransactionManager() {
        return new JpaTransactionManager(replicaCatalogEntityManagerFactory());
    }

    @Bean(name = "workflowCatalogTransactionManager")
    public PlatformTransactionManager workflowCatalogTransactionManager() {
        return new JpaTransactionManager(workflowCatalogEntityManagerFactory());
    }

    @Bean(name = "sharingRegistryTransactionManager")
    public PlatformTransactionManager sharingRegistryTransactionManager() {
        return new JpaTransactionManager(sharingRegistryEntityManagerFactory());
    }

    @Bean(name = "credentialStoreTransactionManager")
    public PlatformTransactionManager credentialStoreTransactionManager() {
        return new JpaTransactionManager(credentialStoreEntityManagerFactory());
    }

    // EntityManager beans for injection into repositories
    // Note: EntityManagers are thread-safe when used with Spring's transaction management
    // We use prototype scope to create new instances as needed
    @Bean(name = "appCatalogEntityManager")
    @Scope("prototype")
    public EntityManager appCatalogEntityManager(
            @Qualifier("appCatalogEntityManagerFactory") EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    @Bean(name = "expCatalogEntityManager")
    @Scope("prototype")
    public EntityManager expCatalogEntityManager(
            @Qualifier("expCatalogEntityManagerFactory") EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    @Bean(name = "replicaCatalogEntityManager")
    @Scope("prototype")
    public EntityManager replicaCatalogEntityManager(
            @Qualifier("replicaCatalogEntityManagerFactory") EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    @Bean(name = "workflowCatalogEntityManager")
    @Scope("prototype")
    public EntityManager workflowCatalogEntityManager(
            @Qualifier("workflowCatalogEntityManagerFactory") EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    @Bean(name = "profileServiceEntityManager")
    @Scope("prototype")
    @Primary
    public EntityManager profileServiceEntityManager(
            @Qualifier("profileServiceEntityManagerFactory") EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    @Bean(name = "sharingRegistryEntityManager")
    @Scope("prototype")
    public EntityManager sharingRegistryEntityManager(
            @Qualifier("sharingRegistryEntityManagerFactory") EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    @Bean(name = "credentialStoreEntityManager")
    @Scope("prototype")
    public EntityManager credentialStoreEntityManager(
            @Qualifier("credentialStoreEntityManagerFactory") EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    // Spring Data JPA Repository Configuration for each persistence unit
    // Note: Order matters - repositories registered later will override earlier ones with the same name
    // We register expcatalog last so it's not overridden by sharing's UserRepository
    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.profile.repositories",
            entityManagerFactoryRef = "profileServiceEntityManagerFactory",
            transactionManagerRef = "profileServiceTransactionManager",
            enableDefaultTransactions = true)
    static class ProfileServiceJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.registry.repositories.appcatalog",
            entityManagerFactoryRef = "appCatalogEntityManagerFactory",
            transactionManagerRef = "appCatalogTransactionManager")
    static class AppCatalogJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.registry.repositories.replicacatalog",
            entityManagerFactoryRef = "replicaCatalogEntityManagerFactory",
            transactionManagerRef = "replicaCatalogTransactionManager")
    static class ReplicaCatalogJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.registry.repositories.workflowcatalog",
            entityManagerFactoryRef = "workflowCatalogEntityManagerFactory",
            transactionManagerRef = "workflowCatalogTransactionManager")
    static class WorkflowCatalogJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.sharing.repositories",
            entityManagerFactoryRef = "sharingRegistryEntityManagerFactory",
            transactionManagerRef = "sharingRegistryTransactionManager")
    static class SharingRegistryJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.credential.repositories",
            entityManagerFactoryRef = "credentialStoreEntityManagerFactory",
            transactionManagerRef = "credentialStoreTransactionManager")
    static class CredentialStoreJpaRepositoriesConfig {}

    // Register expcatalog LAST so its UserRepository (marked as @Primary) overrides sharing's UserRepository
    // This must be the last @Configuration class to ensure expcatalog repository is the final bean
    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.registry.repositories.expcatalog",
            entityManagerFactoryRef = "expCatalogEntityManagerFactory",
            transactionManagerRef = "expCatalogTransactionManager")
    static class ExpCatalogJpaRepositoriesConfig {}
}
