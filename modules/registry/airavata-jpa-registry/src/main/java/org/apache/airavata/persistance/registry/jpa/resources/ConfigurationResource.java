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
package org.apache.airavata.persistance.registry.jpa.resources;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.model.Configuration;
import org.apache.airavata.persistance.registry.jpa.model.Configuration_PK;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;

public class ConfigurationResource extends AbstractResource {
    private String configKey;
    private String configVal;
    private Date expireDate;

    /**
     *
     */
    public ConfigurationResource() {
    }

    /**
     *
     * @param configKey
     * @param configVal
     */
    public ConfigurationResource(String configKey, String configVal) {
        this.configKey = configKey;
        this.configVal = configVal;
    }

    /**
     * Since Configuration does not depend on any other data structures at the
     * system, this method is not valid
     * @param type child resource types
     * @return UnsupportedOperationException
     */
    public Resource create(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    /**
     * Since Configuration does not depend on any other data structures at the
     * system, this method is not valid
     * @param type child resource types
     * @param name  name of the child resource
     * throws UnsupportedOperationException
     */
    public void remove(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }


    /**
     * Since Configuration does not depend on any other data structures at the
     * system, this method is not valid
     * @param type child resource types
     * @param name  name of the child resource
     * @return UnsupportedOperationException
     */
    public Resource get(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    /**
     * key should be the configuration name
     * @param keys names
     * @return list of ConfigurationResources
     */
    public List<Resource> populate(Object[] keys) {
        List<Resource> list = new ArrayList<Resource>();
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        QueryGenerator queryGenerator = new QueryGenerator(CONFIGURATION);
        queryGenerator.setParameter(ConfigurationConstants.CONFIG_KEY, keys[0]);
        Query q = queryGenerator.selectQuery(em);
        List resultList = q.getResultList();
        if (resultList.size() != 0) {
            for (Object result : resultList) {
                Configuration configuration = (Configuration) result;
                ConfigurationResource configurationResource =
                        (ConfigurationResource)Utils.getResource(ResourceType.CONFIGURATION, configuration);
                list.add(configurationResource);
            }
        }
        em.getTransaction().commit();
        em.close();
        return list;

    }

    /**
     *
     * Since Configuration does not depend on any other data structures at the
     * system, this method is not valid
     * @param type child resource types
     * @return UnsupportedOperationException
     */
    public List<Resource> get(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param expireDate expire date of the configuration
     */
    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    /**
     *  save configuration to database
     */
    public synchronized void save() {
        Lock lock = ResourceUtils.getLock();
        lock.lock();
        try {
            EntityManager em = ResourceUtils.getEntityManager();
            //whether existing
            Configuration existing = em.find(Configuration.class, new Configuration_PK(configKey, configVal));
            em.close();

            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Configuration configuration = new Configuration();
            configuration.setConfig_key(configKey);
            configuration.setConfig_val(configVal);
            configuration.setExpire_date(expireDate);
            if(existing != null){
               configuration = em.merge(existing);
            }  else {
                em.merge(configuration);
            }

            em.getTransaction().commit();
            em.close();
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Since Configuration does not depend on any other data structures at the
     * system, this method is not valid
     * @param type child resource types
     * @param name of the child resource
     * @return UnsupportedOperationException
     */
    public boolean isExists(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @return configuration value
     */
    public String getConfigVal() {
        return configVal;
    }

    /**
     *
     * @param configKey configuration key
     */
    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    /**
     *
     * @param configVal configuration value
     */
    public void setConfigVal(String configVal) {
        this.configVal = configVal;
    }
}
