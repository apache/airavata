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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration for multiple JPA persistence units using Spring-provided methods.
 * Uses LocalContainerEntityManagerFactoryBean for each of the 7 persistence units
 * to integrate with Spring's transaction management and lifecycle.
 *
 * <p>This configuration is only enabled when at least one API service is enabled
 * (services.rest.enabled OR services.thrift.enabled). If neither is enabled,
 * no database access or persistence will be initialized.
 */
@Configuration
@EnableTransactionManagement
@ConditionalOnExpression("${services.rest.enabled:false} == true || ${services.thrift.enabled:true} == true")
public class JpaConfig {

    private final AiravataServerProperties properties;
    private final Environment environment;

    public JpaConfig(AiravataServerProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    // Persistence unit names (match database names for consistency)
    public static final String PROFILE_SERVICE_PU = "profile_service";
    public static final String APPCATALOG_PU = "app_catalog";
    public static final String EXPCATALOG_PU = "experiment_catalog";
    public static final String REPLICACATALOG_PU = "replica_catalog";
    public static final String WORKFLOWCATALOG_PU = "workflow_catalog";
    public static final String SHARING_REGISTRY_PU = "sharing_registry";
    public static final String CREDENTIAL_STORE_PU = "credential_store";
    public static final String RESEARCH_CATALOG_PU = "research_catalog";

    // Helper method to create HikariCP DataSource
    private DataSource createDataSource(
            String driver, String url, String user, String password, String validationQuery) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        // Append MySQL/MariaDB connection parameters
        String jdbcUrl = url;
        if (url != null
                && driver != null
                && (driver.toLowerCase().contains("mysql")
                        || driver.toLowerCase().contains("mariadb"))) {
            String separator = url.contains("?") ? "&" : "?";
            jdbcUrl = url + separator + "autoReconnect=true&tinyInt1isBit=false";
        }
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(password);
        if (validationQuery != null && !validationQuery.isEmpty()) {
            config.setConnectionTestQuery(validationQuery);
        }
        config.setMinimumIdle(2);
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        return new HikariDataSource(config);
    }

