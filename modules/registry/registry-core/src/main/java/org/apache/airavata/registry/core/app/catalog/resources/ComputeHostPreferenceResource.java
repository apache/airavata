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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComputeHostPreferenceResource extends AppCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(ComputeHostPreferenceResource.class);
    private String gatewayId;
    private String resourceId;
    private boolean overrideByAiravata;
    private String preferredJobProtocol;
    private String preferedDMProtocol;
    private String batchQueue;
    private String scratchLocation;
    private String projectNumber;
    private String loginUserName;
    private String resourceCSToken;
    private String usageReportingGatewayId;
    private String qualityOfService;
    private String reservation;
    private Timestamp reservationStartTime;
    private Timestamp reservationEndTime;
    private String sshAccountProvisioner;
    private Map<String,String> sshAccountProvisionerConfigurations;
    private String sshAccountProvisionerAdditionalInfo;

    private GatewayProfileResource gatewayProfile;
    private ComputeResourceResource computeHostResource;

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

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public boolean getOverrideByAiravata() {
        return overrideByAiravata;
    }

    public void setOverrideByAiravata(boolean overrideByAiravata) {
        this.overrideByAiravata = overrideByAiravata;
    }

    public String getPreferredJobProtocol() {
        return preferredJobProtocol;
    }

    public void setPreferredJobProtocol(String preferredJobProtocol) {
        this.preferredJobProtocol = preferredJobProtocol;
    }

    public String getPreferedDMProtocol() {
        return preferedDMProtocol;
    }

    public void setPreferedDMProtocol(String preferedDMProtocol) {
        this.preferedDMProtocol = preferedDMProtocol;
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

    public GatewayProfileResource getGatewayProfile() {
        return gatewayProfile;
    }

    public void setGatewayProfile(GatewayProfileResource gatewayProfile) {
        this.gatewayProfile = gatewayProfile;
    }

    public void setUserResourceProfile(GatewayProfileResource gatewayProfile) {
        this.gatewayProfile = gatewayProfile;
    }

    public ComputeResourceResource getComputeHostResource() {
        return computeHostResource;
    }

    public void setComputeHostResource(ComputeResourceResource computeHostResource) {
        this.computeHostResource = computeHostResource;
    }

    public String getResourceCSToken() {
        return resourceCSToken;
    }

    public void setResourceCSToken(String resourceCSToken) {
        this.resourceCSToken = resourceCSToken;
    }

    public String getUsageReportingGatewayId() {
        return usageReportingGatewayId;
    }

    public void setUsageReportingGatewayId(String usageReportingGatewayId) {
        this.usageReportingGatewayId = usageReportingGatewayId;
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

    public String getSshAccountProvisioner() {
        return sshAccountProvisioner;
    }

    public void setSshAccountProvisioner(String sshAccountProvisioner) {
        this.sshAccountProvisioner = sshAccountProvisioner;
    }

    public Map<String, String> getSshAccountProvisionerConfigurations() {
        return sshAccountProvisionerConfigurations;
    }

    public void setSshAccountProvisionerConfigurations(Map<String, String> sshAccountProvisionerConfigurations) {
        this.sshAccountProvisionerConfigurations = sshAccountProvisionerConfigurations;
    }

    public String getSshAccountProvisionerAdditionalInfo() {
        return sshAccountProvisionerAdditionalInfo;
    }

    public void setSshAccountProvisionerAdditionalInfo(String sshAccountProvisionerAdditionalInfo) {
        this.sshAccountProvisionerAdditionalInfo = sshAccountProvisionerAdditionalInfo;
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(COMPUTE_RESOURCE_PREFERENCE);
            generator.setParameter(ComputeResourcePreferenceConstants.RESOURCE_ID, ids.get(ComputeResourcePreferenceConstants.RESOURCE_ID));
            generator.setParameter(ComputeResourcePreferenceConstants.GATEWAY_ID, ids.get(ComputeResourcePreferenceConstants.GATEWAY_ID));

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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(COMPUTE_RESOURCE_PREFERENCE);
            generator.setParameter(ComputeResourcePreferenceConstants.GATEWAY_ID, ids.get(ComputeResourcePreferenceConstants.GATEWAY_ID));
            generator.setParameter(ComputeResourcePreferenceConstants.RESOURCE_ID, ids.get(ComputeResourcePreferenceConstants.RESOURCE_ID));
            Query q = generator.selectQuery(em);
            ComputeResourcePreference preference = (ComputeResourcePreference) q.getSingleResult();
            ComputeHostPreferenceResource preferenceResource =
                    (ComputeHostPreferenceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.COMPUTE_RESOURCE_PREFERENCE, preference);
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(COMPUTE_RESOURCE_PREFERENCE);
            List results;
            if (fieldName.equals(ComputeResourcePreferenceConstants.RESOURCE_ID)) {
                generator.setParameter(ComputeResourcePreferenceConstants.RESOURCE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ComputeResourcePreference preference = (ComputeResourcePreference) result;
                        if (preference.getComputeHostResource()!=null) {
							ComputeHostPreferenceResource preferenceResource = (ComputeHostPreferenceResource) AppCatalogJPAUtils
									.getResource(
											AppCatalogResourceType.COMPUTE_RESOURCE_PREFERENCE,
											preference);
							preferenceResourceList.add(preferenceResource);
						}
                    }
                }
            } else if (fieldName.equals(ComputeResourcePreferenceConstants.GATEWAY_ID)) {
                generator.setParameter(ComputeResourcePreferenceConstants.GATEWAY_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ComputeResourcePreference preference = (ComputeResourcePreference) result;
                        if (preference.getComputeHostResource()!=null) {
	                        ComputeHostPreferenceResource preferenceResource =
	                                (ComputeHostPreferenceResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.COMPUTE_RESOURCE_PREFERENCE, preference);
	                        preferenceResourceList.add(preferenceResource);
                        }
                    }
                }
            } else if (fieldName.equals(ComputeResourcePreferenceConstants.PREFERED_JOB_SUB_PROTOCOL)) {
                generator.setParameter(ComputeResourcePreferenceConstants.PREFERED_JOB_SUB_PROTOCOL, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ComputeResourcePreference preference = (ComputeResourcePreference) result;
                        if (preference.getComputeHostResource()!=null) {
							ComputeHostPreferenceResource preferenceResource = (ComputeHostPreferenceResource) AppCatalogJPAUtils
									.getResource(
											AppCatalogResourceType.COMPUTE_RESOURCE_PREFERENCE,
											preference);
							preferenceResourceList.add(preferenceResource);
						}
                    }
                }
            } else if (fieldName.equals(ComputeResourcePreferenceConstants.PREFERED_DATA_MOVE_PROTOCOL)) {
                generator.setParameter(ComputeResourcePreferenceConstants.PREFERED_DATA_MOVE_PROTOCOL, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ComputeResourcePreference preference = (ComputeResourcePreference) result;
                        if (preference.getResourceId()!=null) {
							ComputeHostPreferenceResource preferenceResource = (ComputeHostPreferenceResource) AppCatalogJPAUtils
									.getResource(
											AppCatalogResourceType.COMPUTE_RESOURCE_PREFERENCE,
											preference);
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
            ComputeResourcePreference existingPreference = em.find(ComputeResourcePreference.class, new ComputeResourcePreferencePK(gatewayId, resourceId));

            em.getTransaction().begin();
            ComputeResource computeResource = em.find(ComputeResource.class, resourceId);
            GatewayProfile gatewayProf = em.find(GatewayProfile.class, gatewayId);
            if (existingPreference != null) {
                existingPreference.setResourceId(resourceId);
                existingPreference.setGatewayId(gatewayId);
                existingPreference.setComputeHostResource(computeResource);
                existingPreference.setGatewayProfile(gatewayProf);
                existingPreference.setOverrideByAiravata(overrideByAiravata);
                existingPreference.setPreferedJobSubmissionProtocol(preferredJobProtocol);
                existingPreference.setPreferedDataMoveProtocol(preferedDMProtocol);
                existingPreference.setScratchLocation(scratchLocation);
                existingPreference.setProjectNumber(projectNumber);
                existingPreference.setBatchQueue(batchQueue);
                existingPreference.setLoginUserName(loginUserName);
                existingPreference.setComputeResourceCSToken(resourceCSToken);
                existingPreference.setUsageReportingGWId(usageReportingGatewayId);
                existingPreference.setQualityOfService(qualityOfService);
                existingPreference.setReservation(reservation);
                existingPreference.setReservationStartTime(reservationStartTime);
                existingPreference.setReservationEndTime(reservationEndTime);
                existingPreference.setSshAccountProvisioner(sshAccountProvisioner);
                if (sshAccountProvisionerConfigurations != null && !sshAccountProvisionerConfigurations.isEmpty()) {
                    List<SSHAccountProvisionerConfiguration> configurations = new ArrayList<>();
                    for (String sshAccountProvisionerConfigName : sshAccountProvisionerConfigurations.keySet()) {
                        String value = sshAccountProvisionerConfigurations.get(sshAccountProvisionerConfigName);
                        configurations.add(new SSHAccountProvisionerConfiguration(sshAccountProvisionerConfigName, value, existingPreference));
                    }
                    existingPreference.setSshAccountProvisionerConfigurations(configurations);
                } else {
                    existingPreference.setSshAccountProvisionerConfigurations(null);
                }
                existingPreference.setSshAccountProvisionerAdditionalInfo(sshAccountProvisionerAdditionalInfo);
                em.merge(existingPreference);
            } else {
                ComputeResourcePreference resourcePreference = new ComputeResourcePreference();
                resourcePreference.setResourceId(resourceId);
                resourcePreference.setGatewayId(gatewayId);
                resourcePreference.setComputeHostResource(computeResource);
                resourcePreference.setGatewayProfile(gatewayProf);
                resourcePreference.setOverrideByAiravata(overrideByAiravata);
                resourcePreference.setPreferedJobSubmissionProtocol(preferredJobProtocol);
                resourcePreference.setPreferedDataMoveProtocol(preferedDMProtocol);
                resourcePreference.setScratchLocation(scratchLocation);
                resourcePreference.setProjectNumber(projectNumber);
                resourcePreference.setBatchQueue(batchQueue);
                resourcePreference.setLoginUserName(loginUserName);
                resourcePreference.setComputeResourceCSToken(resourceCSToken);
                resourcePreference.setUsageReportingGWId(usageReportingGatewayId);
                resourcePreference.setQualityOfService(qualityOfService);
                resourcePreference.setReservation(reservation);
                resourcePreference.setReservationStartTime(reservationStartTime);
                resourcePreference.setReservationEndTime(reservationEndTime);
                resourcePreference.setSshAccountProvisioner(sshAccountProvisioner);
                if (sshAccountProvisionerConfigurations != null && !sshAccountProvisionerConfigurations.isEmpty()){
                    List<SSHAccountProvisionerConfiguration> configurations = new ArrayList<>();
                    for (String sshAccountProvisionerConfigName : sshAccountProvisionerConfigurations.keySet()) {
                        String value = sshAccountProvisionerConfigurations.get(sshAccountProvisionerConfigName);
                        configurations.add(new SSHAccountProvisionerConfiguration(sshAccountProvisionerConfigName, value, resourcePreference));
                    }
                    resourcePreference.setSshAccountProvisionerConfigurations(configurations);
                }
                resourcePreference.setSshAccountProvisionerAdditionalInfo(sshAccountProvisionerAdditionalInfo);

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
            ComputeResourcePreference existingPreference = em.find(ComputeResourcePreference.class,
                    new ComputeResourcePreferencePK(ids.get(ComputeResourcePreferenceConstants.GATEWAY_ID),
                            ids.get(ComputeResourcePreferenceConstants.RESOURCE_ID)));
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
