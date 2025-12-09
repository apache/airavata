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
package org.apache.airavata.registry.repositories.common;

import org.apache.airavata.config.JpaConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for repository tests using Spring Boot testing framework.
 * Migrated from Derby-based setup to Spring Boot with H2 in-memory databases.
 * All tests use @Transactional for automatic rollback.
 */
@SpringBootTest(classes = {JpaConfig.class})
@TestPropertySource(locations = "classpath:airavata.properties")
@Transactional
public class TestBase {

    public enum Database {
        APP_CATALOG,
        EXP_CATALOG,
        REPLICA_CATALOG,
        WORKFLOW_CATALOG
    }

    private Database[] databases;

    public TestBase(Database... databases) {
        if (databases == null) {
            throw new IllegalArgumentException("Databases can not be null");
        }
        this.databases = databases;
    }

    // Note: Database setup is now handled by Spring Boot configuration
    // H2 in-memory databases are configured via airavata.properties
    // No manual setup/teardown needed - Spring Boot handles it automatically
}
