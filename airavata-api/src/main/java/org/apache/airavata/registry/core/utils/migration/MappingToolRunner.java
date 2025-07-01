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
package org.apache.airavata.registry.core.utils.migration;

import org.apache.airavata.common.utils.JDBCConfig;
import org.apache.airavata.common.utils.JPAUtils;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.jdbc.meta.MappingTool;
import org.apache.openjpa.lib.util.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingToolRunner {

    private static Logger logger = LoggerFactory.getLogger(MappingToolRunner.class);

    public static void run(JDBCConfig jdbcConfig, String outputFile, String persistenceUnitName) {
        run(jdbcConfig, outputFile, persistenceUnitName, MappingTool.ACTION_ADD);
    }

    // schemaAction is one of MappingTool's supported actions:
    // http://openjpa.apache.org/builds/2.4.3/apache-openjpa/docs/ref_guide_mapping.html#ref_guide_mapping_mappingtool
    public static void run(JDBCConfig jdbcConfig, String outputFile, String persistenceUnitName, String schemaAction) {

        JDBCConfiguration jdbcConfiguration = new JDBCConfigurationImpl();
        jdbcConfiguration.fromProperties(JPAUtils.createConnectionProperties(jdbcConfig));
        jdbcConfiguration.setConnectionDriverName("org.apache.commons.dbcp2.BasicDataSource");

        Options options = new Options();
        options.put("sqlFile", outputFile);
        // schemaAction "add" brings the schema up to date by adding missing schema elements
        // schemaAction "build" creates the entire schema as if the database is empty
        options.put("schemaAction", schemaAction);
        options.put("foreignKeys", "true");
        options.put("indexes", "true");
        options.put("primaryKeys", "true");
        // Specify persistence-unit name using it's anchor in the persistence.xml file
        // http://openjpa.apache.org/builds/2.4.3/apache-openjpa/docs/ref_guide_conf_devtools.html
        options.put("properties", "persistence.xml#" + persistenceUnitName);
        try {
            MappingTool.run(jdbcConfiguration, new String[] {}, options, null);
        } catch (Exception mappingToolEx) {
            logger.error("Failed to run MappingTool", mappingToolEx);
            throw new RuntimeException("Failed to run MappingTool to generate migration script", mappingToolEx);
        }
    }
}
