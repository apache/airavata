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
package org.apache.airavata.service.profile.tenant.core.util;

import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.service.profile.tenant.core.entities.Gateway;
import org.apache.airavata.service.profile.tenant.core.resources.GatewayResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by goshenoy on 3/7/17.
 */
public class GatewayUtils {

    private final static Logger logger = LoggerFactory.getLogger(GatewayUtils.class);

    /**
     * This method converts Gateway object to GatewayResource
     * @param gateway
     * @return
     */
    public static GatewayResource toGatewayResource(Gateway gateway) throws Exception {
        GatewayResource gatewayResource = new GatewayResource();
        return toGatewayResource(gatewayResource, gateway);
    }

    /**
     * This method converts Gateway object to GatewayResource
     * @param gatewayResource
     * @param gateway
     * @return
     */
    public static GatewayResource toGatewayResource(GatewayResource gatewayResource, Gateway gateway) throws Exception {
        if (gatewayResource != null) {
            gatewayResource.setGatewayName(gateway.getGatewayName());
            gatewayResource.setGatewayId(gateway.getGatewayId());
            gatewayResource.setDomain(gateway.getDomain());
            gatewayResource.setEmailAddress(gateway.getEmailAddress());
            gatewayResource.setGatewayApprovalStatus(gateway.getGatewayApprovalStatus());
            gatewayResource.setGatewayAcronym(gateway.getGatewayAcronym());
            gatewayResource.setGatewayUrl(gateway.getGatewayUrl());
            gatewayResource.setGatewayPublicAbstract(gateway.getGatewayPublicAbstract());
            gatewayResource.setReviewProposalDescription(gateway.getReviewProposalDescription());
            gatewayResource.setGatewayAdminFirstName(gateway.getGatewayAdminFirstName());
            gatewayResource.setGetGatewayAdminLastName(gateway.getGetGatewayAdminLastName());
            gatewayResource.setGatewayAdminEmail(gateway.getGatewayAdminEmail());
            gatewayResource.setIdentityServerUserName(gateway.getIdentityServerUserName());
            gatewayResource.setIdentityServerPasswordToken(gateway.getIdentityServerPasswordToken());
            gatewayResource.setDeclinedReason(gateway.getDeclinedReason());
            gatewayResource.setOauthClientId(gateway.getOauthClientId());
            gatewayResource.setRequestCreationTime(gateway.getRequestCreationTime());
            gatewayResource.setRequesterUsername(gateway.getRequesterUsername());
            gatewayResource.setOauthClientSecret(gateway.getGetOauthClientSecret());
        } else {
            throw new Exception("Could not get GatewayResource object because Gateway object is null");
        }
        return gatewayResource;
    }

    /**
     * This method converts GatewayResource to Gateway
     * @param resource
     * @return
     */
    public static org.apache.airavata.model.workspace.Gateway toGateway (GatewayResource resource){
        org.apache.airavata.model.workspace.Gateway gateway = new org.apache.airavata.model.workspace.Gateway();
        gateway.setGatewayId(resource.getGatewayId());
        gateway.setGatewayApprovalStatus(GatewayApprovalStatus.valueOf(resource.getGatewayApprovalStatus()));
        gateway.setGatewayName(resource.getGatewayName());
        gateway.setDomain(resource.getDomain());
        gateway.setEmailAddress(resource.getEmailAddress());
        gateway.setGatewayAcronym(resource.getGatewayAcronym());
        gateway.setGatewayURL(resource.getGatewayUrl());
        gateway.setGatewayPublicAbstract(resource.getGatewayPublicAbstract());
        gateway.setReviewProposalDescription(resource.getReviewProposalDescription());
        gateway.setDeclinedReason(resource.getDeclinedReason());
        gateway.setGatewayAdminFirstName(resource.getGatewayAdminFirstName());
        gateway.setGatewayAdminLastName(resource.getGetGatewayAdminLastName());
        gateway.setGatewayAdminEmail(resource.getGatewayAdminEmail());
        gateway.setIdentityServerUserName(resource.getIdentityServerUserName());
        gateway.setIdentityServerPasswordToken(resource.getIdentityServerPasswordToken());
        gateway.setOauthClientId(resource.getOauthClientId());
        gateway.setOauthClientSecret(resource.getOauthClientSecret());
        if (resource.getRequestCreationTime() != null) {
            gateway.setRequestCreationTime(resource.getRequestCreationTime().getTime());
        }
        gateway.setRequesterUsername(resource.getRequesterUsername());
        return gateway;
    }

