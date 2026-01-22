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

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import javax.sql.DataSource;
import org.apache.airavata.config.conditional.ConditionalOnApiService;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA configuration for Airavata using a single unified 'airavata' database.
 *
 * <p>All entities (profiles, registry, sharing, credentials) are stored in
 * one database, eliminating the need for multiple data sources.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {
            "org.apache.airavata.profile.repositories",
            "org.apache.airavata.registry.repositories",
            "org.apache.airavata.sharing.repositories",
            "org.apache.airavata.credential.repositories"
        })
@ConditionalOnApiService
public class JpaConfig {

    private static final String[] ENTITY_PACKAGES = {
        "org.apache.airavata.profile.entities",
        "org.apache.airavata.registry.entities",
        "org.apache.airavata.sharing.entities",
        "org.apache.airavata.credential.entities"
    };

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Profile("!test")
    public DataSource dataSource(DataSourceProperties properties) {
        return properties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, Environment env) {
        var em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan(ENTITY_PACKAGES);
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        // Set JPA properties from environment
        var properties = new HashMap<String, Object>();

        // Core Hibernate properties
        var ddlAuto = env.getProperty("spring.jpa.hibernate.ddl-auto", "none");
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);

        var dialect = env.getProperty("spring.jpa.properties.hibernate.dialect");
        if (dialect != null) {
            properties.put("hibernate.dialect", dialect);
        }

        boolean showSql = env.getProperty("spring.jpa.show-sql", Boolean.class, false);
        properties.put("hibernate.show_sql", showSql);

        boolean formatSql = env.getProperty("spring.jpa.properties.hibernate.format_sql", Boolean.class, false);
        properties.put("hibernate.format_sql", formatSql);

        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
