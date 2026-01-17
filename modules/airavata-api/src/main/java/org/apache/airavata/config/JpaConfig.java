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
import org.apache.airavata.config.conditional.ConditionalOnApiService;
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
@ConditionalOnApiService
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

    // Helper method to get property from environment with fallback
    private String getEnvProperty(String key, String defaultValue) {
        String value = environment.getProperty(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
    
    // Helper method to create DataSource from environment properties
    private DataSource createDataSourceFromEnv(String prefix, String poolName) {
        String url = environment.getProperty(prefix + ".url");
        if (url == null || url.isEmpty()) {
            throw new IllegalStateException(
                    "Database configuration missing: " + prefix + ".url is not set in application.properties");
        }
        String driver = getEnvProperty(prefix + ".driver", "org.mariadb.jdbc.Driver");
        String user = environment.getProperty(prefix + ".user");
        String password = environment.getProperty(prefix + ".password");
        String validationQuery = getEnvProperty(prefix + ".validation-query", "SELECT 1");
        return createDataSource(driver, url, user, password, validationQuery, poolName);
    }

    // Helper method to create HikariCP DataSource
    private DataSource createDataSource(
            String driver, String url, String user, String password, String validationQuery, String poolName) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        // Set pool name for better identification in logs
        if (poolName != null && !poolName.isEmpty()) {
            config.setPoolName(poolName);
        }
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
        // Register MBeans for monitoring
        config.setRegisterMbeans(true);
        // Don't fail application startup if database is not available
        // Connection will be established lazily when needed
        config.setInitializationFailTimeout(-1);
        return new HikariDataSource(config);
    }

    // Helper method to create JPA properties
    private Properties createJpaProperties(String url) {
        Properties props = new Properties();
        // Check if we're in test profile - use create-drop for tests, validate for production
        boolean isTestProfile =
                environment != null && environment.acceptsProfiles(org.springframework.core.env.Profiles.of("test"));

        // Set dialect explicitly based on JDBC URL to avoid metadata access issues
        // Hibernate 6+ requires dialect when jdbc metadata access is disabled
        if (url != null) {
            String urlLower = url.toLowerCase();
            if (urlLower.contains("mariadb")) {
                props.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
            } else if (urlLower.contains("mysql")) {
                props.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
            } else if (urlLower.contains("h2")) {
                props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            } else if (urlLower.contains("postgresql")) {
                props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            }
            // If none match, Hibernate will auto-detect (requires metadata access)
        }
        
        // Hibernate mode: create-drop for tests (creates schema on startup, drops on shutdown),
        // none for production when database might not be available (skip validation)
        // Can be overridden via airavata.hibernate.hbm2ddl-auto property
        String hbm2ddlAuto = environment != null ? environment.getProperty("airavata.hibernate.hbm2ddl-auto") : null;
        if (hbm2ddlAuto == null) {
            props.put("hibernate.hbm2ddl.auto", isTestProfile ? "create-drop" : "none");
        } else {
            props.put("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        }
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "false");
        props.put("hibernate.connection.provider_disables_autocommit", "true");
        props.put("hibernate.archive.scanner", "org.hibernate.boot.archive.scan.internal.StandardScanner");
        // Set reasonable cache sizes (must be at least twice concurrencyLevel)
        props.put("hibernate.query.plan_cache_max_size", "2048");
        props.put("hibernate.query.plan_parameter_metadata_max_size", "128");
        // Disable query validation to avoid database connection during startup
        props.put("hibernate.query.validate_queries", "false");
        props.put("spring.jpa.properties.hibernate.query.validate_queries", "false");
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
        Properties jpaProps = createJpaProperties(url);
        // Use new property name instead of deprecated one
        jpaProps.put("hibernate.boot.allow_jdbc_metadata_access", "false");
        // Use physical naming strategy to ensure column names are used as-is
        jpaProps.put(
                "hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
        jpaProps.put(
                "hibernate.implicit_naming_strategy",
                "org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl");
        emf.setJpaProperties(jpaProps);

        return emf;
    }

    // DataSource beans
    // These are only created when not in test profile (TestcontainersConfig provides DataSources for tests)
    @Bean
    @Primary
    @Profile("!test")
    public DataSource profileServiceDataSource() {
        return createDataSourceFromEnv("airavata.database.profile", "ProfileServicePool");
    }

    @Bean
    @Profile("!test")
    public DataSource appCatalogDataSource() {
        return createDataSourceFromEnv("airavata.database.catalog", "AppCatalogPool");
    }

    @Bean
    @Profile("!test")
    public DataSource registryDataSource() {
        return createDataSourceFromEnv("airavata.database.registry", "RegistryPool");
    }

    @Bean
    @Profile("!test")
    public DataSource replicaDataSource() {
        return createDataSourceFromEnv("airavata.database.replica", "ReplicaPool");
    }

    @Bean
    @Profile("!test")
    public DataSource workflowDataSource() {
        return createDataSourceFromEnv("airavata.database.workflow", "WorkflowPool");
    }

    @Bean
    @Profile("!test")
    public DataSource sharingDataSource() {
        return createDataSourceFromEnv("airavata.database.sharing", "SharingPool");
    }

    @Bean
    @Profile("!test")
    public DataSource credentialStoreDataSource() {
        // Fallback to registry database if vault DB not configured
        String url = environment.getProperty("airavata.database.vault.url");
        if (url == null || url.isEmpty()) {
            url = environment.getProperty("airavata.database.registry.url");
        }
        String user = getEnvProperty("airavata.database.vault.user", environment.getProperty("airavata.database.registry.user"));
        String password = getEnvProperty("airavata.database.vault.password", environment.getProperty("airavata.database.registry.password"));
        String driver = getEnvProperty("airavata.database.vault.driver", getEnvProperty("airavata.database.registry.driver", "org.mariadb.jdbc.Driver"));
        String validationQuery = getEnvProperty("airavata.database.validation-query", "SELECT 1");
        
        return createDataSource(driver, url, user, password, validationQuery, "CredentialStorePool");
    }

    @Bean
    @Profile("!test")
    public DataSource researchCatalogDataSource() {
        return createDataSourceFromEnv("airavata.database.research", "ResearchCatalogPool");
    }

    // EntityManagerFactory beans using Spring's LocalContainerEntityManagerFactoryBean
    @Bean
    @DependsOn("profileServiceDataSource")
    public LocalContainerEntityManagerFactoryBean profileServiceEntityManagerFactoryBean(
            @Qualifier("profileServiceDataSource") DataSource dataSource) {
        return createEntityManagerFactory(PROFILE_SERVICE_PU, dataSource, "org.apache.airavata.profile.entities");
    }

    // Expose the actual EntityManagerFactory for Spring Data JPA
    @Bean
    @Primary
    @DependsOn("profileServiceEntityManagerFactoryBean")
    public EntityManagerFactory profileServiceEntityManagerFactory(
            @Qualifier("profileServiceEntityManagerFactoryBean") LocalContainerEntityManagerFactoryBean emfBean) {
        return emfBean.getObject();
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
    @org.springframework.context.annotation.Lazy
    public PlatformTransactionManager profileServiceTransactionManager(
            @Qualifier("profileServiceEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    @Primary
    public PlatformTransactionManager appCatalogTransactionManager(
            @Qualifier("appCatalogEntityManagerFactory") LocalContainerEntityManagerFactoryBean emfBean) {
        return new JpaTransactionManager(emfBean.getObject());
    }

    @Bean
    public PlatformTransactionManager expCatalogTransactionManager(
            @Qualifier("expCatalogEntityManagerFactory") LocalContainerEntityManagerFactoryBean emfBean) {
        return new JpaTransactionManager(emfBean.getObject());
    }

    @Bean
    public PlatformTransactionManager replicaCatalogTransactionManager(
            @Qualifier("replicaCatalogEntityManagerFactory") LocalContainerEntityManagerFactoryBean emfBean) {
        return new JpaTransactionManager(emfBean.getObject());
    }

    @Bean
    public PlatformTransactionManager workflowCatalogTransactionManager(
            @Qualifier("workflowCatalogEntityManagerFactory") LocalContainerEntityManagerFactoryBean emfBean) {
        return new JpaTransactionManager(emfBean.getObject());
    }

    @Bean
    public PlatformTransactionManager sharingRegistryTransactionManager(
            @Qualifier("sharingRegistryEntityManagerFactory") LocalContainerEntityManagerFactoryBean emfBean) {
        return new JpaTransactionManager(emfBean.getObject());
    }

    @Bean
    public PlatformTransactionManager credentialStoreTransactionManager(
            @Qualifier("credentialStoreEntityManagerFactory") LocalContainerEntityManagerFactoryBean emfBean) {
        return new JpaTransactionManager(emfBean.getObject());
    }

    @Bean
    public PlatformTransactionManager researchCatalogTransactionManager(
            @Qualifier("researchCatalogEntityManagerFactory") LocalContainerEntityManagerFactoryBean emfBean) {
        return new JpaTransactionManager(emfBean.getObject());
    }

    // EntityManager beans with qualifiers for direct injection
    // Using SharedEntityManagerCreator to create proxy EntityManagers that participate in transactions
    @Bean
    public EntityManager profileServiceEntityManager(
            @Qualifier("profileServiceEntityManagerFactory") EntityManagerFactory emf) {
        return SharedEntityManagerCreator.createSharedEntityManager(emf);
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
            considerNestedRepositories = true,
            bootstrapMode = org.springframework.data.repository.config.BootstrapMode.LAZY)
    @DependsOn({"profileServiceEntityManagerFactoryBean"})
    static class ProfileServiceJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.registry.repositories.appcatalog",
            entityManagerFactoryRef = "appCatalogEntityManagerFactory",
            transactionManagerRef = "appCatalogTransactionManager",
            considerNestedRepositories = true,
            bootstrapMode = org.springframework.data.repository.config.BootstrapMode.LAZY)
    static class AppCatalogJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.registry.repositories.replicacatalog",
            entityManagerFactoryRef = "replicaCatalogEntityManagerFactory",
            transactionManagerRef = "replicaCatalogTransactionManager",
            considerNestedRepositories = true,
            bootstrapMode = org.springframework.data.repository.config.BootstrapMode.LAZY)
    static class ReplicaCatalogJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.registry.repositories.workflowcatalog",
            entityManagerFactoryRef = "workflowCatalogEntityManagerFactory",
            transactionManagerRef = "workflowCatalogTransactionManager",
            considerNestedRepositories = true,
            bootstrapMode = org.springframework.data.repository.config.BootstrapMode.LAZY)
    static class WorkflowCatalogJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.sharing.repositories",
            entityManagerFactoryRef = "sharingRegistryEntityManagerFactory",
            transactionManagerRef = "sharingRegistryTransactionManager",
            considerNestedRepositories = true,
            bootstrapMode = org.springframework.data.repository.config.BootstrapMode.LAZY)
    static class SharingRegistryJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.credential.repositories",
            entityManagerFactoryRef = "credentialStoreEntityManagerFactory",
            transactionManagerRef = "credentialStoreTransactionManager",
            considerNestedRepositories = true,
            bootstrapMode = org.springframework.data.repository.config.BootstrapMode.LAZY)
    static class CredentialStoreJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.research.service.model.repo",
            entityManagerFactoryRef = "researchCatalogEntityManagerFactory",
            transactionManagerRef = "researchCatalogTransactionManager",
            considerNestedRepositories = true,
            bootstrapMode = org.springframework.data.repository.config.BootstrapMode.LAZY)
    static class ResearchCatalogJpaRepositoriesConfig {}

    // Register expcatalog LAST so its UserRepository (marked as @Primary) overrides sharing's UserRepository
    // This must be the last @Configuration class to ensure expcatalog repository is the final bean
    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.registry.repositories.expcatalog",
            entityManagerFactoryRef = "expCatalogEntityManagerFactory",
            transactionManagerRef = "expCatalogTransactionManager",
            considerNestedRepositories = true,
            repositoryImplementationPostfix = "Impl",
            bootstrapMode = org.springframework.data.repository.config.BootstrapMode.LAZY)
    static class ExpCatalogJpaRepositoriesConfig {}
}
