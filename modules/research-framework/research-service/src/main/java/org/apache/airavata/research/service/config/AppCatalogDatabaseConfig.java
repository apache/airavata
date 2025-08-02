/*
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
package org.apache.airavata.research.service.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(
    basePackages = "org.apache.airavata.research.service.repository",
    entityManagerFactoryRef = "appCatalogEntityManagerFactory",
    transactionManagerRef = "appCatalogTransactionManager"
)
public class AppCatalogDatabaseConfig {

    @Bean
    @ConfigurationProperties("app.datasource.app-catalog")
    public DataSourceProperties appCatalogDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource appCatalogDataSource() {
        return appCatalogDataSourceProperties()
            .initializeDataSourceBuilder()
            .build();
    }
    
    @Bean
    public LocalContainerEntityManagerFactoryBean appCatalogEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(appCatalogDataSource());
        em.setPersistenceUnitName("appCatalogPU");
        
        // Scan our local entities that mirror database schema exactly
        em.setPackagesToScan("org.apache.airavata.research.service.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false); // Don't modify existing schema
        vendorAdapter.setShowSql(false);
        em.setJpaVendorAdapter(vendorAdapter);
        
        Properties props = new Properties();
        props.setProperty("hibernate.hbm2ddl.auto", "none"); // Don't modify existing schema
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
        props.setProperty("hibernate.format_sql", "true");
        props.setProperty("hibernate.show_sql", "false");
        
        // Disable validation to avoid issues with existing data
        props.setProperty("hibernate.validator.apply_to_ddl", "false");
        props.setProperty("hibernate.check_nullability", "false");
        props.setProperty("jakarta.persistence.validation.mode", "NONE");
        
        em.setJpaProperties(props);
        
        return em;
    }

    @Bean
    public PlatformTransactionManager appCatalogTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(appCatalogEntityManagerFactory().getObject());
        return transactionManager;
    }
}