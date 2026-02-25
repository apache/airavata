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
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
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
@EnableJpaAuditing
@EnableJpaRepositories(
        basePackages = {
            "org.apache.airavata.research.experiment.repository",
            "org.apache.airavata.compute.resource.repository",
            "org.apache.airavata.accounting.repository",
            "org.apache.airavata.execution.repository",
            "org.apache.airavata.research.application.repository",
            "org.apache.airavata.gateway.repository",
            "org.apache.airavata.iam.repository",
            "org.apache.airavata.workflow.repository",
            "org.apache.airavata.status.repository",
            "org.apache.airavata.credential.repository",
            "org.apache.airavata.research.artifact.repository",
            "org.apache.airavata.research.project.repository",
            "org.apache.airavata.research.session.repository",
            "org.apache.airavata.agent.db.repository",
            "org.apache.airavata.research.repository"
        })
public class JpaConfiguration {

    private static final String[] ENTITY_PACKAGES = {
        "org.apache.airavata.research.experiment.entity",
        "org.apache.airavata.compute.resource.entity",
        "org.apache.airavata.accounting.entity",
        "org.apache.airavata.execution.entity",
        "org.apache.airavata.research.application.entity",
        "org.apache.airavata.gateway.entity",
        "org.apache.airavata.iam.entity",
        "org.apache.airavata.workflow.entity",
        "org.apache.airavata.status.entity",
        "org.apache.airavata.credential.entity",
        "org.apache.airavata.research.artifact.entity",
        "org.apache.airavata.research.project.entity",
        "org.apache.airavata.research.session.entity",
        "org.apache.airavata.agent.db.entity",
        "org.apache.airavata.research.entity"
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
