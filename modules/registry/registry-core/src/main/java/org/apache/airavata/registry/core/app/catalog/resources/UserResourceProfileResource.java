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
package org.apache.airavata.registry.core.app.catalog.resources;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.core.app.catalog.model.UserResourceProfile;
import org.apache.airavata.registry.core.app.catalog.model.UserResourceProfilePK;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogJPAUtils;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogQueryGenerator;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogResourceType;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.CompositeIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserResourceProfileResource extends AppCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(UserResourceProfileResource.class);

    private String userId;
    private String gatewayID;
    private Timestamp createdTime;
    private Timestamp updatedTime;
    private String credentialStoreToken;
    private String identityServerTenant;
    private String identityServerPwdCredToken;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGatewayID() {
        return gatewayID;
    }

    public void setGatewayID(String gatewayID) {
        this.gatewayID = gatewayID;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public Timestamp getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Timestamp updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getCredentialStoreToken() {
        return credentialStoreToken;
    }

    public void setCredentialStoreToken(String credentialStoreToken) {
        this.credentialStoreToken = credentialStoreToken;
    }

    public String getIdentityServerTenant() {
        return identityServerTenant;
    }

    public void setIdentityServerTenant(String identityServerTenant) {
        this.identityServerTenant = identityServerTenant;
    }

    public String getIdentityServerPwdCredToken() {
        return identityServerPwdCredToken;
    }

    public void setIdentityServerPwdCredToken(String identityServerPwdCredToken) {
        this.identityServerPwdCredToken = identityServerPwdCredToken;
    }

    public void remove(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        CompositeIdentifier ids;
        if (identifier instanceof CompositeIdentifier) {
            ids = (CompositeIdentifier) identifier;
        } else {
            logger.error("Identifier should be a instance of CompositeIdentifier class");
            throw new AppCatalogException("Identifier should be a instance of CompositeIdentifier class");
        }
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(USER_RESOURCE_PROFILE);
            generator.setParameter(UserResourceProfileConstants.GATEWAY_ID, ids.getSecondLevelIdentifier().toString());
            generator.setParameter(UserResourceProfileConstants.USER_ID, ids.getTopLevelIdentifier().toString());
            Query q = generator.deleteQuery(em);
            q.executeUpdate();
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public AppCatalogResource get(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        CompositeIdentifier ids;
        if (identifier instanceof CompositeIdentifier) {
            ids = (CompositeIdentifier) identifier;
        } else {
            logger.error("Identifier should be a instance of CompositeIdentifier class");
            throw new AppCatalogException("Identifier should be a instance of CompositeIdentifier class");
        }
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(USER_RESOURCE_PROFILE);
            generator.setParameter(UserResourceProfileConstants.USER_ID, ids.getTopLevelIdentifier().toString());
            generator.setParameter(UserResourceProfileConstants.GATEWAY_ID, ids.getSecondLevelIdentifier().toString());
            Query q = generator.selectQuery(em);
            if(q.getResultList().size() != 0){
                UserResourceProfile userResourceProfile = (UserResourceProfile) q.getSingleResult();
                UserResourceProfileResource userResourceProfileResource =
                        (UserResourceProfileResource) AppCatalogJPAUtils.getResource(
                                AppCatalogResourceType.USER_RESOURCE_PROFILE, userResourceProfile);
                em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
                userResourceProfileResource.setUserId(ids.getTopLevelIdentifier().toString());
                return userResourceProfileResource;
            }else{
                return null;
            }
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public List<AppCatalogResource> get(String fieldName, Object value) throws AppCatalogException {
        List<AppCatalogResource> userResourceProfileResources = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        CompositeIdentifier ids;
        if (value instanceof CompositeIdentifier) {
            ids = (CompositeIdentifier) value;
        } else {
            logger.error("Identifier should be a instance of CompositeIdentifier class");
            throw new AppCatalogException("Identifier should be a instance of CompositeIdentifier class");
        }
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(USER_RESOURCE_PROFILE);
            List results;
            if (fieldName.equals(UserResourceProfileConstants.USER_ID)) {
                generator.setParameter(UserStoragePreferenceConstants.USER_ID, ids.getTopLevelIdentifier().toString());
                generator.setParameter(UserStoragePreferenceConstants.GATEWAY_ID, ids.getSecondLevelIdentifier().toString());
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        UserResourceProfile userResourceProfile = (UserResourceProfile) result;
                        UserResourceProfileResource userResourceProfileResource =
                                (UserResourceProfileResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.USER_RESOURCE_PROFILE, userResourceProfile);
                        userResourceProfileResources.add(userResourceProfileResource);
                    }
                }
            } else {
                em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
                logger.error("Unsupported field name for User Resource Profile resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for User Resource Profile resource.");
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return userResourceProfileResources;
    }

    @Override
    public List<AppCatalogResource> getAll() throws AppCatalogException {
        List<AppCatalogResource> resourceList = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(USER_RESOURCE_PROFILE);
            Query q = generator.selectQuery(em);
            List results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    UserResourceProfile userResourceProfile = (UserResourceProfile) result;
                    UserResourceProfileResource userResourceProfileResource =
                            (UserResourceProfileResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.USER_RESOURCE_PROFILE, userResourceProfile);
                    resourceList.add(userResourceProfileResource);
                }
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return resourceList;
    }

    @Override
    public List<String> getAllIds() throws AppCatalogException {
        return null;
    }

    public List<String> getIds(String userId, Object value) throws AppCatalogException {
        List<String> userResourceProfileResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        HashMap<String, String> ids;
        if (value instanceof Map) {
            ids = (HashMap) value;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(USER_RESOURCE_PROFILE);
            List results;
            if (userId.equals(UserResourceProfileConstants.USER_ID)) {
                generator.setParameter(UserResourceProfileConstants.USER_ID, ids.get(UserResourceProfileConstants.USER_ID));
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        UserResourceProfile userResourceProfile = (UserResourceProfile) result;
                        userResourceProfileResourceIDs.add(userResourceProfile.getUserID());
                    }
                }
            } else {
                em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
                logger.error("Unsupported field name for User Resource Profile resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for User Resource Profile resource.");
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return userResourceProfileResourceIDs;
    }

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            UserResourceProfile existingUserResourceProfile = em.find(UserResourceProfile.class, new UserResourceProfilePK(userId,gatewayID));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingUserResourceProfile != null) {
                existingUserResourceProfile.setUpdateTime(AiravataUtils.getCurrentTimestamp());
                if (credentialStoreToken != null){
                    existingUserResourceProfile.setCredentialStoreToken(credentialStoreToken);
                }
                if (identityServerTenant != null){
                    existingUserResourceProfile.setIdentityServerTenant(identityServerTenant);
                }
                if (identityServerPwdCredToken != null){
                    existingUserResourceProfile.setIdentityServerPwdCredToken(identityServerPwdCredToken);
                }
                em.merge(existingUserResourceProfile);
            } else {
                UserResourceProfile userResourceProfile = new UserResourceProfile();
                userResourceProfile.setGatewayID(gatewayID);
                userResourceProfile.setUserId(userId);
                userResourceProfile.setCreationTime(AiravataUtils.getCurrentTimestamp());
                if (credentialStoreToken != null){
                    userResourceProfile.setCredentialStoreToken(credentialStoreToken);
                }
                if (identityServerTenant != null){
                    userResourceProfile.setIdentityServerTenant(identityServerTenant);
                }
                if (identityServerPwdCredToken != null){
                    userResourceProfile.setIdentityServerPwdCredToken(identityServerPwdCredToken);
                }
                em.persist(userResourceProfile);
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public boolean isExists(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        CompositeIdentifier ids;
        if (identifier instanceof CompositeIdentifier) {
            ids = (CompositeIdentifier) identifier;
        } else {
            logger.error("Identifier should be a instance of CompositeIdentifier class");
            throw new AppCatalogException("Identifier should be a instance of CompositeIdentifier class");
        }
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(USER_RESOURCE_PROFILE);
            List results;
            generator.setParameter(UserResourceProfileConstants.USER_ID, ids.getTopLevelIdentifier().toString());
            generator.setParameter(UserResourceProfileConstants.GATEWAY_ID, ids.getSecondLevelIdentifier().toString());
            q = generator.selectQuery(em);
            results = q.getResultList();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return results != null;
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }
}
