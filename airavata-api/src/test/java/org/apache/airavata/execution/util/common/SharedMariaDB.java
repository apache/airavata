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

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.common.db.EntityManagerFactoryHolder;
import org.testcontainers.containers.MariaDBContainer;

/**
 * Singleton Testcontainer for MariaDB shared across all test classes in the module.
 * Eliminates the overhead of starting multiple containers per test run.
 *
 * <p>Uses lazy initialization so that merely loading this class (e.g. during
 * JUnit parallel class scanning) does not trigger a Docker connection attempt.
 */
public class SharedMariaDB {

    private static volatile MariaDBContainer<?> INSTANCE;
    private static volatile boolean initialized = false;

    private static synchronized void ensureStarted() {
        if (initialized) {
            return;
        }
        initialized = true;

        INSTANCE = new MariaDBContainer<>("mariadb:11.8")
                .withDatabaseName("airavata")
                .withUsername("airavata")
                .withPassword("airavata")
                .withCommand("--lower-case-table-names=1", "--sql-mode=")
                .withInitScript("conf/db/migration/airavata/V1__Baseline_schema.sql");
        INSTANCE.start();

        // Set system properties (still used by ServerSettings in some tests)
        System.setProperty("airavata.jdbc.driver", INSTANCE.getDriverClassName());
        System.setProperty("airavata.jdbc.url", INSTANCE.getJdbcUrl());
        System.setProperty("airavata.jdbc.user", INSTANCE.getUsername());
        System.setProperty("airavata.jdbc.password", INSTANCE.getPassword());
        System.setProperty("airavata.jdbc.validationQuery", "SELECT 1");

        // Create the single EntityManagerFactory and register it in the holder
        initEntityManagerFactory();
    }

    private static void initEntityManagerFactory() {
        try {
            Map<String, String> props = new HashMap<>();
            props.put("jakarta.persistence.jdbc.url", INSTANCE.getJdbcUrl());
            props.put("jakarta.persistence.jdbc.user", INSTANCE.getUsername());
            props.put("jakarta.persistence.jdbc.password", INSTANCE.getPassword());
            props.put("jakarta.persistence.jdbc.driver", INSTANCE.getDriverClassName());
            props.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
            props.put("hibernate.hbm2ddl.auto", "update");
            props.put("hibernate.enable_lazy_load_no_trans", "true");
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("airavata", props);
            EntityManagerFactoryHolder.setFactory(emf);
        } catch (Exception e) {
            System.err.println("FATAL: Failed to create EntityManagerFactory: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create EntityManagerFactory", e);
        }
    }

    public static MariaDBContainer<?> getInstance() {
        ensureStarted();
        return INSTANCE;
    }
}
