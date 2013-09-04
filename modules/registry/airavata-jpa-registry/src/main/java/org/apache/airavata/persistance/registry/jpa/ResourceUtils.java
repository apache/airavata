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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.airavata.persistance.registry.jpa.model.Configuration;
import org.apache.airavata.persistance.registry.jpa.model.Configuration_PK;
import org.apache.airavata.persistance.registry.jpa.model.Gateway;
import org.apache.airavata.persistance.registry.jpa.model.Gateway_Worker;
import org.apache.airavata.persistance.registry.jpa.model.Gateway_Worker_PK;
import org.apache.airavata.persistance.registry.jpa.model.Users;
import org.apache.airavata.persistance.registry.jpa.resources.AbstractResource;
import org.apache.airavata.persistance.registry.jpa.resources.ConfigurationResource;
import org.apache.airavata.persistance.registry.jpa.resources.GatewayResource;
import org.apache.airavata.persistance.registry.jpa.resources.UserResource;
import org.apache.airavata.persistance.registry.jpa.resources.Utils;
import org.apache.airavata.persistance.registry.jpa.resources.WorkerResource;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;
import org.apache.airavata.registry.api.exception.AiravataRegistryUninitializedException;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceUtils {
    private final static Logger logger = LoggerFactory.getLogger(ResourceUtils.class);
    private static final String PERSISTENCE_UNIT_NAME = "airavata_data";
    protected static EntityManagerFactory factory;

    private static Lock lock = new ReentrantLock();

    public static void reset(){
    	factory=null;
    }
    
    public static EntityManager getEntityManager(){
        if (factory == null) {
            String connectionProperties = "DriverClassName=" + Utils.getJDBCDriver() + "," + "Url=" + Utils.getJDBCURL() + "," +
                    "Username=" + Utils.getJDBCUser() + "," + "Password=" + Utils.getJDBCPassword() + ",validationQuery=" +
            Utils.getValidationQuery() + "," + Utils.getJPAConnectionProperties();
            System.out.println(connectionProperties);
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("openjpa.ConnectionDriverName", "org.apache.commons.dbcp.BasicDataSource");
            properties.put("openjpa.ConnectionProperties", connectionProperties);
            properties.put("openjpa.DynamicEnhancementAgent", "true");
            properties.put("openjpa.RuntimeUnenhancedClasses", "unsupported");
            properties.put("openjpa.Log", "SQL=ERROR");
//            properties.put("openjpa.Log","DefaultLevel=WARN, Runtime=INFO, Tool=INFO, SQL=TRACE");
            properties.put("openjpa.ReadLockLevel", "none");
            properties.put("openjpa.WriteLockLevel", "none");
            properties.put("openjpa.LockTimeout", "30000");
            properties.put("openjpa.LockManager", "none");
            properties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
            properties.put("openjpa.ConnectionFactoryProperties", "PrettyPrint=true, PrettyPrintLineLength=72, PrintParameters=true, MaxActive=10, MaxIdle=5, MinIdle=2, MaxWait=60000");
			properties.put("openjpa.jdbc.QuerySQLCache", "false");
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
        }
		return factory.createEntityManager();
    }

    /**
     * @param gatewayName
     * @return
     */
    public static Resource createGateway(String gatewayName){
        if (!isGatewayExist(gatewayName)) {
            GatewayResource gatewayResource = new GatewayResource();
            gatewayResource.setGatewayName(gatewayName);
            return gatewayResource;
        }
        return null;

    }

    public static Resource getGateway(String gatewayName){
        if (isGatewayExist(gatewayName)) {
            EntityManager em = getEntityManager();
            Gateway gateway = em.find(Gateway.class, gatewayName);
            GatewayResource gatewayResource = (GatewayResource)Utils.getResource(ResourceType.GATEWAY, gateway);
            em.close();
            return gatewayResource;
        }
        return null;

    }

    public static Resource getWorker(String gatewayName, String userName){
        EntityManager em = getEntityManager();
        Gateway_Worker gatewayWorker = em.find(Gateway_Worker.class, new Gateway_Worker_PK(gatewayName, userName));
        WorkerResource workerResource = (WorkerResource) Utils.getResource(ResourceType.GATEWAY_WORKER, gatewayWorker);
        em.close();
        return workerResource;
    }


    /**
     * @param gatewayName
     * @return
     */
    public static boolean isGatewayExist(String gatewayName){

        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        QueryGenerator generator = new QueryGenerator(AbstractResource.GATEWAY);
        generator.setParameter(AbstractResource.GatewayConstants.GATEWAY_NAME, gatewayName);
        Query q = generator.selectQuery(em);
        int size = q.getResultList().size();
        em.getTransaction().commit();
        em.close();
		return size>0;
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
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
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
            logger.error(e.getMessage(), e);
            return false;
        }

    }

    /**
     * @param configKey
     * @return
     */
    public static List<ConfigurationResource> getConfigurations(String configKey){
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
    public static ConfigurationResource getConfiguration(String configKey){
        List<ConfigurationResource> configurations = getConfigurations(configKey);
        return (configurations != null && configurations.size() > 0) ? configurations.get(0) : null;
    }

    /**
     * @param configKey
     * @return
     */
    public static boolean isConfigurationExist(String configKey){
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
    public static void removeConfiguration(String configkey, String configValue){
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
    public static void removeConfiguration(String configkey){
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
            //Currently categoryID is hardcoded value
            EntityManager em = ResourceUtils.getEntityManager();
            Configuration existing = em.find(Configuration.class, new Configuration_PK(configKey, configVal, AbstractResource.ConfigurationConstants.CATEGORY_ID_DEFAULT_VALUE));
            em.close();
            return existing!= null;
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            throw new EntityNotFoundException();
        }
    }

    public static Lock getLock() {
        return lock;
    }

}