    /**
     * This method gets all gateways
     * @param gatewayList
     * @return
     */
    public static List<org.apache.airavata.model.workspace.Gateway> getAllGateways (List<GatewayResource> gatewayList){
        List<org.apache.airavata.model.workspace.Gateway> gateways = new ArrayList<org.apache.airavata.model.workspace.Gateway>();
        for (GatewayResource resource : gatewayList){
            gateways.add(toGateway(resource));
        }
        return gateways;
    }

    /**
     * This method creates new or returns existing gateway
     * @param gatewayId
     * @return
     * @throws Exception
     */
    public static GatewayResource createGateway(String gatewayId) throws Exception {
        if (!isGatewayExist(gatewayId)) {
            GatewayResource gatewayResource = new GatewayResource();
            gatewayResource.setGatewayId(gatewayId);
            return gatewayResource;
        } else {
            return getGateway(gatewayId);
        }
    }

    /**
     *  This method checks if a gateway exists
     * @param gatewayId
     * @return
     */
    public static boolean isGatewayExist(String gatewayId) throws Exception {
        EntityManager em = null;
        try {
            em = JPAUtils.getEntityManager();
            em.getTransaction().begin();
            Gateway gateway = em.find(Gateway.class, gatewayId);
            return gateway != null;
        } catch (Exception e){
            logger.error("Error checking if gateway exists, reason: " + e.getMessage(), e);
            throw e;
        } finally {
            if (em != null && em.isOpen()){
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }


    /**
     * This method finds a gateway by id
     * @param gatewayId
     * @return
     * @throws Exception
     */
    public static GatewayResource getGateway(String gatewayId) throws Exception {
        EntityManager em = null;
        try {
            if (isGatewayExist(gatewayId)) {
                em = JPAUtils.getEntityManager();
                Gateway gateway = em.find(Gateway.class, gatewayId);
                GatewayResource gatewayResource = toGatewayResource(gateway);
                em.close();
                return gatewayResource;
            }
        } catch (Exception e){
            logger.error("Error finding gateway, reason: " + e.getMessage(), e);
            throw e;
        } finally {
            if (em != null && em.isOpen()){
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return null;
    }

    /**
     * This method returns all gateways
     * @return
     * @throws Exception
     */
    public static List<GatewayResource> getAllGateways() throws Exception {
        List<GatewayResource> resourceList = new ArrayList<GatewayResource>();
        EntityManager em = null;
        try {
            em = JPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query query = em.createQuery(QueryConstants.GET_ALL_GATEWAYS);
            List results = query.getResultList();

            if (!results.isEmpty()) {
                for (Object result : results) {
                    Gateway gateway = (Gateway) result;
                    GatewayResource gatewayResource = toGatewayResource(gateway);
                    resourceList.add(gatewayResource);
                }
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            if (em != null && em.isOpen()){
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return resourceList;
    }

    /**
     * This method deletes a gateway
     * @param gatewayId
     * @return
     */
    public static boolean removeGateway(String gatewayId) {
        EntityManager em = null;
        try {
            em = JPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query query = em.createQuery(MessageFormat.format(QueryConstants.DELETE_GATEWAY_BY_ID, gatewayId));
            query.executeUpdate();
            em.getTransaction().commit();
            em.close();
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }finally {
            if (em != null && em.isOpen()){
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }
}
