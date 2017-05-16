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
import org.apache.airavata.registry.core.app.catalog.model.UserResourceProfile;
import org.apache.airavata.registry.core.app.catalog.model.UserResourceProfilePK;
import org.apache.airavata.registry.core.app.catalog.model.UserStoragePreference;
import org.apache.airavata.registry.core.app.catalog.model.UserStoragePreferencePK;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogJPAUtils;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogQueryGenerator;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogResourceType;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.CompositeIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class UserStoragePreferenceResource extends AppCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(UserStoragePreferenceResource.class);
    private String gatewayID;
    private String userId;
    private String storageResourceId;
    private String loginUserName;
    private String fsRootLocation;
    private String resourceCSToken;


    private UserResourceProfileResource userResourceProfileResource;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLoginUserName() {
        return loginUserName;
    }

    public void setLoginUserName(String loginUserName) {
        this.loginUserName = loginUserName;
    }

    public String getGatewayId() {
        return gatewayID;
    }

    public void setGatewayId(String gatewayID) {
        this.gatewayID = gatewayID;
    }

    public UserResourceProfileResource getUserResourceProfileResource() {
        return userResourceProfileResource;
    }

    public void setUserResourceProfileResource(UserResourceProfileResource userResourceProfileResource) {
        this.userResourceProfileResource = userResourceProfileResource;
    }

    public String getResourceCSToken() {
        return resourceCSToken;
    }

    public void setResourceCSToken(String resourceCSToken) {
        this.resourceCSToken = resourceCSToken;
    }

    public String getFsRootLocation() {
        return fsRootLocation;
    }

    public void setFsRootLocation(String fsRootLocation) {
        this.fsRootLocation = fsRootLocation;
    }

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    @Override
    public void remove(Object identifier) throws AppCatalogException {
        CompositeIdentifier ids;
        if (identifier instanceof CompositeIdentifier) {
            ids = (CompositeIdentifier) identifier;
        } else {
            logger.error("Identifier should be a instance of CompositeIdentifier class");
            throw new AppCatalogException("Identifier should be a instance of CompositeIdentifier class");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(USER_STORAGE_PREFERENCE);
            generator.setParameter(UserStoragePreferenceConstants.STORAGE_ID, ids.getTopLevelIdentifier().toString());
            generator.setParameter(UserStoragePreferenceConstants.USER_ID, ids.getSecondLevelIdentifier().toString());
            generator.setParameter(UserStoragePreferenceConstants.GATEWAY_ID, ids.getThirdLevelIdentifier().toString());

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

    @Override
    public AppCatalogResource get(Object identifier) throws AppCatalogException {
        CompositeIdentifier ids;
        if (identifier instanceof CompositeIdentifier) {
            ids = (CompositeIdentifier) identifier;
        } else {
            logger.error("Identifier should be a instance of CompositeIdentifier class");
            throw new AppCatalogException("Identifier should be a instance of CompositeIdentifier class");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(USER_STORAGE_PREFERENCE);
            generator.setParameter(UserStoragePreferenceConstants.STORAGE_ID, ids.getTopLevelIdentifier().toString());
            generator.setParameter(UserStoragePreferenceConstants.USER_ID, ids.getSecondLevelIdentifier().toString());
            generator.setParameter(UserStoragePreferenceConstants.GATEWAY_ID, ids.getThirdLevelIdentifier().toString());
            Query q = generator.selectQuery(em);
            UserStoragePreference preference = (UserStoragePreference) q.getSingleResult();
            UserStoragePreferenceResource preferenceResource =
                    (UserStoragePreferenceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.USER_STORAGE_PREFERENCE, preference);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return preferenceResource;
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

    @Override
    public List<AppCatalogResource> get(String fieldName, Object value) throws AppCatalogException {
        List<AppCatalogResource> preferenceResourceList = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(USER_STORAGE_PREFERENCE);
            List results;
            if (fieldName.equals(UserStoragePreferenceConstants.STORAGE_ID)) {
                generator.setParameter(UserStoragePreferenceConstants.STORAGE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        UserStoragePreference preference = (UserStoragePreference) result;
                        if (preference.getStorageResourceId()!=null) {
							UserStoragePreferenceResource preferenceResource = (UserStoragePreferenceResource) AppCatalogJPAUtils
									.getResource(
											AppCatalogResourceType.USER_STORAGE_PREFERENCE,
											preference);
							preferenceResourceList.add(preferenceResource);
						}
                    }
                }
            } else if (fieldName.equals(UserStoragePreferenceConstants.USER_ID)) {
                CompositeIdentifier ids;
                if (value instanceof CompositeIdentifier) {
                    ids = (CompositeIdentifier) value;
                } else {
                    logger.error("Identifier should be a instance of CompositeIdentifier class");
                    throw new AppCatalogException("Identifier should be a instance of CompositeIdentifier class");
                }
                generator.setParameter(UserStoragePreferenceConstants.USER_ID, ids.getTopLevelIdentifier().toString());
                generator.setParameter(UserStoragePreferenceConstants.GATEWAY_ID, ids.getSecondLevelIdentifier().toString());
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        UserStoragePreference preference = (UserStoragePreference) result;
                        if (preference.getStorageResourceId()!=null) {
	                        UserStoragePreferenceResource preferenceResource =
	                                (UserStoragePreferenceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.USER_STORAGE_PREFERENCE, preference);
	                        preferenceResourceList.add(preferenceResource);
                        }
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
                logger.error("Unsupported field name for data storage preference Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for data storage preference Resource.");
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
        return preferenceResourceList;
    }

    @Override
    public List<AppCatalogResource> getAll() throws AppCatalogException {
        return null;
    }

    @Override
    public List<String> getAllIds() throws AppCatalogException {
        return null;
    }

    @Override
    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
        logger.error("Unsupported for objects with a composite identifier");
        throw new AppCatalogException("Unsupported for objects with a composite identifier");
    }

    @Override
    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            UserStoragePreference existingPreference = em.find(UserStoragePreference.class, new UserStoragePreferencePK(userId, gatewayID, storageResourceId));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            UserResourceProfile userResourceProfile = em.find(UserResourceProfile.class, new UserResourceProfilePK(userId,gatewayID));
            if (existingPreference != null) {
                existingPreference.setStorageResourceId(storageResourceId);
                existingPreference.setUserId(userId);
                existingPreference.setGatewayID(gatewayID);
                existingPreference.setUserResourceProfile(userResourceProfile);
                existingPreference.setLoginUserName(loginUserName);
                existingPreference.setComputeResourceCSToken(resourceCSToken);
                existingPreference.setFsRootLocation(fsRootLocation);
                em.merge(existingPreference);
            } else {
                UserStoragePreference resourcePreference = new UserStoragePreference();
                resourcePreference.setStorageResourceId(storageResourceId);
                resourcePreference.setGatewayID(gatewayID);
                resourcePreference.setUserId(userId);
                resourcePreference.setUserResourceProfile(userResourceProfile);
                resourcePreference.setLoginUserName(loginUserName);
                resourcePreference.setComputeResourceCSToken(resourceCSToken);
                resourcePreference.setFsRootLocation(fsRootLocation);
                em.persist(resourcePreference);
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

    @Override
    public boolean isExists(Object identifier) throws AppCatalogException {
        CompositeIdentifier ids;
        if (identifier instanceof CompositeIdentifier) {
            ids = (CompositeIdentifier) identifier;
        } else {
            logger.error("Identifier should be a instance of CompositeIdentifier class");
            throw new AppCatalogException("Identifier should be a instance of CompositeIdentifier class");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            String storageResourceId = ids.getTopLevelIdentifier().toString();
            String userId = ids.getSecondLevelIdentifier().toString();
            String gatewayID = ids.getThirdLevelIdentifier().toString();
            UserStoragePreference existingPreference = em.find(UserStoragePreference.class,
                    new UserStoragePreferencePK(userId, gatewayID, storageResourceId));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return existingPreference != null;
        }catch (Exception e) {
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
