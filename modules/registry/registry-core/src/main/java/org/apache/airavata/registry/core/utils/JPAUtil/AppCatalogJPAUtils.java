/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.registry.core.utils.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.airavata.common.utils.JDBCConfig;
import org.apache.airavata.common.utils.JPAUtils;
import org.apache.airavata.registry.core.utils.AppCatalogJDBCConfig;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.meta.MappingTool;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.persistence.ArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppCatalogJPAUtils {

    // TODO: we can rename this back to appcatalog_data once we completely replace
    // the other appcatalog_data persistence context in airavata-registry-core
    private static final String PERSISTENCE_UNIT_NAME = "appcatalog_data_new";
    private static final JDBCConfig JDBC_CONFIG = new AppCatalogJDBCConfig();
    private static final EntityManagerFactory factory = JPAUtils.getEntityManagerFactory(PERSISTENCE_UNIT_NAME,
            JDBC_CONFIG);
    private static final Logger logger = LoggerFactory.getLogger(AppCatalogJPAUtils.class);

    public static EntityManager getEntityManager() {
        try {
            return factory.createEntityManager();
        } catch (ArgumentException e) {
            // TODO: refactor this
            JDBCConfiguration jdbcConfiguration = JPAUtils.getJDBCConfiguration(JDBC_CONFIG);
            Options options = new Options();
            options.put("sqlFile", "migration.sql");
            // If you want to generate the entire schema instead of just what is
            // needed to bring the database up to date, use schemaAction=build
            // options.put("schemaAction", "build");
            options.put("foreignKeys", "true");
            options.put("indexes", "true");
            options.put("primaryKeys", "true");
            try {
                MappingTool.run(jdbcConfiguration, new String[] {}, options, null);
            } catch (Exception mappingToolEx) {
                logger.error("Failed to run MappingTool", mappingToolEx);
                throw new RuntimeException("Failed to get EntityManager, then failed to run MappingTool to generate migration script", e);
            }
            throw new RuntimeException("Failed to get EntityManager, but successfully executed MappingTool to generate migration script (to file named migration.sql) in case the error was caused by the database schema being out of date with the mappings", e);
        }
    }
}
