/**
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

package org.apache.airavata.service.profile.gateway.core.resources;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.service.profile.gateway.core.entities.GatewayProfile;
import org.apache.airavata.service.profile.gateway.core.util.JPAUtils;
import org.apache.airavata.service.profile.gateway.core.util.QueryConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class GatewayProfileResource {
    private final static Logger logger = LoggerFactory.getLogger(GatewayProfileResource.class);

    private String gatewayID;
    private Timestamp createdTime;
    private Timestamp updatedTime;
    private String credentialStoreToken;
    private String identityServerTenant;
    private String identityServerPwdCredToken;

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

    public void remove(String gatewayID) throws Exception {
        EntityManager em = null;
        try {
            em = JPAUtils.getEntityManager();
            em.getTransaction().begin();
            GatewayProfile gp = em.find(GatewayProfile.class, gatewayID);
            em.remove(gp);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public GatewayProfileResource getByID(String gatewayID) throws Exception {
        EntityManager em = null;
        try {
            em = JPAUtils.getEntityManager();
            em.getTransaction().begin();
            GatewayProfile gatewayProfile = em.find(GatewayProfile.class, gatewayID);
            GatewayProfileResource gatewayProfileResource = JPAUtils.createGatewayProfile(gatewayProfile);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return gatewayProfileResource;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<GatewayProfileResource> getListByID(String gatewayID) throws Exception {
        List<GatewayProfileResource> gatewayProfileResources = new ArrayList<GatewayProfileResource>();
        EntityManager em = null;
        try {
            em = JPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q = em.createQuery(MessageFormat.format(QueryConstants.FIND_GATEWAY_PROFILE_BY_ID, gatewayID));
            List<GatewayProfile> results = q.getResultList();
            if (results.size() != 0) {
                for (GatewayProfile gatewayProfile : results) {
                    GatewayProfileResource gatewayProfileResource = JPAUtils.createGatewayProfile(gatewayProfile);
                    gatewayProfileResources.add(gatewayProfileResource);
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
            throw e;
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return gatewayProfileResources;
    }

    @SuppressWarnings("unchecked")
    public List<GatewayProfileResource> getAll() throws Exception {
        List<GatewayProfileResource> resourceList = new ArrayList<GatewayProfileResource>();
        EntityManager em = null;
        try {
            em = JPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q = em.createQuery(QueryConstants.GET_ALL_GATEWAY_PROFILES);
            List<GatewayProfile> results = q.getResultList();
            if (results.size() != 0) {
                for (GatewayProfile gatewayProfile : results) {
                    GatewayProfileResource gatewayProfileResource = JPAUtils.createGatewayProfile(gatewayProfile);
                    resourceList.add(gatewayProfileResource);
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
            throw e;
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

    @SuppressWarnings("unchecked")
    public List<String> getGatewayProfileIds(String gatewayID) throws Exception {
        List<String> gatewayProfileResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = JPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q = em.createQuery(MessageFormat.format(QueryConstants.FIND_GATEWAY_PROFILE_BY_ID, gatewayID));
            List<GatewayProfile> results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    GatewayProfile gatewayProfile = (GatewayProfile) result;
                    gatewayProfileResourceIDs.add(gatewayProfile.getGatewayID());
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
            throw e;
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return gatewayProfileResourceIDs;
    }

    public void save() throws Exception {
        EntityManager em = null;
        try {
            em = JPAUtils.getEntityManager();
            GatewayProfile existingGatewayProfile = em.find(GatewayProfile.class, gatewayID);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = JPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingGatewayProfile != null) {
                existingGatewayProfile.setUpdateTime(AiravataUtils.getCurrentTimestamp());
                if (credentialStoreToken != null){
                    existingGatewayProfile.setCredentialStoreToken(credentialStoreToken);
                }
                if (identityServerTenant != null){
                    existingGatewayProfile.setIdentityServerTenant(identityServerTenant);
                }
                if (identityServerPwdCredToken != null){
                    existingGatewayProfile.setIdentityServerPwdCredToken(identityServerPwdCredToken);
                }
                em.merge(existingGatewayProfile);
            } else {
                GatewayProfile gatewayProfile = new GatewayProfile();
                gatewayProfile.setGatewayID(gatewayID);
                gatewayProfile.setCreationTime(AiravataUtils.getCurrentTimestamp());
                if (credentialStoreToken != null){
                    gatewayProfile.setCredentialStoreToken(credentialStoreToken);
                }
                if (identityServerTenant != null){
                    gatewayProfile.setIdentityServerTenant(identityServerTenant);
                }
                if (identityServerPwdCredToken != null){
                    gatewayProfile.setIdentityServerPwdCredToken(identityServerPwdCredToken);
                }
                em.persist(gatewayProfile);
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
            throw e;
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public boolean isExists(Object identifier) throws Exception {
        EntityManager em = null;
        try {
            em = JPAUtils.getEntityManager();
            GatewayProfile gatewayProfile = em.find(GatewayProfile.class, identifier);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return gatewayProfile != null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public String getGatewayID() {
        return gatewayID;
    }

    public void setGatewayID(String gatewayID) {
        this.gatewayID = gatewayID;
    }
}
