/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.registry.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

public class JPAUtils {
    private final static Logger logger = LoggerFactory.getLogger(JPAUtils.class);
    private static final String PERSISTENCE_UNIT_NAME = "airavata_catalog";
    @PersistenceUnit(unitName = "airavata_catalog")
    protected static EntityManagerFactory factory;
    @PersistenceContext(unitName = "airavata_catalog")
    private static EntityManager entityManager;

    public static EntityManager getEntityManager(){
        if (factory == null) {
            //FIXME
            String connectionProperties = "DriverClassName=com.mysql.jdbc.Driver," +
                    "Url=jdbc:mysql://localhost:3306/airavata_catalog," +
                    "Username=root," +
                    "Password=root";
            logger.info(connectionProperties);
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("openjpa.ConnectionDriverName", "org.apache.commons.dbcp.BasicDataSource");
            properties.put("openjpa.ConnectionProperties", connectionProperties);
            properties.put("openjpa.DynamicEnhancementAgent", "true");
            properties.put("openjpa.RuntimeUnenhancedClasses", "warn");
            properties.put("openjpa.RemoteCommitProvider", "sjvm");
            properties.put("openjpa.Log", "DefaultLevel=INFO, Runtime=INFO, Tool=INFO, SQL=INFO");
            properties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
            properties.put("openjpa.jdbc.QuerySQLCache", "false");
            properties.put("openjpa.ConnectionFactoryProperties", "PrettyPrint=true, PrettyPrintLineLength=72," +
                    " PrintParameters=true, MaxActive=10, MaxIdle=5, MinIdle=2, MaxWait=31536000,  autoReconnect=true");
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
        }

        entityManager = factory.createEntityManager();
        return entityManager;
    }

    public static <R> R execute(Committer<EntityManager, R> committer){
        EntityManager entityManager = JPAUtils.getEntityManager();
        try {
            entityManager.getTransaction().begin();
            R r = committer.commit(entityManager);
            entityManager.getTransaction().commit();
            return  r;
        }finally {
            if (entityManager != null && entityManager.isOpen()) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                entityManager.close();
            }
        }
    }


}