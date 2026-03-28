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
package org.apache.airavata.execution.util.common;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("integration")
@Testcontainers
public abstract class TestBase {

    private static final Logger logger = LoggerFactory.getLogger(TestBase.class);

    public enum Database {
        APP_CATALOG,
        EXP_CATALOG,
        REPLICA_CATALOG,
        WORKFLOW_CATALOG
    }

    @SuppressWarnings("resource")
    @Container
    protected static final MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.8")
            .withDatabaseName("airavata")
            .withUsername("airavata")
            .withPassword("airavata")
            .withCommand("--lower-case-table-names=1", "--sql-mode=")
            .withInitScript("conf/db/migration/airavata/V1__Baseline_schema.sql");

    private Database[] databases;

    public TestBase(Database... databases) {
        if (databases == null) {
            throw new IllegalArgumentException("Databases can not be null");
        }
        this.databases = databases;
    }

    @BeforeAll
    static void configureJdbc() {
        System.setProperty("airavata.jdbc.driver", mariadb.getDriverClassName());
        System.setProperty("airavata.jdbc.url", mariadb.getJdbcUrl());
        System.setProperty("airavata.jdbc.user", mariadb.getUsername());
        System.setProperty("airavata.jdbc.password", mariadb.getPassword());
        System.setProperty("airavata.jdbc.validationQuery", "SELECT 1");
    }

    @BeforeEach
    public void setUp() throws Exception {
        truncateAllTables();
    }

    /**
     * Truncate all user tables so each test starts with clean data.
     */
    private void truncateAllTables() throws SQLException {
        try (Connection conn =
                DriverManager.getConnection(mariadb.getJdbcUrl(), mariadb.getUsername(), mariadb.getPassword())) {
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            DatabaseMetaData meta = conn.getMetaData();
            List<String> tables = new ArrayList<>();
            try (ResultSet rs = meta.getTables("airavata", null, "%", new String[] {"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }

            for (String table : tables) {
                stmt.execute("TRUNCATE TABLE `" + table + "`");
            }

            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            conn.commit();
        }
    }
}
