/**
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
 */
package org.apache.airavata.registry.core.experiment.catalog.resources;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.SecurityUtil;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.model.Gateway;
import org.apache.airavata.registry.core.experiment.catalog.model.UserPK;
import org.apache.airavata.registry.core.experiment.catalog.model.Users;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class UserResource extends AbstractExpCatResource {
    private final static Logger logger = LoggerFactory.getLogger(UserResource.class);
    private String userName;
    private String password;
    private String gatewayId;
//    private GatewayResource gatewayResource;

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

//    public GatewayResource getGatewayResource() {
//        return gatewayResource;
//    }
//
//    public void setGatewayResource(GatewayResource gatewayResource) {
//        this.gatewayResource = gatewayResource;
//    }

    /**
     *
     */
    public UserResource() {
    }

    /**
     *
     * @param userName user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     *
     * @return user name
     */
    public String getUserName() {
        return userName;
    }


    /**
     * User is a hypothical data structure.
     * @param type child resource type
     * @return child resource
     */
    public ExperimentCatResource create(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for user resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param type child resource type
     * @param name child resource name
     */
    public void remove(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for user resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param type child resource type
     * @param name child resource name
     * @return UnsupportedOperationException
     */
    public ExperimentCatResource get(ResourceType type, Object name) throws RegistryException {
        logger.error("Unsupported resource type for user resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param type child resource type
     * @return UnsupportedOperationException
     */
    public List<ExperimentCatResource> get(ResourceType type) throws RegistryException{
        logger.error("Unsupported resource type for user resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    /**
     * save user to the database
     */
    public void save() throws RegistryException {
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            UserPK userPK = new UserPK();
            userPK.setGatewayId(gatewayId);
            userPK.setUserName(userName);
            Users existingUser = em.find(Users.class, userPK);
            Gateway gateway = em.find(Gateway.class, gatewayId);
            em.close();

            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Users user = new Users();
            user.setAiravataInternalUserId(userName+"@"+gatewayId);
            user.setUserName(userName);
            user.setGatewayId(gateway.getGatewayId());
            user.setGateway(gateway);
            if (password != null && !password.equals("")) {
                try {
                    user.setPassword(SecurityUtil.digestString(password,
                            ServerSettings.getSetting("default.registry.password.hash.method")));
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException("Error hashing default admin password. Invalid hash algorithm.", e);
                } catch (ApplicationSettingsException e) {
                    throw new RuntimeException("Error reading hash algorithm from configurations", e);
                }
            }
            if (existingUser != null) {
                if (password != null && !password.equals("")) {
                    try {
                        existingUser.setPassword(SecurityUtil.digestString(password,
                                ServerSettings.getSetting("default.registry.password.hash.method")));
                        existingUser.setGatewayId(gateway.getGatewayId());
                        existingUser.setGateway(gateway);
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException("Error hashing default admin password. Invalid hash algorithm.", e);
                    } catch (ApplicationSettingsException e) {
                        throw new RuntimeException("Error reading hash algorithm from configurations", e);
                    }
                }
                user = em.merge(existingUser);
            } else {
                em.persist(user);
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    /**
     *
     * @param type child resource type
     * @param name child resource name
     * @return UnsupportedOperationException
     */
    public boolean isExists(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for user resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @return  password
     */
    public String getPassword() {
        return password;
    }

    /**
     *
     * @param password  password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
