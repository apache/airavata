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
package org.apache.airavata.service.profile.commons.tenant.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "GATEWAY")
public class GatewayEntity {
    private final static Logger logger = LoggerFactory.getLogger(GatewayEntity.class);
    private String airavataInternalGatewayId;
    private String gatewayId;
    private String gatewayName;
    private String domain;
    private String emailAddress;
    private String gatewayApprovalStatus;
    private String gatewayAcronym;
    private String gatewayUrl;
    private String gatewayPublicAbstract;
    private String reviewProposalDescription;
    private String gatewayAdminFirstName;
    private String gatewayAdminLastName;
    private String gatewayAdminEmail;
    private String identityServerUserName;
    private String identityServerPasswordToken;
    private String declinedReason;
    private String oauthClientId;
    private String oauthClientSecret;
    private long requestCreationTime;
    private String requesterUsername;

    // set random value for internalGatewayId
    public GatewayEntity() {
        this.airavataInternalGatewayId = UUID.randomUUID().toString();
    }

    @Id
    @Column(name = "AIRAVATA_INTERNAL_GATEWAY_ID")
    public String getAiravataInternalGatewayId() {
        return airavataInternalGatewayId;
    }

    public void setAiravataInternalGatewayId(String airavataInternalGatewayId) {
        this.airavataInternalGatewayId = airavataInternalGatewayId;
    }

    @Column(name = "GATEWAY_ID")
    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Column(name = "GATEWAY_NAME")
    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    @Column(name = "GATEWAY_DOMAIN")
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Column(name = "EMAIL_ADDRESS")
    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Column(name = "GATEWAY_APPROVAL_STATUS")
    public String getGatewayApprovalStatus() {
        return gatewayApprovalStatus;
    }

    public void setGatewayApprovalStatus(String gatewayApprovalStatus) {
        this.gatewayApprovalStatus = gatewayApprovalStatus;
    }

    @Column(name = "GATEWAY_ACRONYM")
    public String getGatewayAcronym() {
        return gatewayAcronym;
    }

    public void setGatewayAcronym(String gatewayAcronym) {
        this.gatewayAcronym = gatewayAcronym;
    }

    @Column(name = "GATEWAY_URL")
    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }

    @Column(name = "GATEWAY_PUBLIC_ABSTRACT")
    public String getGatewayPublicAbstract() {
        return gatewayPublicAbstract;
    }

    public void setGatewayPublicAbstract(String gatewayPublicAbstract) {
        this.gatewayPublicAbstract = gatewayPublicAbstract;
    }

    @Column(name = "GATEWAY_REVIEW_PROPOSAL_DESCRIPTION")
    public String getReviewProposalDescription() {
        return reviewProposalDescription;
    }

    public void setReviewProposalDescription(String reviewProposalDescription) {
        this.reviewProposalDescription = reviewProposalDescription;
    }

    @Column(name = "GATEWAY_ADMIN_FIRST_NAME")
    public String getGatewayAdminFirstName() {
        return gatewayAdminFirstName;
    }

    public void setGatewayAdminFirstName(String gatewayAdminFirstName) {
        this.gatewayAdminFirstName = gatewayAdminFirstName;
    }

    @Column(name = "GATEWAY_ADMIN_LAST_NAME")
    public String getGatewayAdminLastName() {
        return gatewayAdminLastName;
    }

    public void setGatewayAdminLastName(String gatewayAdminLastName) {
        this.gatewayAdminLastName = gatewayAdminLastName;
    }

    @Column(name = "GATEWAY_ADMIN_EMAIL")
    public String getGatewayAdminEmail() {
        return gatewayAdminEmail;
    }

    public void setGatewayAdminEmail(String gatewayAdminEmail) {
        this.gatewayAdminEmail = gatewayAdminEmail;
    }

    @Column(name = "IDENTITY_SERVER_USERNAME")
    public String getIdentityServerUserName() {
        return identityServerUserName;
    }

    public void setIdentityServerUserName(String identityServerUserName) {
        this.identityServerUserName = identityServerUserName;
    }

    @Column(name = "IDENTITY_SERVER_PASSWORD_TOKEN")
    public String getIdentityServerPasswordToken() {
        return identityServerPasswordToken;
    }

    public void setIdentityServerPasswordToken(String identityServerPasswordToken) {
        this.identityServerPasswordToken = identityServerPasswordToken;
    }

    @Column(name = "REQUESTER_USERNAME")
    public String getRequesterUsername() {
        return requesterUsername;
    }

    public void setRequesterUsername(String requesterUsername) {
        this.requesterUsername = requesterUsername;
    }

    @Column(name = "DECLINED_REASON")
    public String getDeclinedReason() {
        return declinedReason;
    }

    public void setDeclinedReason(String declinedReason) {
        this.declinedReason = declinedReason;
    }

    @Column(name = "OAUTH_CLIENT_ID")
    public String getOauthClientId() {
        return oauthClientId;
    }

    public void setOauthClientId(String oauthClientId) {
        this.oauthClientId = oauthClientId;
    }

    @Column(name = "REQUEST_CREATION_TIME")
    public long getRequestCreationTime() {
        return requestCreationTime;
    }

    public void setRequestCreationTime(long requestCreationTime) {
        this.requestCreationTime = requestCreationTime;
    }

    @Column(name = "OAUTH_CLIENT_SECRET")
    public String getOauthClientSecret() {
        return oauthClientSecret;
    }

    public void setOauthClientSecret(String oauthClientSecret) {
        this.oauthClientSecret = oauthClientSecret;
    }

    @PrePersist
    void createdAt() {
        this.requestCreationTime = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GatewayEntity)) {
            return false;
        }
        GatewayEntity gwy = (GatewayEntity) obj;
        return getAiravataInternalGatewayId().equals(gwy.getAiravataInternalGatewayId());
    }

    @Override
    public String toString() {
        return "GatewayEntity{" +
                "airavataInternalGatewayId='" + airavataInternalGatewayId + '\'' +
                ", gatewayId='" + gatewayId + '\'' +
                ", gatewayName='" + gatewayName + '\'' +
                ", domain='" + domain + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", gatewayApprovalStatus='" + gatewayApprovalStatus + '\'' +
                ", gatewayAcronym='" + gatewayAcronym + '\'' +
                ", gatewayUrl='" + gatewayUrl + '\'' +
                ", gatewayPublicAbstract='" + gatewayPublicAbstract + '\'' +
                ", reviewProposalDescription='" + reviewProposalDescription + '\'' +
                ", gatewayAdminFirstName='" + gatewayAdminFirstName + '\'' +
                ", gatewayAdminLastName='" + gatewayAdminLastName + '\'' +
                ", gatewayAdminEmail='" + gatewayAdminEmail + '\'' +
                ", identityServerUserName='" + identityServerUserName + '\'' +
                ", identityServerPasswordToken='" + identityServerPasswordToken + '\'' +
                ", declinedReason='" + declinedReason + '\'' +
                ", oauthClientId='" + oauthClientId + '\'' +
                ", oauthClientSecret='" + oauthClientSecret + '\'' +
                ", requestCreationTime=" + requestCreationTime +
                ", requesterUsername='" + requesterUsername + '\'' +
                '}';
    }
}
