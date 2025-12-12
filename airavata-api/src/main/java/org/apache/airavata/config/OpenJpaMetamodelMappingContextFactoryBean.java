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

    public OpenJpaMetamodelMappingContextFactoryBean() {
        System.out.println("DEBUG: OpenJpaMetamodelMappingContextFactoryBean CONSTRUCTOR called");
        logger.info("OpenJpaMetamodelMappingContextFactoryBean instantiated");
    }

    @org.springframework.beans.factory.annotation.Autowired
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
            logger.info("Checking EntityManagerFactory: {}", emfName);
            System.out.println("DEBUG: Checking EntityManagerFactory: " + emfName);
            try {
                // Try to get metamodel - this may fail if entities aren't enhanced
                Metamodel metamodel = emf.getMetamodel();
                if (metamodel != null) {
                    // Try to access the metamodel to ensure it's fully initialized
                    // This will throw if there are enhancement issues
                    try {
                        // Force full validation of all entities
                        for (jakarta.persistence.metamodel.ManagedType<?> type : metamodel.getManagedTypes()) {
                            // Check if class implements PersistenceCapable
                            Class<?> javaType = type.getJavaType();
                            if (javaType != null && 
                                !org.apache.openjpa.enhance.PersistenceCapable.class.isAssignableFrom(javaType) &&
                                !javaType.getName().contains("$openjpa")) {
                                
                                logger.warn("Entity {} in {} is NOT enhanced. This may cause issues.", javaType.getName(), emfName);
                                System.out.println("DEBUG: Entity " + javaType.getName() + " is NOT enhanced");
                                // We are forcing failure here to ensure we skip unenhanced factories
                                throw new IllegalStateException("Entity " + javaType.getName() + " is not enhanced");
                            }
                            
                            try {
                                // Just accessing the type is sometimes enough, but let's try to get attributes
                                type.getAttributes();
                            } catch (Exception e) {
                                throw e;
                            }
                        }
                        
                        workingEmfs.add(emf);
                        logger.info("Successfully retrieved and validated metamodel from EntityManagerFactory: {}", emfName);
                        System.out.println("DEBUG: Successfully validated " + emfName);
                    } catch (Exception e) {
                        logger.warn(
                                "Metamodel retrieved but validation failed for EntityManagerFactory {}: {}. "
                                        + "Skipping this factory.",
                                emfName,
                                e.getMessage());
                        System.out.println("DEBUG: Validation failed for " + emfName + ": " + e.getMessage());
                        logger.debug("Metamodel validation error details for {}", emfName, e);
                    }
                } else {
                    logger.warn("EntityManagerFactory {} returned null metamodel", emfName);
                }
            } catch (Throwable e) {
                // Log warning but continue - this allows the application to start
                // even if some EntityManagerFactory instances have enhancement issues
                // Catch Throwable to catch all errors including OpenJPA MetaDataException
                String errorMsg = e.getMessage();
                if (e.getCause() != null) {
                    errorMsg = e.getCause().getMessage();
                }
                
                logger.warn(
                        "Failed to retrieve/validate metamodel from EntityManagerFactory {}: {}. "
                                + "Skipping this factory to allow application startup.",
                        emfName,
                        errorMsg);
                System.out.println("DEBUG: Error processing " + emfName + ": " + errorMsg);
                logger.debug("Metamodel retrieval error details for {}", emfName, e);
            }
        }

        if (workingEmfs.isEmpty()) {
            // Instead of failing, return an empty context or a context with no EMFs
            // This allows the bean to be created, though repositories might fail later
            logger.error("No working EntityManagerFactory instances found. Creating empty JpaMetamodelMappingContext.");
        } else {
            logger.info(
                "Creating JpaMetamodelMappingContext with {} working EntityManagerFactory(ies) out of {} total",
                workingEmfs.size(),
                entityManagerFactories.size());
        }

        // Create JpaMetamodelMappingContext manually using the working metamodels
        // This avoids using JpaMetamodelMappingContextFactoryBean which might try to access things
        // or have field name mismatches.
        try {
            java.util.Set<Metamodel> metamodels = new java.util.HashSet<>();
            for (EntityManagerFactory emf : workingEmfs) {
                try {
                    Metamodel mm = emf.getMetamodel();
                    if (mm != null) {
                        metamodels.add(mm);
                    }
                } catch (Exception e) {
                    // Should not happen as we filtered already, but safe guard
                    logger.warn("Unexpected error retrieving metamodel during context creation", e);
                }
            }
            
            JpaMetamodelMappingContext context = new JpaMetamodelMappingContext(metamodels);
            logger.info("Successfully created JpaMetamodelMappingContext with {} metamodels", metamodels.size());
            return context;
        } catch (Exception e) {
            logger.error("Failed to create JpaMetamodelMappingContext manually", e);
            // Return empty context as fallback
            return new JpaMetamodelMappingContext(java.util.Collections.emptySet());
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
