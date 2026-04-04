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
package org.apache.airavata.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Static holder for the single application-wide {@link EntityManagerFactory}.
 * Set once at startup by {@code EntityManagerFactoryRegistrar} (Spring) or
 * manually in tests.
 */
public class EntityManagerFactoryHolder {
    private static volatile EntityManagerFactory factory;
    private static final ThreadLocal<EntityManager> testEntityManager = new ThreadLocal<>();

    public static void setFactory(EntityManagerFactory emf) {
        factory = emf;
    }

    public static EntityManagerFactory getFactory() {
        if (factory == null) {
            throw new IllegalStateException("EntityManagerFactory not initialized");
        }
        return factory;
    }

    public static EntityManager createEntityManager() {
        return getFactory().createEntityManager();
    }

    public static void setTestEntityManager(EntityManager em) {
        testEntityManager.set(em);
    }

    public static EntityManager getTestEntityManager() {
        return testEntityManager.get();
    }

    public static void clearTestEntityManager() {
        testEntityManager.remove();
    }
}
