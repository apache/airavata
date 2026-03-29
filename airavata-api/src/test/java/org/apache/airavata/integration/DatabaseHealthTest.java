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
package org.apache.airavata.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verifies JDBC connectivity to the Airavata database and the presence of
 * core tables required for experiment and resource management.
 */
@Tag("runtime")
class DatabaseHealthTest {

    private static final String HOST = System.getProperty("airavata.db.host", "localhost");
    private static final int PORT = Integer.parseInt(System.getProperty("airavata.db.port", "13306"));
    private static final String DATABASE = System.getProperty("airavata.db.name", "airavata");
    private static final String USER = System.getProperty("airavata.db.user", "airavata");
    private static final String PASSWORD = System.getProperty("airavata.db.password", "123456");

    private static final String JDBC_URL = "jdbc:mariadb://" + HOST + ":" + PORT + "/" + DATABASE;

    static Stream<String> requiredTables() {
        return Stream.of(
                "EXPERIMENT",
                "APPLICATION_DEPLOYMENT",
                "COMPUTE_RESOURCE",
                "GATEWAY",
                "USER_PROFILE",
                "SHARING_ENTITY",
                "CREDENTIAL");
    }

    @ParameterizedTest(name = "table {0} exists")
    @MethodSource("requiredTables")
    void requiredTableShouldExist(String tableName) throws Exception {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD)) {
            assertTrue(connection.isValid(5), "Database connection is not valid");

            DatabaseMetaData meta = connection.getMetaData();
            List<String> found = new ArrayList<>();
            try (ResultSet rs = meta.getTables(DATABASE, null, tableName, new String[] {"TABLE"})) {
                while (rs.next()) {
                    found.add(rs.getString("TABLE_NAME"));
                }
            }
            assertTrue(
                    found.stream().anyMatch(t -> t.equalsIgnoreCase(tableName)),
                    "Expected table '" + tableName + "' was not found in database '" + DATABASE + "'");
        }
    }
}
