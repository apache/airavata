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
import org.apache.airavata.registry.cpi.CompositeIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UserComputeHostPreferenceResource extends AppCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(UserComputeHostPreferenceResource.class);
    private String gatewayID;
    private String resourceId;
    private String userId;
    private String batchQueue;
    private String scratchLocation;
    private String projectNumber;
    private String loginUserName;
    private String resourceCSToken;
    private String qualityOfService;
    private String reservation;
    private Timestamp reservationStartTime;
    private Timestamp reservationEndTime;
    private boolean validated = false;

    private UserResourceProfileResource userResourceProfileResource;
    private ComputeResourceResource computeHostResource;

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

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getBatchQueue() {
        return batchQueue;
    }

    public void setBatchQueue(String batchQueue) {
        this.batchQueue = batchQueue;
    }

    public String getScratchLocation() {
        return scratchLocation;
    }

    public void setScratchLocation(String scratchLocation) {
        this.scratchLocation = scratchLocation;
    }

    public String getProjectNumber() {
        return projectNumber;
    }

    public void setProjectNumber(String projectNumber) {
        this.projectNumber = projectNumber;
    }

    public ComputeResourceResource getComputeHostResource() {
        return computeHostResource;
    }

    public void setComputeHostResource(ComputeResourceResource computeHostResource) {
        this.computeHostResource = computeHostResource;
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


    public String getQualityOfService() {
        return qualityOfService;
    }

    public void setQualityOfService(String qualityOfService) {
        this.qualityOfService = qualityOfService;
    }

    public String getReservation() {
        return reservation;
    }

    public void setReservation(String reservation) {
        this.reservation = reservation;
    }

    public Timestamp getReservationStartTime() {
        return reservationStartTime;
    }

    public void setReservationStartTime(Timestamp reservationStartTime) {
        this.reservationStartTime = reservationStartTime;
    }

    public Timestamp getReservationEndTime() {
        return reservationEndTime;
    }

    public void setReservationEndTime(Timestamp reservationEndTime) {
        this.reservationEndTime = reservationEndTime;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(USER_COMPUTE_RESOURCE_PREFERENCE);
            generator.setParameter(UserComputeResourcePreferenceConstants.RESOURCE_ID, ids.getTopLevelIdentifier().toString());
            generator.setParameter(UserComputeResourcePreferenceConstants.USER_ID, ids.getSecondLevelIdentifier().toString());
            generator.setParameter(UserComputeResourcePreferenceConstants.GATEWAY_ID, ids.getThirdLevelIdentifier().toString());

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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(USER_COMPUTE_RESOURCE_PREFERENCE);
            generator.setParameter(UserComputeResourcePreferenceConstants.RESOURCE_ID, ids.getTopLevelIdentifier().toString());
            generator.setParameter(UserComputeResourcePreferenceConstants.USER_ID, ids.getSecondLevelIdentifier().toString());
            generator.setParameter(UserComputeResourcePreferenceConstants.GATEWAY_ID, ids.getThirdLevelIdentifier().toString());
            Query q = generator.selectQuery(em);
            UserComputeResourcePreference preference = (UserComputeResourcePreference) q.getSingleResult();
            UserComputeHostPreferenceResource preferenceResource =
                    (UserComputeHostPreferenceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.USER_COMPUTE_RESOURCE_PREFERENCE, preference);
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(USER_COMPUTE_RESOURCE_PREFERENCE);
            List results;
            if (fieldName.equals(UserComputeResourcePreferenceConstants.RESOURCE_ID)) {
                generator.setParameter(UserComputeResourcePreferenceConstants.RESOURCE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        UserComputeResourcePreference preference = (UserComputeResourcePreference) result;
                        if (preference.getComputeHostResource()!=null) {
							UserComputeHostPreferenceResource preferenceResource = (UserComputeHostPreferenceResource) AppCatalogJPAUtils
									.getResource(
											AppCatalogResourceType.USER_COMPUTE_RESOURCE_PREFERENCE,
											preference);
							preferenceResourceList.add(preferenceResource);
						}
                    }
                }
            } else if (fieldName.equals(UserComputeResourcePreferenceConstants.USER_ID)) {
                CompositeIdentifier ids;
                if (value instanceof CompositeIdentifier) {
                    ids = (CompositeIdentifier) value;
                } else {
                    logger.error("Identifier should be a instance of CompositeIdentifier class");
                    throw new AppCatalogException("Identifier should be a instance of CompositeIdentifier class");
                }
                generator.setParameter(UserComputeResourcePreferenceConstants.USER_ID, ids.getTopLevelIdentifier().toString());
                generator.setParameter(UserComputeResourcePreferenceConstants.GATEWAY_ID, ids.getSecondLevelIdentifier().toString());
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        UserComputeResourcePreference preference = (UserComputeResourcePreference) result;
                        if (preference.getComputeHostResource()!=null) {
	                        UserComputeHostPreferenceResource preferenceResource =
	                                (UserComputeHostPreferenceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.USER_COMPUTE_RESOURCE_PREFERENCE, preference);
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
                logger.error("Unsupported field name for Compute host preference Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Compute host preference Resource.");
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
            UserComputeResourcePreference existingPreference = em.find(UserComputeResourcePreference.class, new UserComputeResourcePreferencePK(userId, gatewayID, resourceId));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            ComputeResource computeResource = em.find(ComputeResource.class, resourceId);
            UserResourceProfile userResourceProfile = em.find(UserResourceProfile.class, new UserResourceProfilePK(userId,gatewayID));
            if (existingPreference != null) {
                existingPreference.setResourceId(resourceId);
                existingPreference.setUserId(userId);
                existingPreference.setGatewayID(gatewayID);
                existingPreference.setComputeHostResource(computeResource);
                existingPreference.setUserResouceProfile(userResourceProfile);
                existingPreference.setScratchLocation(scratchLocation);
                existingPreference.setProjectNumber(projectNumber);
                existingPreference.setBatchQueue(batchQueue);
                existingPreference.setLoginUserName(loginUserName);
                existingPreference.setComputeResourceCSToken(resourceCSToken);
                existingPreference.setQualityOfService(qualityOfService);
                existingPreference.setReservation(reservation);
                existingPreference.setReservationStartTime(reservationStartTime);
                existingPreference.setReservationEndTime(reservationEndTime);
                existingPreference.setValidated(validated);
                em.merge(existingPreference);
            } else {
                UserComputeResourcePreference resourcePreference = new UserComputeResourcePreference();
                resourcePreference.setResourceId(resourceId);
                resourcePreference.setGatewayID(gatewayID);
                resourcePreference.setUserId(userId);
                resourcePreference.setComputeHostResource(computeResource);
                resourcePreference.setScratchLocation(scratchLocation);
                resourcePreference.setProjectNumber(projectNumber);
                resourcePreference.setBatchQueue(batchQueue);
                resourcePreference.setLoginUserName(loginUserName);
                resourcePreference.setComputeResourceCSToken(resourceCSToken);
                resourcePreference.setQualityOfService(qualityOfService);
                resourcePreference.setReservation(reservation);
                resourcePreference.setReservationStartTime(reservationStartTime);
                resourcePreference.setReservationEndTime(reservationEndTime);
                resourcePreference.setValidated(validated);
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
            String resourceId = ids.getTopLevelIdentifier().toString();
            String userId = ids.getSecondLevelIdentifier().toString();
            String gatewayId = ids.getThirdLevelIdentifier().toString();
            UserComputeResourcePreference existingPreference = em.find(UserComputeResourcePreference.class,
                    new UserComputeResourcePreferencePK(userId, gatewayId, resourceId));
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
