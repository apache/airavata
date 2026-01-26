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

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test to verify Testcontainers setup and database connectivity.
 */
@SpringBootTest(
        classes = {JpaConfig.class, TestcontainersConfig.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "airavata.flyway.enabled=false",
        })
@ActiveProfiles("test")
public class TestcontainersSetupTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testDataSourceIsCreated() {
        assertNotNull(dataSource, "DataSource should be created");
    }

    @Test
    public void testDatabaseConnectionIsValid() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5), "Database connection should be valid");
        }
    }

    @Test
    public void testTablesAreCreated() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, null, new String[] {"TABLE"});

            int tableCount = 0;
            while (tables.next()) {
                tableCount++;
            }
            tables.close();

            assertTrue(tableCount > 0, "Database should have tables created by Hibernate");
        }
    }

    @Test
    public void testKeyTablesExist() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            // Check for key tables from different entity packages
            assertTrue(
                    tableExists(conn, "COMPUTE_RESOURCE") || tableExists(conn, "compute_resource"),
                    "COMPUTE_RESOURCE table should exist");
            assertTrue(
                    tableExists(conn, "EXPERIMENT") || tableExists(conn, "experiment"),
                    "EXPERIMENT table should exist");
            assertTrue(
                    tableExists(conn, "STATUS") || tableExists(conn, "status"),
                    "STATUS table should exist");
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws Exception {
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet tables = metaData.getTables(null, null, tableName, null);
        boolean exists = tables.next();
        tables.close();
        return exists;
    }
}
