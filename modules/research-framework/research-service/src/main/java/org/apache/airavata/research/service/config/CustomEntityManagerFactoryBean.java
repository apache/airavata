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

import org.apache.airavata.registry.core.entities.appcatalog.ComputeResourceEntity;
import org.apache.airavata.registry.core.entities.appcatalog.StorageResourceEntity;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.Properties;

/**
 * Custom EntityManagerFactory that programmatically registers ONLY the specific
 * entities we need, completely bypassing package scanning to avoid ParserInputEntity
 * and other problematic entities.
 */
public class CustomEntityManagerFactoryBean extends LocalContainerEntityManagerFactoryBean {
    
    private final DataSource dataSource;
    private final Properties hibernateProperties;
    
    public CustomEntityManagerFactoryBean(DataSource dataSource, Properties hibernateProperties) {
        this.dataSource = dataSource;
        this.hibernateProperties = hibernateProperties;
        setDataSource(dataSource);
        setPersistenceUnitName("appCatalogPU");
        // DO NOT set packages to scan - we will register entities manually
    }
    
    @Override
    protected EntityManagerFactory createNativeEntityManagerFactory() {
        // Create Hibernate service registry with our properties
        StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
        
        // Add datasource configuration
        hibernateProperties.put("hibernate.connection.datasource", dataSource);
        registryBuilder.applySettings(hibernateProperties);
        
        StandardServiceRegistry registry = registryBuilder.build();
        
        try {
            // Create metadata sources and add ONLY our specific entities
            MetadataSources metadataSources = new MetadataSources(registry);
            
            // CRITICAL: Add ONLY the entities we need - no package scanning
            metadataSources.addAnnotatedClass(ComputeResourceEntity.class);
            metadataSources.addAnnotatedClass(StorageResourceEntity.class);
            
            // Build metadata and create session factory
            Metadata metadata = metadataSources.buildMetadata();
            
            // Return the EntityManagerFactory from the session factory
            return metadata.buildSessionFactory().unwrap(EntityManagerFactory.class);
            
        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw new RuntimeException("Failed to create EntityManagerFactory", e);
        }
    }
}