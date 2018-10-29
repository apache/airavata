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
package org.apache.airavata.registry.core.entities.expcatalog;

import org.apache.airavata.model.workspace.GatewayApprovalStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * The persistent class for the gateway database table.
 */
@Entity
@Table(name="GATEWAY")
public class GatewayEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Column(name = "GATEWAY_NAME")
    private String gatewayName;

    @Column(name = "DOMAIN")
    private String domain;

    @Column(name = "EMAIL_ADDRESS")
    private String emailAddress;

    @Column(name = "GATEWAY_APPROVAL_STATUS")
    @Enumerated(EnumType.STRING)
    private GatewayApprovalStatus gatewayApprovalStatus;

    @Column(name = "GATEWAY_ACRONYM")
    private String gatewayAcronym;

    @Column(name = "GATEWAY_URL")
    private String gatewayUrl;

    @Column(name = "GATEWAY_PUBLIC_ABSTRACT")
    private String gatewayPublicAbstract;

    @Column(name = "GATEWAY_REVIEW_PROPOSAL_DESCRIPTION")
    private String reviewProposalDescription;

    @Column(name = "GATEWAY_ADMIN_FIRST_NAME")
    private String gatewayAdminFirstName;

    @Column(name = "GATEWAY_ADMIN_LAST_NAME")
    private String gatewayAdminLastName;

    @Column(name = "GATEWAY_ADMIN_EMAIL")
    private String gatewayAdminEmail;

    @Column(name = "IDENTITY_SERVER_USERNAME")
    private String identityServerUserName;

    @Column(name = "IDENTITY_SERVER_PASSWORD_TOKEN")
    private String identityServerPasswordToken;

    @Column(name = "DECLINED_REASON")
    private String declinedReason;

    @Column(name = "OAUTH_CLIENT_ID")
    private String oauthClientId;

    @Column(name = "OAUTH_CLIENT_SECRET")
    private String oauthClientSecret;

    @Column(name = "REQUEST_CREATION_TIME")
    private Timestamp requestCreationTime;

    @Column(name = "REQUESTER_USERNAME")
    private String requesterUsername;

    public GatewayEntity() {
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String id) {
        this.gatewayId = id;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public GatewayApprovalStatus getGatewayApprovalStatus() {
        return gatewayApprovalStatus;
    }

    public void setGatewayApprovalStatus(GatewayApprovalStatus gatewayApprovalStatus) {
        this.gatewayApprovalStatus = gatewayApprovalStatus;
    }

    public String getGatewayAcronym() {
        return gatewayAcronym;
    }

    public void setGatewayAcronym(String gatewayAcronym) {
        this.gatewayAcronym = gatewayAcronym;
    }

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }

    public String getGatewayPublicAbstract() {
        return gatewayPublicAbstract;
    }

    public void setGatewayPublicAbstract(String gatewayPublicAbstract) {
        this.gatewayPublicAbstract = gatewayPublicAbstract;
    }

    public String getReviewProposalDescription() {
        return reviewProposalDescription;
    }

    public void setReviewProposalDescription(String reviewProposalDescription) {
        this.reviewProposalDescription = reviewProposalDescription;
    }

    public String getGatewayAdminFirstName() {
        return gatewayAdminFirstName;
    }

    public void setGatewayAdminFirstName(String gatewayAdminFirstName) {
        this.gatewayAdminFirstName = gatewayAdminFirstName;
    }

    public String getGatewayAdminLastName() {
        return gatewayAdminLastName;
    }

    public void setGatewayAdminLastName(String gatewayAdminLastName) {
        this.gatewayAdminLastName = gatewayAdminLastName;
    }

    public String getGatewayAdminEmail() {
        return gatewayAdminEmail;
    }

    public void setGatewayAdminEmail(String gatewayAdminEmail) {
        this.gatewayAdminEmail = gatewayAdminEmail;
    }

    public String getIdentityServerUserName() {
        return identityServerUserName;
    }

    public void setIdentityServerUserName(String identityServerUserName) {
        this.identityServerUserName = identityServerUserName;
    }

    public String getIdentityServerPasswordToken() {
        return identityServerPasswordToken;
    }

    public void setIdentityServerPasswordToken(String identityServerPasswordToken) {
        this.identityServerPasswordToken = identityServerPasswordToken;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public void setRequesterUsername(String requesterUsername) {
        this.requesterUsername = requesterUsername;
    }

    public String getDeclinedReason() {
        return declinedReason;
    }

    public void setDeclinedReason(String declinedReason) {
        this.declinedReason = declinedReason;
    }


    public String getOauthClientId() {
        return oauthClientId;
    }

    public void setOauthClientId(String oauthClientId) {
        this.oauthClientId = oauthClientId;
    }

    public String getOauthClientSecret() {
        return oauthClientSecret;
    }

    public void setOauthClientSecret(String oauthClientSecret) {
        this.oauthClientSecret = oauthClientSecret;
    }

    public Timestamp getRequestCreationTime() {
        return requestCreationTime;
    }

    public void setRequestCreationTime(Timestamp requestCreationTime) {
        this.requestCreationTime = requestCreationTime;
    }

}