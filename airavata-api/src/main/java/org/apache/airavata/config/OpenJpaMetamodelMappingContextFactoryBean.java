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

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.Metamodel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.util.Assert;

/**
 * Custom factory bean for creating JpaMetamodelMappingContext that handles
 * OpenJPA enhancement errors gracefully. This allows the application to start
 * even if some entities are not enhanced, as long as RuntimeUnenhancedClasses
 * is set to "supported".
 */
public class OpenJpaMetamodelMappingContextFactoryBean
        implements FactoryBean<JpaMetamodelMappingContext>, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(OpenJpaMetamodelMappingContextFactoryBean.class);

    private Collection<EntityManagerFactory> entityManagerFactories;
    private JpaMetamodelMappingContext mappingContext;

    /**
     * Set the EntityManagerFactory instances to use.
     */
    public void setEntityManagerFactories(Collection<EntityManagerFactory> entityManagerFactories) {
        this.entityManagerFactories = entityManagerFactories;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(entityManagerFactories, "EntityManagerFactories must not be null");
        logger.info(
                "Initializing OpenJpaMetamodelMappingContextFactoryBean with {} EntityManagerFactory(ies)",
                entityManagerFactories.size());
        try {
            this.mappingContext = createInstance();
            logger.info("Successfully created JpaMetamodelMappingContext");
        } catch (Exception e) {
            logger.error("Failed to create JpaMetamodelMappingContext", e);
            throw e;
        }
    }

    protected JpaMetamodelMappingContext createInstance() {
        logger.info("Creating JpaMetamodelMappingContext instance...");
        // Collect EntityManagerFactory instances that can successfully provide metamodels
        List<EntityManagerFactory> workingEmfs = new ArrayList<>();

        for (EntityManagerFactory emf : entityManagerFactories) {
            String emfName = getEmfName(emf);
            logger.debug("Attempting to retrieve metamodel from EntityManagerFactory: {}", emfName);
            try {
                Metamodel metamodel = emf.getMetamodel();
                if (metamodel != null) {
                    workingEmfs.add(emf);
                    logger.info("Successfully retrieved metamodel from EntityManagerFactory: {}", emfName);
                } else {
                    logger.warn("EntityManagerFactory {} returned null metamodel", emfName);
                }
            } catch (Exception e) {
                // Log warning but continue - this allows the application to start
                // even if some EntityManagerFactory instances have enhancement issues
                logger.warn(
                        "Failed to retrieve metamodel from EntityManagerFactory {}: {}. "
                                + "This may be due to OpenJPA enhancement issues. Continuing with other factories.",
                        emfName,
                        e.getMessage());
                logger.debug("Metamodel retrieval error details for {}", emfName, e);
            }
        }

        if (workingEmfs.isEmpty()) {
            logger.error("No EntityManagerFactory instances could provide metamodels. "
                    + "This will cause Spring Data JPA to fail.");
            throw new IllegalStateException("Cannot create JpaMetamodelMappingContext: "
                    + "no EntityManagerFactory instances could provide metamodels");
        }

        logger.info(
                "Creating JpaMetamodelMappingContext with {} working EntityManagerFactory(ies) out of {} total",
                workingEmfs.size(),
                entityManagerFactories.size());

        // Use Spring Data JPA's standard factory to create the context
        // but only with the EntityManagerFactory instances that work
        org.springframework.data.jpa.repository.config.JpaMetamodelMappingContextFactoryBean standardFactory =
                new org.springframework.data.jpa.repository.config.JpaMetamodelMappingContextFactoryBean();

        // Use reflection to set EntityManagerFactories since the method signature may vary
        try {
            java.lang.reflect.Method setMethod =
                    standardFactory.getClass().getMethod("setEntityManagerFactories", Collection.class);
            setMethod.invoke(standardFactory, workingEmfs);
        } catch (NoSuchMethodException e) {
            // Try with List
            try {
                java.lang.reflect.Method setMethod =
                        standardFactory.getClass().getMethod("setEntityManagerFactories", List.class);
                setMethod.invoke(standardFactory, workingEmfs);
            } catch (Exception e2) {
                // Try setting via field
                try {
                    java.lang.reflect.Field field =
                            standardFactory.getClass().getDeclaredField("entityManagerFactories");
                    field.setAccessible(true);
                    field.set(standardFactory, workingEmfs);
                } catch (Exception e3) {
                    logger.error("Cannot set EntityManagerFactories on JpaMetamodelMappingContextFactoryBean", e3);
                    throw new IllegalStateException("Cannot configure JpaMetamodelMappingContextFactoryBean", e3);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to set EntityManagerFactories on standard factory", e);
            throw new IllegalStateException("Cannot configure JpaMetamodelMappingContextFactoryBean", e);
        }

        try {
            standardFactory.afterPropertiesSet();
            return standardFactory.getObject();
        } catch (Exception e) {
            logger.error("Failed to create JpaMetamodelMappingContext using standard factory", e);
            throw new IllegalStateException("Cannot create JpaMetamodelMappingContext", e);
        }
    }

    private String getEmfName(EntityManagerFactory emf) {
        try {
            if (emf instanceof org.apache.openjpa.persistence.EntityManagerFactoryImpl) {
                org.apache.openjpa.persistence.EntityManagerFactoryImpl openJpaEmf =
                        (org.apache.openjpa.persistence.EntityManagerFactoryImpl) emf;
                // Try to get the persistence unit name
                try {
                    java.lang.reflect.Field field =
                            org.apache.openjpa.persistence.EntityManagerFactoryImpl.class.getDeclaredField("name");
                    field.setAccessible(true);
                    Object name = field.get(openJpaEmf);
                    if (name != null) {
                        return name.toString();
                    }
                } catch (Exception e) {
                    // Ignore reflection errors
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return emf.getClass().getSimpleName();
    }

    @Override
    public JpaMetamodelMappingContext getObject() {
        return mappingContext;
    }

    @Override
    public Class<?> getObjectType() {
        return JpaMetamodelMappingContext.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
