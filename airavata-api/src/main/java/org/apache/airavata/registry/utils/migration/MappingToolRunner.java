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
package org.apache.airavata.registry.utils.migration;

import org.apache.airavata.common.utils.DBInitConfig;
import org.apache.airavata.common.utils.JPAUtils;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for generating database schema DDL scripts using Hibernate.
 * 
 * Note: Consider migrating to a proper database migration tool like Flyway or Liquibase
 * for production schema management.
 */
public class MappingToolRunner {

    private static Logger logger = LoggerFactory.getLogger(MappingToolRunner.class);

    public static final String ACTION_ADD = "add";
    public static final String ACTION_BUILD = "build";

    public static void run(DBInitConfig dbInitConfig, String outputFile, String persistenceUnitName) {
        run(dbInitConfig, outputFile, persistenceUnitName, ACTION_ADD);
    }

    /**
     * Generate database schema DDL script using Hibernate SchemaExport.
     * 
     * @param dbInitConfig Database configuration
     * @param outputFile Output file path for the DDL script
     * @param persistenceUnitName Name of the persistence unit
     * @param schemaAction "add" to add missing schema elements, "build" to create entire schema
     */
    public static void run(
            DBInitConfig dbInitConfig, String outputFile, String persistenceUnitName, String schemaAction) {

        EntityManagerFactory emf = null;
        try {
            // Create EntityManagerFactory using JPAUtils
            emf = JPAUtils.getEntityManagerFactory(
                    persistenceUnitName,
                    dbInitConfig.getDriver(),
                    dbInitConfig.getUrl(),
                    dbInitConfig.getUser(),
                    dbInitConfig.getPassword(),
                    dbInitConfig.getValidationQuery());
            
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
        } finally {
            if (emf != null) {
                emf.close();
            }
        }
    }
}
