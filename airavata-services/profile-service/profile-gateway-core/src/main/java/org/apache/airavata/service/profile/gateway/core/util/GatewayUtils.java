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
package org.apache.airavata.service.profile.gateway.core.util;

import org.apache.airavata.service.profile.gateway.core.entities.Gateway;
import org.apache.airavata.service.profile.gateway.core.resources.GatewayResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
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
    public static GatewayResource createGateway(Gateway gateway) {
        GatewayResource gatewayResource = new GatewayResource();
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

        return gatewayResource;
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
                    GatewayResource gatewayResource = createGateway(gateway);
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
}
