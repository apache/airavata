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

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Test to validate persistence.xml configuration.
 * Verifies that all entity classes are correctly referenced.
 */
public class ValidatePersistenceXml {

    private static final String[] PERSISTENCE_UNITS = {
        "profile_service",
        "app_catalog",
        "experiment_catalog",
        "replica_catalog",
        "workflow_catalog",
        "sharing_registry",
        "credential_store"
    };

    @Test
    public void testAllPersistenceUnits() {
        int errors = 0;
        StringBuilder errorMessages = new StringBuilder();

        for (String puName : PERSISTENCE_UNITS) {
            try {
                // Use in-memory H2 database to avoid connection issues
                Map<String, String> properties = new HashMap<>();
                properties.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
                properties.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
                properties.put("jakarta.persistence.jdbc.user", "sa");
                properties.put("jakarta.persistence.jdbc.password", "");
                properties.put("hibernate.hbm2ddl.auto", "create");

                EntityManagerFactory emf = Persistence.createEntityManagerFactory(puName, properties);
                if (emf != null) {
                    emf.close();
                } else {
                    errorMessages.append("Persistence unit ").append(puName).append(": EntityManagerFactory is null\n");
                    errors++;
                }
            } catch (Exception e) {
                errorMessages
                        .append("Persistence unit ")
                        .append(puName)
                        .append(": ")
                        .append(e.getMessage());
                if (e.getCause() != null) {
                    errorMessages
                            .append(" (Caused by: ")
                            .append(e.getCause().getMessage())
                            .append(")");
                }
                errorMessages.append("\n");
                errors++;
            }
        }

        if (errors > 0) {
            fail("Validation failed with " + errors + " error(s):\n" + errorMessages.toString());
        }
    }
}
