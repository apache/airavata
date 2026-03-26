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

import org.junit.jupiter.api.Tag;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests. Uses Testcontainers MariaDB.
 *
 * <p>Tag: "integration" — can be filtered in CI with {@code -Dgroups=integration} or excluded
 * with {@code -DexcludedGroups=integration}.
 *
 * <p>Usage: {@code mvn test -pl airavata-api -Dgroups=integration -DexcludedGroups=""}
 */
@Tag("integration")
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    protected static final MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11")
            .withDatabaseName("airavata_test")
            .withUsername("airavata")
            .withPassword("airavata");

    protected static String getJdbcUrl() {
        return mariadb.getJdbcUrl();
    }

    protected static String getUsername() {
        return mariadb.getUsername();
    }

    protected static String getPassword() {
        return mariadb.getPassword();
    }

    protected static String getJdbcDriver() {
        return "org.mariadb.jdbc.Driver";
    }
}
