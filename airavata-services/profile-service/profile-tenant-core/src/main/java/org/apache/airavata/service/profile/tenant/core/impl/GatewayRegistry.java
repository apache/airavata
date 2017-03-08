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

package org.apache.airavata.service.profile.tenant.core.impl;

import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.service.profile.commons.utils.ObjectMapperSingleton;
import org.apache.airavata.service.profile.tenant.core.resources.GatewayResource;
import org.apache.airavata.service.profile.tenant.core.util.GatewayUtils;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GatewayRegistry {

    private final static Logger logger = LoggerFactory.getLogger(GatewayRegistry.class);

    public GatewayResource getExistingGateway (String gatewayName) throws Exception {
        return GatewayUtils.getGateway(gatewayName);
    }

    public String addGateway (Gateway gateway) throws Exception {
        try {
            GatewayResource resource = GatewayUtils.createGateway(gateway.getGatewayId());
            resource.setGatewayApprovalStatus(gateway.getGatewayApprovalStatus().toString());
            resource.setGatewayName(gateway.getGatewayName());
            resource.setEmailAddress(gateway.getEmailAddress());
            resource.setDomain(gateway.getDomain());
            resource.setGatewayAcronym(gateway.getGatewayAcronym());
            resource.setGatewayUrl(gateway.getGatewayURL());
            resource.setGatewayPublicAbstract(gateway.getGatewayPublicAbstract());
            resource.setReviewProposalDescription(gateway.getReviewProposalDescription());
            resource.setGatewayAdminFirstName(gateway.getGatewayAdminFirstName());
            resource.setGetGatewayAdminLastName(gateway.getGatewayAdminLastName());
            resource.setGatewayAdminEmail(gateway.getGatewayAdminEmail());
            resource.setIdentityServerUserName(gateway.getIdentityServerUserName());
            resource.setIdentityServerPasswordToken(gateway.getIdentityServerPasswordToken());
            resource.setDeclinedReason(gateway.getDeclinedReason());
            resource.setOauthClientId(gateway.getOauthClientId());
            resource.setOauthClientSecret(gateway.getOauthClientSecret());
            resource.setRequestCreationTime(new Timestamp(System.currentTimeMillis()));
            resource.setRequesterUsername(gateway.getRequesterUsername());
            resource.save();
            return gateway.getGatewayId();
        } catch (Exception e){
            logger.error("Error while saving gateway to registry, reason: " + e.getMessage(), e);
            throw e;
        }
    }

    public void updateGateway (String gatewayId, Gateway updatedGateway) throws Exception {
        try {
            GatewayResource existingGateway = GatewayUtils.getGateway(gatewayId);
            existingGateway.setGatewayApprovalStatus(updatedGateway.getGatewayApprovalStatus().toString());
            existingGateway.setGatewayName(updatedGateway.getGatewayName());
            existingGateway.setEmailAddress(updatedGateway.getEmailAddress());
            existingGateway.setDomain(updatedGateway.getDomain());
            existingGateway.setGatewayAcronym(updatedGateway.getGatewayAcronym());
            existingGateway.setGatewayUrl(updatedGateway.getGatewayURL());
            existingGateway.setGatewayPublicAbstract(updatedGateway.getGatewayPublicAbstract());
            existingGateway.setReviewProposalDescription(updatedGateway.getReviewProposalDescription());
            existingGateway.setGatewayAdminFirstName(updatedGateway.getGatewayAdminFirstName());
            existingGateway.setGetGatewayAdminLastName(updatedGateway.getGatewayAdminLastName());
            existingGateway.setGatewayAdminEmail(updatedGateway.getGatewayAdminEmail());
            existingGateway.setIdentityServerUserName(updatedGateway.getIdentityServerUserName());
            existingGateway.setIdentityServerPasswordToken(updatedGateway.getIdentityServerPasswordToken());
            existingGateway.setDeclinedReason(updatedGateway.getDeclinedReason());
            existingGateway.setOauthClientId(updatedGateway.getOauthClientId());
            existingGateway.setOauthClientSecret(updatedGateway.getOauthClientSecret());
            existingGateway.setRequesterUsername(updatedGateway.getRequesterUsername());
            existingGateway.save();
        } catch (Exception e){
            logger.error("Error while updating gateway to registry, reason: " + e.getMessage(), e);
            throw e;
        }
    }

    public Gateway getGateway (String gatewayId) throws Exception {
        try {
            GatewayResource resource = GatewayUtils.getGateway(gatewayId);
            return GatewayUtils.toGateway(resource);
        } catch (Exception e){
            logger.error("Error while getting gateway, reason: " + e.getMessage(), e);
            throw e;
        }
    }

    public boolean isGatewayExist (String gatewayId) throws Exception {
        try {
            return GatewayUtils.isGatewayExist(gatewayId);
        } catch (Exception e){
            logger.error("Error while checking gateway exists, reason: " + e.getMessage(), e);
            throw e;
        }
    }

    public boolean removeGateway (String gatewayId) throws Exception {
        try {
            return GatewayUtils.removeGateway(gatewayId);
        } catch (Exception e){
            logger.error("Error while removing the gateway, reason: " + e.getMessage(), e);
            throw new Exception(e);
        }
    }

    public List<Gateway> getAllGateways () throws Exception {
        List<Gateway> gatewayList = new ArrayList<Gateway>();
        try {
            List<GatewayResource> allGateways = GatewayUtils.getAllGateways();
            return GatewayUtils.getAllGateways(allGateways);
        } catch (Exception e){
            logger.error("Error while getting all the gateways, reason: ", e);
            throw e;
        }
    }

//    public static void main(String args[]) {
//        Mapper mapper = ObjectMapperSingleton.getInstance();
//
//        GatewayEntity g = new GatewayEntity();
//        g.setGatewayId("sd");
//        g.setRequestCreationTime(1213232);
//        g.setGatewayApprovalStatus(GatewayApprovalStatus.ACTIVE);
//        System.out.println("T: " + g);
//
//        org.apache.airavata.service.profile.commons.tenant.entities.GatewayEntity ge = new org.apache.airavata.service.profile.commons.tenant.entities.GatewayEntity();
//        ge.setGatewayId("ads");
//        ge.setRequestCreationTime(new Date().getTime());
//        ge.setGatewayApprovalStatus("ACTIVE");
//
//        Class t = GatewayEntity.class;
//        Class e = org.apache.airavata.service.profile.commons.tenant.entities.GatewayEntity.class;
//        Object o = mapper.map(ge, t);
//        System.out.println(o);
//    }

}
