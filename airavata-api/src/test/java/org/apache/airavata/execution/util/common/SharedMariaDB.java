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

import org.testcontainers.containers.MariaDBContainer;

/**
 * Singleton Testcontainer for MariaDB shared across all test classes in the module.
 * Eliminates the overhead of starting multiple containers per test run.
 */
public class SharedMariaDB {

    private static final MariaDBContainer<?> INSTANCE;

    static {
        INSTANCE = new MariaDBContainer<>("mariadb:11.8")
                .withDatabaseName("airavata")
                .withUsername("airavata")
                .withPassword("airavata")
                .withCommand("--lower-case-table-names=1", "--sql-mode=")
                .withInitScript("conf/db/migration/airavata/V1__Baseline_schema.sql");
        INSTANCE.start();

        // Set system properties for JPA
        System.setProperty("airavata.jdbc.driver", INSTANCE.getDriverClassName());
        System.setProperty("airavata.jdbc.url", INSTANCE.getJdbcUrl());
        System.setProperty("airavata.jdbc.user", INSTANCE.getUsername());
        System.setProperty("airavata.jdbc.password", INSTANCE.getPassword());
        System.setProperty("airavata.jdbc.validationQuery", "SELECT 1");
    }

    public static MariaDBContainer<?> getInstance() {
        return INSTANCE;
    }
}
