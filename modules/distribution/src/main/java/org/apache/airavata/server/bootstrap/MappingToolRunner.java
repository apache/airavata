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
package org.apache.airavata.server.bootstrap;

import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.config.JpaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Spring component for generating database schema DDL scripts using Hibernate.
 *
 * Note: Consider migrating to a proper database migration tool like Flyway or Liquibase
 * for production schema management.
 */
@Component
public class MappingToolRunner {

    private static final Logger logger = LoggerFactory.getLogger(MappingToolRunner.class);

    public static final String ACTION_ADD = "add";
    public static final String ACTION_BUILD = "build";

    // Map persistence unit names to EntityManagerFactory beans
    private final Map<String, EntityManagerFactory> emfMap;

    public MappingToolRunner(
            @Qualifier("profileServiceEntityManagerFactory") EntityManagerFactory profileServiceEmf,
            @Qualifier("appCatalogEntityManagerFactory") EntityManagerFactory appCatalogEmf,
            @Qualifier("expCatalogEntityManagerFactory") EntityManagerFactory expCatalogEmf,
            @Qualifier("replicaCatalogEntityManagerFactory") EntityManagerFactory replicaCatalogEmf,
            @Qualifier("workflowCatalogEntityManagerFactory") EntityManagerFactory workflowCatalogEmf,
            @Qualifier("sharingRegistryEntityManagerFactory") EntityManagerFactory sharingRegistryEmf,
            @Qualifier("credentialStoreEntityManagerFactory") EntityManagerFactory credentialStoreEmf) {
        this.emfMap = new HashMap<>();
        this.emfMap.put(JpaConfig.PROFILE_SERVICE_PU, profileServiceEmf);
        this.emfMap.put(JpaConfig.APPCATALOG_PU, appCatalogEmf);
        this.emfMap.put(JpaConfig.EXPCATALOG_PU, expCatalogEmf);
        this.emfMap.put(JpaConfig.REPLICACATALOG_PU, replicaCatalogEmf);
        this.emfMap.put(JpaConfig.WORKFLOWCATALOG_PU, workflowCatalogEmf);
        this.emfMap.put(JpaConfig.SHARING_REGISTRY_PU, sharingRegistryEmf);
        this.emfMap.put(JpaConfig.CREDENTIAL_STORE_PU, credentialStoreEmf);
    }

    /**
     * Generate database schema DDL script using Hibernate SchemaExport.
     *
     * @param outputFile Output file path for the DDL script
     * @param persistenceUnitName Name of the persistence unit
     */
    public void run(String outputFile, String persistenceUnitName) {
        run(outputFile, persistenceUnitName, ACTION_ADD);
    }

    /**
     * Generate database schema DDL script using Hibernate SchemaExport.
     *
     * @param outputFile Output file path for the DDL script
     * @param persistenceUnitName Name of the persistence unit
     * @param schemaAction "add" to add missing schema elements, "build" to create entire schema
     */
    public void run(String outputFile, String persistenceUnitName, String schemaAction) {
        EntityManagerFactory emf = getEntityManagerFactory(persistenceUnitName);
        if (emf == null) {
            throw new IllegalArgumentException(
                    "Unknown persistence unit name: " + persistenceUnitName + ". Valid names: " + emfMap.keySet());
        }

        try {

            // Note: Hibernate 6 schema export API has changed significantly from Hibernate 5.
            // For now, this method logs a warning. To implement full schema export:
            // 1. Use Hibernate's SchemaManagementTool with proper SourceDescriptor and TargetDescriptor
            // 2. Or use a database migration tool like Flyway/Liquibase
            // 3. Or use hibernate.hbm2ddl.auto=update in development
            // 4. Or use Hibernate's SchemaExport programmatically with proper Hibernate 6 API

            logger.warn("Schema export via MappingToolRunner is not fully implemented for Hibernate 6.");
            logger.warn("Consider using hibernate.hbm2ddl.auto=update or a migration tool like Flyway/Liquibase.");
            logger.warn("Requested output file: {}", outputFile);

            // TODO: Implement proper Hibernate 6 schema export API
            // The API requires:
            // - SourceDescriptor for source metadata
            // - TargetDescriptor for output target
            // - Proper ExecutionOptions
            // See: org.hibernate.tool.schema.spi.SchemaManagementTool

        } catch (Exception ex) {
            logger.error("Failed to generate schema DDL script", ex);
            throw new RuntimeException("Failed to generate schema DDL script using Hibernate", ex);
        }
        // Note: We don't close the EntityManagerFactory here as it's managed by Spring
    }

    /**
     * Get EntityManagerFactory by persistence unit name.
     *
     * @param persistenceUnitName Name of the persistence unit
     * @return EntityManagerFactory for the persistence unit, or null if not found
     */
    private EntityManagerFactory getEntityManagerFactory(String persistenceUnitName) {
        return emfMap.get(persistenceUnitName);
    }
}
