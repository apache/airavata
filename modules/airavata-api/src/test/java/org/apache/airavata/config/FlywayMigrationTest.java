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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test to verify database schema is properly created.
 * Note: In test mode, Hibernate creates schema via hbm2ddl, not Flyway.
 */
@SpringBootTest(
        classes = {JpaConfig.class, TestcontainersConfig.class},
        properties = {"spring.main.allow-bean-definition-overriding=true", "airavata.flyway.enabled=false"})
@ActiveProfiles("test")
public class FlywayMigrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testDataSourceIsAvailable() {
        assertNotNull(dataSource, "DataSource should be available");
    }

    @Test
    public void testDatabaseSchemaIsCreated() throws Exception {
        try (var conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5), "Connection should be valid");

            // Verify schema exists by checking for tables
            var metaData = conn.getMetaData();
            var tables = metaData.getTables(null, null, null, new String[] {"TABLE"});

            int tableCount = 0;
            while (tables.next()) {
                tableCount++;
            }
            tables.close();

            assertTrue(tableCount > 0, "Database should have tables created");
        }
    }
}
