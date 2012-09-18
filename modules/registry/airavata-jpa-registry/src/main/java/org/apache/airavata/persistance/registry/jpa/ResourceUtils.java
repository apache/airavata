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
package org.apache.airavata.persistance.registry.jpa;

import org.apache.airavata.persistance.registry.jpa.model.*;
import org.apache.airavata.persistance.registry.jpa.resources.*;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ResourceUtils {
    private static final String PERSISTENCE_UNIT_NAME = "airavata_data";
    protected static EntityManagerFactory factory;

    private static Lock lock = new ReentrantLock();

    public static EntityManager getEntityManager(){
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("openjpa.ConnectionURL", Utils.getJDBCURL());
        properties.put("openjpa.ConnectionDriverName", Utils.getJDBCDriver());
        properties.put("openjpa.ConnectionUserName",Utils.getJDBCUser());
        properties.put("openjpa.ConnectionPassword",Utils.getJDBCPassword());
        properties.put("openjpa.DynamicEnhancementAgent","true");
        properties.put("openjpa.RuntimeUnenhancedClasses","supported");
        properties.put("openjpa.Log","SQL=TRACE");
        properties.put("openjpa.ReadLockLevel", "none");
        properties.put("openjpa.WriteLockLevel", "none");
        properties.put("openjpa.LockTimeout", "30000");
        properties.put("openjpa.LockManager", "none");
//        properties.put("openjpa.jdbc.Schema", "AIRAVATA");
        properties.put("openjpa.ConnectionFactoryProperties","PrettyPrint=true, PrettyPrintLineLength=72, PrintParameters=true, MaxActive=10, MaxIdle=5, MinIdle=2, MaxWait=60000");

        if (factory == null) {
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
        }

        return factory.createEntityManager();
    }

    /**
     * @param gatewayName
     * @return
     */
    public static Resource createGateway(String gatewayName) {
        if (!isGatewayExist(gatewayName)) {
            GatewayResource gatewayResource = new GatewayResource();
            gatewayResource.setGatewayName(gatewayName);
            return gatewayResource;
        }
        return null;

    }

    /**
     * @param gatewayName
     * @return
     */
    public static boolean isGatewayExist(String gatewayName) {

        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        QueryGenerator generator = new QueryGenerator(AbstractResource.GATEWAY);
        generator.setParameter(AbstractResource.GatewayConstants.GATEWAY_NAME, gatewayName);
        Query q = generator.selectQuery(em);
        Gateway gateway = (Gateway) q.getSingleResult();
        em.getTransaction().commit();
        em.close();
        return gateway != null;
    }

    /**
     * @param gatewayName
     * @return
     */
    public static boolean removeGateway(String gatewayName) {
        try {
            EntityManager em = getEntityManager();
            em.getTransaction().begin();
            QueryGenerator generator = new QueryGenerator(AbstractResource.GATEWAY);
            generator.setParameter(AbstractResource.GatewayConstants.GATEWAY_NAME, gatewayName);
            Query q = generator.deleteQuery(em);
            q.executeUpdate();
            em.getTransaction().commit();
            em.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }

    /**
     * @param gatewayResource
     * @param userResource
     */
    public static void addGatewayWorker(GatewayResource gatewayResource, UserResource userResource) {
        try {
            EntityManager em = getEntityManager();
            em.getTransaction().begin();
            Gateway gateway = new Gateway();
            gateway.setGateway_name(gatewayResource.getGatewayName());
            Users user = new Users();
            user.setUser_name(userResource.getUserName());
            Gateway_Worker gatewayWorker = new Gateway_Worker();
            gatewayWorker.setGateway(gateway);
            gatewayWorker.setUser(user);
            em.persist(gatewayWorker);
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param gatewayResource
     * @param userResource
     * @return
     */
    public static boolean removeGatewayWorker(GatewayResource gatewayResource, UserResource userResource) {
        try {
            EntityManager em = getEntityManager();
            em.getTransaction().begin();
            QueryGenerator generator = new QueryGenerator(AbstractResource.GATEWAY_WORKER);
            generator.setParameter(AbstractResource.GatewayWorkerConstants.GATEWAY_NAME,
                    gatewayResource.getGatewayName());
            generator.setParameter(AbstractResource.UserConstants.USERNAME, userResource.getUserName());
            Query q = generator.deleteQuery(em);
            q.executeUpdate();
            em.getTransaction().commit();
            em.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * @param configKey
     * @return
     */
    public static List<ConfigurationResource> getConfigurations(String configKey) {
        List<ConfigurationResource> list = new ArrayList<ConfigurationResource>();
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        QueryGenerator generator = new QueryGenerator(AbstractResource.CONFIGURATION);
        generator.setParameter(AbstractResource.ConfigurationConstants.CONFIG_KEY, configKey);
        Query q = generator.selectQuery(em);
        List<?> resultList = q.getResultList();
        if (resultList.size() != 0) {
            for (Object result : resultList) {
                ConfigurationResource configurationResource = createConfigurationResourceObject(result);
                list.add(configurationResource);
            }
        }
        em.getTransaction().commit();
        em.close();
        return list;
    }

    /**
     * @param configKey
     * @return
     */
    public static ConfigurationResource getConfiguration(String configKey) {
        List<ConfigurationResource> configurations = getConfigurations(configKey);
        return (configurations != null && configurations.size() > 0) ? configurations.get(0) : null;
    }

    /**
     * @param configKey
     * @return
     */
    public static boolean isConfigurationExist(String configKey) {
        List<ConfigurationResource> configurations = getConfigurations(configKey);
        return (configurations != null && configurations.size() > 0);
    }

    /**
     * @param configKey
     * @return
     */
    public static ConfigurationResource createConfiguration(String configKey) {
        ConfigurationResource config = new ConfigurationResource();
        config.setConfigKey(configKey);
        return config;
    }

    /**
     * @param result
     * @return
     */
    private static ConfigurationResource createConfigurationResourceObject(
            Object result) {
        Configuration configuration = (Configuration) result;
        ConfigurationResource configurationResource = new ConfigurationResource(configuration.getConfig_key(), configuration.getConfig_val());
        configurationResource.setExpireDate(configuration.getExpire_date());
        return configurationResource;
    }

    /**
     * @param configkey
     * @param configValue
     */
    public static void removeConfiguration(String configkey, String configValue) {
        QueryGenerator queryGenerator = new QueryGenerator(AbstractResource.CONFIGURATION);
        queryGenerator.setParameter(AbstractResource.ConfigurationConstants.CONFIG_KEY, configkey);
        queryGenerator.setParameter(AbstractResource.ConfigurationConstants.CONFIG_VAL, configValue);
        if(isConfigurationExists(configkey, configValue)){
            EntityManager em = getEntityManager();
            em.getTransaction().begin();
            Query q = queryGenerator.deleteQuery(em);
            q.executeUpdate();
            em.getTransaction().commit();
            em.close();
        }
    }

    /**
     * @param configkey
     */
    public static void removeConfiguration(String configkey) {
        QueryGenerator queryGenerator = new QueryGenerator(AbstractResource.CONFIGURATION);
        queryGenerator.setParameter(AbstractResource.ConfigurationConstants.CONFIG_KEY, configkey);
        if(isConfigurationExist(configkey)){
            EntityManager em = getEntityManager();
            em.getTransaction().begin();
            Query q = queryGenerator.deleteQuery(em);
            q.executeUpdate();
            em.getTransaction().commit();
            em.close();
        }
    }

    public static boolean isConfigurationExists(String configKey, String configVal){
        try{
            EntityManager em = ResourceUtils.getEntityManager();
            Configuration existing = em.find(Configuration.class, new Configuration_PK(configKey, configVal));
            em.close();
            return existing!= null;
        } catch (Exception e){
            throw new EntityNotFoundException();
        }
    }

    public static Lock getLock() {
        return lock;
    }

}
