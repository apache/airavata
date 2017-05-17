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
package org.apache.airavata.registry.core.experiment.catalog.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;

@Entity
@Table(name = "GATEWAY")
public class Gateway {
    private final static Logger logger = LoggerFactory.getLogger(Gateway.class);
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
    private String getGatewayAdminLastName;
    private String gatewayAdminEmail;
    private String identityServerUserName;
    private String identityServerPasswordToken;
    private String declinedReason;
    private String oauthClientId;
    private String getOauthClientSecret;
    private Timestamp requestCreationTime;
    private String requesterUsername;
    private Collection<GatewayWorker> gatewayWorkers;
    private Collection<Project> projects;

    @Id
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
    public String getGetGatewayAdminLastName() {
        return getGatewayAdminLastName;
    }

    public void setGetGatewayAdminLastName(String getGatewayAdminLastName) {
        this.getGatewayAdminLastName = getGatewayAdminLastName;
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
    public Timestamp getRequestCreationTime() {
        return requestCreationTime;
    }

    public void setRequestCreationTime(Timestamp requestCreationTime) {
        this.requestCreationTime = requestCreationTime;
    }

    @Column(name = "OAUTH_CLIENT_SECRET")
    public String getGetOauthClientSecret() {
        return getOauthClientSecret;
    }

    public void setGetOauthClientSecret(String oauthClientSecret) {
        this.getOauthClientSecret = oauthClientSecret;
    }

    //    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        Gateway gateway = (Gateway) o;
//
//        if (domain != null ? !domain.equals(gateway.domain) : gateway.domain != null) return false;
//        if (emailAddress != null ? !emailAddress.equals(gateway.emailAddress) : gateway.emailAddress != null)
//            return false;
//        if (gatewayId != null ? !gatewayId.equals(gateway.gatewayId) : gateway.gatewayId != null) return false;
//        if (gatewayName != null ? !gatewayName.equals(gateway.gatewayName) : gateway.gatewayName != null)
//            return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = gatewayId != null ? gatewayId.hashCode() : 0;
//        result = 31 * result + (gatewayName != null ? gatewayName.hashCode() : 0);
//        result = 31 * result + (domain != null ? domain.hashCode() : 0);
//        result = 31 * result + (emailAddress != null ? emailAddress.hashCode() : 0);
//        return result;
//    }

    @OneToMany(mappedBy = "gateway")
    public Collection<GatewayWorker> getGatewayWorkers() {
        return gatewayWorkers;
    }

    public void setGatewayWorkers(Collection<GatewayWorker> gatewayWorkersByGatewayId) {
        this.gatewayWorkers = gatewayWorkersByGatewayId;
    }

    @OneToMany(mappedBy = "gateway")
    public Collection<Project> getProjects() {
        return projects;
    }

    public void setProjects(Collection<Project> projectByGatewayId) {
        this.projects = projectByGatewayId;
    }
}