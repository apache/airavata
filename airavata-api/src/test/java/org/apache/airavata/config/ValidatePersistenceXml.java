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

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/**
 * Standalone utility to validate persistence.xml configuration.
 * This can be run to check if all entity classes are correctly referenced.
 */
public class ValidatePersistenceXml {
    
    private static final String[] PERSISTENCE_UNITS = {
        "profile_service",
        "appcatalog_data_new",
        "experiment_data_new",
        "replicacatalog_data_new",
        "workflowcatalog_data_new",
        "airavata-sharing-registry",
        "credential_store"
    };

    public static void main(String[] args) {
        System.out.println("Validating persistence.xml configuration...");
        int errors = 0;
        
        for (String puName : PERSISTENCE_UNITS) {
            System.out.print("Checking persistence unit: " + puName + " ... ");
            try {
                // Use in-memory H2 database to avoid connection issues
                Map<String, String> properties = new HashMap<>();
                properties.put("openjpa.jdbc.Driver", "org.h2.Driver");
                properties.put("openjpa.jdbc.URL", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
                properties.put("openjpa.jdbc.User", "sa");
                properties.put("openjpa.jdbc.Password", "");
                properties.put("openjpa.jdbc.DBDictionary", "h2");
                properties.put("openjpa.RuntimeUnenhancedClasses", "unsupported");
                
                EntityManagerFactory emf = Persistence.createEntityManagerFactory(puName, properties);
                if (emf != null) {
                    System.out.println("OK");
                    emf.close();
                } else {
                    System.out.println("FAILED - EntityManagerFactory is null");
                    errors++;
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                if (e.getCause() != null) {
                    System.out.println("  Caused by: " + e.getCause().getMessage());
                }
                errors++;
            }
        }
        
        if (errors == 0) {
            System.out.println("\nAll persistence units validated successfully!");
            System.exit(0);
        } else {
            System.out.println("\nValidation failed with " + errors + " error(s)");
            System.exit(1);
        }
    }
}

