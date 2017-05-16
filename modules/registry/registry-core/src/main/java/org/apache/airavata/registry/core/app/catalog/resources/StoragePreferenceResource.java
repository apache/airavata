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
import org.apache.airavata.registry.core.app.catalog.model.*;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogJPAUtils;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogQueryGenerator;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogResourceType;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoragePreferenceResource extends AppCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(StoragePreferenceResource.class);
    private String gatewayId;
    private String storageResourceId;
    private String loginUserName;
    private String fsRootLocation;
    private String resourceCSToken;

    private GatewayProfileResource gatewayProfile;

    public String getLoginUserName() {
        return loginUserName;
    }

    public void setLoginUserName(String loginUserName) {
        this.loginUserName = loginUserName;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public GatewayProfileResource getGatewayProfile() {
        return gatewayProfile;
    }

    public void setGatewayProfile(GatewayProfileResource gatewayProfile) {
        this.gatewayProfile = gatewayProfile;
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
        HashMap<String, String> ids;
        if (identifier instanceof Map) {
            ids = (HashMap) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(STORAGE_PREFERENCE);
            generator.setParameter(StoragePreferenceConstants.STORAGE_ID, ids.get(StoragePreferenceConstants.STORAGE_ID));
            generator.setParameter(StoragePreferenceConstants.GATEWAY_ID, ids.get(StoragePreferenceConstants.GATEWAY_ID));

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
        HashMap<String, String> ids;
        if (identifier instanceof Map) {
            ids = (HashMap) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(STORAGE_PREFERENCE);
            generator.setParameter(StoragePreferenceConstants.GATEWAY_ID, ids.get(StoragePreferenceConstants.GATEWAY_ID));
            generator.setParameter(StoragePreferenceConstants.STORAGE_ID, ids.get(StoragePreferenceConstants.STORAGE_ID));
            Query q = generator.selectQuery(em);
            StoragePreference preference = (StoragePreference) q.getSingleResult();
            StoragePreferenceResource preferenceResource =
                    (StoragePreferenceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.STORAGE_PREFERENCE, preference);
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(STORAGE_PREFERENCE);
            List results;
            if (fieldName.equals(StoragePreferenceConstants.STORAGE_ID)) {
                generator.setParameter(StoragePreferenceConstants.STORAGE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        StoragePreference preference = (StoragePreference) result;
                        if (preference.getStorageResourceId()!=null) {
							StoragePreferenceResource preferenceResource = (StoragePreferenceResource) AppCatalogJPAUtils
									.getResource(
											AppCatalogResourceType.STORAGE_PREFERENCE,
											preference);
							preferenceResourceList.add(preferenceResource);
						}
                    }
                }
            } else if (fieldName.equals(StoragePreferenceConstants.GATEWAY_ID)) {
                generator.setParameter(StoragePreferenceConstants.GATEWAY_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        StoragePreference preference = (StoragePreference) result;
                        if (preference.getStorageResourceId()!=null) {
	                        StoragePreferenceResource preferenceResource =
	                                (StoragePreferenceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.STORAGE_PREFERENCE, preference);
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
            StoragePreference existingPreference = em.find(StoragePreference.class, new StoragePreferencePK(gatewayId, storageResourceId));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            GatewayProfile gatewayProf = em.find(GatewayProfile.class, gatewayId);
            if (existingPreference != null) {
                existingPreference.setStorageResourceId(storageResourceId);
                existingPreference.setGatewayId(gatewayId);
                existingPreference.setGatewayProfile(gatewayProf);
                existingPreference.setLoginUserName(loginUserName);
                existingPreference.setComputeResourceCSToken(resourceCSToken);
                existingPreference.setFsRootLocation(fsRootLocation);
                em.merge(existingPreference);
            } else {
                StoragePreference resourcePreference = new StoragePreference();
                resourcePreference.setStorageResourceId(storageResourceId);
                resourcePreference.setGatewayId(gatewayId);
                resourcePreference.setGatewayProfile(gatewayProf);
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
        HashMap<String, String> ids;
        if (identifier instanceof Map) {
            ids = (HashMap) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            StoragePreference existingPreference = em.find(StoragePreference.class,
                    new StoragePreferencePK(ids.get(StoragePreferenceConstants.GATEWAY_ID),
                            ids.get(StoragePreferenceConstants.STORAGE_ID)));
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
