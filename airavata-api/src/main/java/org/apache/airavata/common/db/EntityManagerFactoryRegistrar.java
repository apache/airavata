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
package org.apache.airavata.common.db;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Bridges the Spring-managed {@link EntityManagerFactory} into the static
 * {@link EntityManagerFactoryHolder} so that non-Spring repository classes
 * can obtain an {@link jakarta.persistence.EntityManager}.
 */
@Component
public class EntityManagerFactoryRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(EntityManagerFactoryRegistrar.class);

    private final EntityManagerFactory emf;

    public EntityManagerFactoryRegistrar(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @PostConstruct
    public void register() {
        EntityManagerFactoryHolder.setFactory(emf);
        logger.info("Registered Spring-managed EntityManagerFactory in EntityManagerFactoryHolder");
    }
}