    // Helper method to create JPA properties
    private Properties createJpaProperties(String url) {
        Properties props = new Properties();
        // Check if we're in test profile - use create-drop for tests, validate for production
        boolean isTestProfile =
                environment != null && environment.acceptsProfiles(org.springframework.core.env.Profiles.of("test"));

        // Hibernate mode: create-drop for tests (creates schema on startup, drops on shutdown), validate for production
        props.put("hibernate.hbm2ddl.auto", isTestProfile ? "create-drop" : "validate");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "false");
        props.put("hibernate.connection.provider_disables_autocommit", "true");
        props.put("hibernate.archive.scanner", "org.hibernate.boot.archive.scan.internal.StandardScanner");
        // Always use MySQL dialect (MariaDB is compatible)
        props.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        return props;
    }

    // Helper method to create LocalContainerEntityManagerFactoryBean
    private LocalContainerEntityManagerFactoryBean createEntityManagerFactory(
            String persistenceUnitName, DataSource dataSource, String... packagesToScan) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);

        // Configure programmatically using packagesToScan - this avoids persistence.xml lookup
        // Setting packagesToScan tells Spring to use programmatic configuration
        emf.setPackagesToScan(packagesToScan);
        // Set persistence unit name for reference (used by some legacy code)
        // Note: When packagesToScan is set, Spring uses programmatic config and doesn't load persistence.xml
        emf.setPersistenceUnitName(persistenceUnitName);

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        emf.setJpaVendorAdapter(vendorAdapter);

        String url = ((HikariDataSource) dataSource).getJdbcUrl();
        emf.setJpaProperties(createJpaProperties(url));

        return emf;
    }

    // DataSource beans
    // These are only created when not in test profile (TestcontainersConfig provides DataSources for tests)
    @Bean
    @Primary
    @Profile("!test")
    public DataSource profileServiceDataSource() {
        var db = properties.database.profile;
        if (db == null || db.url == null || db.url.isEmpty()) {
            throw new IllegalStateException(
                    "Database configuration for profile service is missing or invalid. Check airavata.properties for database.profile.url");
        }
        return createDataSource(db.driver, db.url, db.user, db.password, db.validationQuery);
    }

    @Bean
    @Profile("!test")
    public DataSource appCatalogDataSource() {
        var db = properties.database.catalog;
        if (db == null || db.url == null || db.url.isEmpty()) {
            throw new IllegalStateException(
                    "Database configuration for app catalog is missing or invalid. Check airavata.properties for database.catalog.url");
        }
        return createDataSource(db.driver, db.url, db.user, db.password, db.validationQuery);
    }

    @Bean
    @Profile("!test")
    public DataSource registryDataSource() {
        var db = properties.database.registry;
        if (db == null || db.url == null || db.url.isEmpty()) {
            throw new IllegalStateException(
                    "Database configuration for registry is missing or invalid. Check airavata.properties for database.registry.url");
        }
        return createDataSource(db.driver, db.url, db.user, db.password, properties.database.validationQuery);
    }

    @Bean
    @Profile("!test")
    public DataSource replicaDataSource() {
        var db = properties.database.replica;
        if (db == null || db.url == null || db.url.isEmpty()) {
            throw new IllegalStateException(
                    "Database configuration for replica catalog is missing or invalid. Check airavata.properties for database.replica.url");
        }
        return createDataSource(db.driver, db.url, db.user, db.password, db.validationQuery);
    }

    @Bean
    @Profile("!test")
    public DataSource workflowDataSource() {
        var db = properties.database.workflow;
        if (db == null || db.url == null || db.url.isEmpty()) {
            throw new IllegalStateException(
                    "Database configuration for workflow catalog is missing or invalid. Check airavata.properties for database.workflow.url");
        }
        return createDataSource(db.driver, db.url, db.user, db.password, db.validationQuery);
    }

    @Bean
    @Profile("!test")
    public DataSource sharingDataSource() {
        var db = properties.database.sharing;
        if (db == null || db.url == null || db.url.isEmpty()) {
            throw new IllegalStateException(
                    "Database configuration for sharing registry is missing or invalid. Check airavata.properties for database.sharing.url");
        }
        return createDataSource(db.driver, db.url, db.user, db.password, db.validationQuery);
    }

    @Bean
    @Profile("!test")
    public DataSource credentialStoreDataSource() {
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
        String validationQuery = properties.database.validationQuery;

        return createDataSource(driver, url, user, password, validationQuery);
    }

    @Bean
    @Profile("!test")
    public DataSource researchCatalogDataSource() {
        var db = properties.database.research;
        if (db == null || db.url == null || db.url.isEmpty()) {
            throw new IllegalStateException(
                    "Database configuration for research catalog is missing or invalid. Check airavata.properties for database.research.url");
        }
        return createDataSource(db.driver, db.url, db.user, db.password, db.validationQuery);
    }

    // EntityManagerFactory beans using Spring's LocalContainerEntityManagerFactoryBean
    @Bean
    @Primary
    @DependsOn("profileServiceDataSource")
    public LocalContainerEntityManagerFactoryBean profileServiceEntityManagerFactory(
            @Qualifier("profileServiceDataSource") DataSource dataSource) {
        return createEntityManagerFactory(PROFILE_SERVICE_PU, dataSource, "org.apache.airavata.profile.entities");
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean appCatalogEntityManagerFactory(
            @Qualifier("appCatalogDataSource") DataSource dataSource) {
        return createEntityManagerFactory(
                APPCATALOG_PU, dataSource, "org.apache.airavata.registry.entities.appcatalog");
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean expCatalogEntityManagerFactory(
            @Qualifier("registryDataSource") DataSource dataSource) {
        return createEntityManagerFactory(
                EXPCATALOG_PU, dataSource, "org.apache.airavata.registry.entities.expcatalog");
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean replicaCatalogEntityManagerFactory(
            @Qualifier("replicaDataSource") DataSource dataSource) {
        return createEntityManagerFactory(
                REPLICACATALOG_PU, dataSource, "org.apache.airavata.registry.entities.replicacatalog");
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean workflowCatalogEntityManagerFactory(
            @Qualifier("workflowDataSource") DataSource dataSource) {
        return createEntityManagerFactory(
                WORKFLOWCATALOG_PU, dataSource, "org.apache.airavata.registry.entities.airavataworkflowcatalog");
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean sharingRegistryEntityManagerFactory(
            @Qualifier("sharingDataSource") DataSource dataSource) {
        return createEntityManagerFactory(SHARING_REGISTRY_PU, dataSource, "org.apache.airavata.sharing.entities");
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean credentialStoreEntityManagerFactory(
            @Qualifier("credentialStoreDataSource") DataSource dataSource) {
        return createEntityManagerFactory(CREDENTIAL_STORE_PU, dataSource, "org.apache.airavata.credential.entities");
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean researchCatalogEntityManagerFactory(
            @Qualifier("researchCatalogDataSource") DataSource dataSource) {
        return createEntityManagerFactory(
                RESEARCH_CATALOG_PU, dataSource, "org.apache.airavata.research.service.model.entity");
    }

    @Bean
    public PlatformTransactionManager profileServiceTransactionManager(
            @Qualifier("profileServiceEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    @Primary
    public PlatformTransactionManager appCatalogTransactionManager(
            @Qualifier("appCatalogEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public PlatformTransactionManager expCatalogTransactionManager(
            @Qualifier("expCatalogEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public PlatformTransactionManager replicaCatalogTransactionManager(
            @Qualifier("replicaCatalogEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public PlatformTransactionManager workflowCatalogTransactionManager(
            @Qualifier("workflowCatalogEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public PlatformTransactionManager sharingRegistryTransactionManager(
            @Qualifier("sharingRegistryEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public PlatformTransactionManager credentialStoreTransactionManager(
            @Qualifier("credentialStoreEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public PlatformTransactionManager researchCatalogTransactionManager(
            @Qualifier("researchCatalogEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    // EntityManager beans with qualifiers for direct injection
    // Using SharedEntityManagerCreator to create proxy EntityManagers that participate in transactions
    @Bean
    public EntityManager profileServiceEntityManager(
            @Qualifier("profileServiceEntityManagerFactory") LocalContainerEntityManagerFactoryBean emfBean) {
        return SharedEntityManagerCreator.createSharedEntityManager(emfBean.getObject());
    }

    @Bean
    public EntityManager appCatalogEntityManager(
            @Qualifier("appCatalogEntityManagerFactory") LocalContainerEntityManagerFactoryBean emfBean) {
        return SharedEntityManagerCreator.createSharedEntityManager(emfBean.getObject());
    }

    @Bean
    public EntityManager expCatalogEntityManager(
            @Qualifier("expCatalogEntityManagerFactory") LocalContainerEntityManagerFactoryBean emfBean) {
        return SharedEntityManagerCreator.createSharedEntityManager(emfBean.getObject());
    }

    @Bean
    public EntityManager replicaCatalogEntityManager(
            @Qualifier("replicaCatalogEntityManagerFactory") LocalContainerEntityManagerFactoryBean emfBean) {
        return SharedEntityManagerCreator.createSharedEntityManager(emfBean.getObject());
    }

    @Bean
    public EntityManager workflowCatalogEntityManager(
            @Qualifier("workflowCatalogEntityManagerFactory") LocalContainerEntityManagerFactoryBean emfBean) {
        return SharedEntityManagerCreator.createSharedEntityManager(emfBean.getObject());
    }

    @Bean
    public EntityManager sharingRegistryEntityManager(
            @Qualifier("sharingRegistryEntityManagerFactory") LocalContainerEntityManagerFactoryBean emfBean) {
        return SharedEntityManagerCreator.createSharedEntityManager(emfBean.getObject());
    }

    @Bean
    public EntityManager credentialStoreEntityManager(
            @Qualifier("credentialStoreEntityManagerFactory") LocalContainerEntityManagerFactoryBean emfBean) {
        return SharedEntityManagerCreator.createSharedEntityManager(emfBean.getObject());
    }

    @Bean
    public EntityManager researchCatalogEntityManager(
            @Qualifier("researchCatalogEntityManagerFactory") LocalContainerEntityManagerFactoryBean emfBean) {
        return SharedEntityManagerCreator.createSharedEntityManager(emfBean.getObject());
    }

    // Spring Data JPA Repository Configuration for each persistence unit
    // Each configuration uses its own EntityManagerFactory and TransactionManager
    // Spring Data JPA will automatically create a mapping context for each configuration
    // Note: Order matters - repositories registered later will override earlier ones with the same name
    // We register expcatalog last so it's not overridden by sharing's UserRepository
    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.profile.repositories",
            entityManagerFactoryRef = "profileServiceEntityManagerFactory",
            transactionManagerRef = "profileServiceTransactionManager",
            enableDefaultTransactions = true,
            considerNestedRepositories = true)
    static class ProfileServiceJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.registry.repositories.appcatalog",
            entityManagerFactoryRef = "appCatalogEntityManagerFactory",
            transactionManagerRef = "appCatalogTransactionManager",
            considerNestedRepositories = true)
    static class AppCatalogJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.registry.repositories.replicacatalog",
            entityManagerFactoryRef = "replicaCatalogEntityManagerFactory",
            transactionManagerRef = "replicaCatalogTransactionManager",
            considerNestedRepositories = true)
    static class ReplicaCatalogJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.registry.repositories.workflowcatalog",
            entityManagerFactoryRef = "workflowCatalogEntityManagerFactory",
            transactionManagerRef = "workflowCatalogTransactionManager",
            considerNestedRepositories = true)
    static class WorkflowCatalogJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.sharing.repositories",
            entityManagerFactoryRef = "sharingRegistryEntityManagerFactory",
            transactionManagerRef = "sharingRegistryTransactionManager",
            considerNestedRepositories = true)
    static class SharingRegistryJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.credential.repositories",
            entityManagerFactoryRef = "credentialStoreEntityManagerFactory",
            transactionManagerRef = "credentialStoreTransactionManager",
            considerNestedRepositories = true)
    static class CredentialStoreJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.research.service.model.repo",
            entityManagerFactoryRef = "researchCatalogEntityManagerFactory",
            transactionManagerRef = "researchCatalogTransactionManager",
            considerNestedRepositories = true)
    static class ResearchCatalogJpaRepositoriesConfig {}

    // Register expcatalog LAST so its UserRepository (marked as @Primary) overrides sharing's UserRepository
    // This must be the last @Configuration class to ensure expcatalog repository is the final bean
    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.registry.repositories.expcatalog",
            entityManagerFactoryRef = "expCatalogEntityManagerFactory",
            transactionManagerRef = "expCatalogTransactionManager",
            considerNestedRepositories = true)
    static class ExpCatalogJpaRepositoriesConfig {}
}
