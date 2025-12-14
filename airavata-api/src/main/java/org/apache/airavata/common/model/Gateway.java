/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.common.model;

import java.util.Objects;

/**
 * Domain model: Gateway
 */
public class Gateway {
    private String airavataInternalGatewayId;
    private String gatewayId;
    private GatewayApprovalStatus gatewayApprovalStatus;
    private String gatewayName;
    private String domain;
    private String emailAddress;
    private String gatewayAcronym;
    private String gatewayURL;
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

    public Gateway() {}

    public String getAiravataInternalGatewayId() {
        return airavataInternalGatewayId;
    }

    public void setAiravataInternalGatewayId(String airavataInternalGatewayId) {
        this.airavataInternalGatewayId = airavataInternalGatewayId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public GatewayApprovalStatus getGatewayApprovalStatus() {
        return gatewayApprovalStatus;
    }

    public void setGatewayApprovalStatus(GatewayApprovalStatus gatewayApprovalStatus) {
        this.gatewayApprovalStatus = gatewayApprovalStatus;
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

    public String getGatewayAcronym() {
        return gatewayAcronym;
    }

    public void setGatewayAcronym(String gatewayAcronym) {
        this.gatewayAcronym = gatewayAcronym;
    }

    public String getGatewayURL() {
        return gatewayURL;
    }

    public void setGatewayURL(String gatewayURL) {
        this.gatewayURL = gatewayURL;
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

    public long getRequestCreationTime() {
        return requestCreationTime;
    }

    public void setRequestCreationTime(long requestCreationTime) {
        this.requestCreationTime = requestCreationTime;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public void setRequesterUsername(String requesterUsername) {
        this.requesterUsername = requesterUsername;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gateway that = (Gateway) o;
        return Objects.equals(airavataInternalGatewayId, that.airavataInternalGatewayId)
                && Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(gatewayApprovalStatus, that.gatewayApprovalStatus)
                && Objects.equals(gatewayName, that.gatewayName)
                && Objects.equals(domain, that.domain)
                && Objects.equals(emailAddress, that.emailAddress)
                && Objects.equals(gatewayAcronym, that.gatewayAcronym)
                && Objects.equals(gatewayURL, that.gatewayURL)
                && Objects.equals(gatewayPublicAbstract, that.gatewayPublicAbstract)
                && Objects.equals(reviewProposalDescription, that.reviewProposalDescription)
                && Objects.equals(gatewayAdminFirstName, that.gatewayAdminFirstName)
                && Objects.equals(gatewayAdminLastName, that.gatewayAdminLastName)
                && Objects.equals(gatewayAdminEmail, that.gatewayAdminEmail)
                && Objects.equals(identityServerUserName, that.identityServerUserName)
                && Objects.equals(identityServerPasswordToken, that.identityServerPasswordToken)
                && Objects.equals(declinedReason, that.declinedReason)
                && Objects.equals(oauthClientId, that.oauthClientId)
                && Objects.equals(oauthClientSecret, that.oauthClientSecret)
                && Objects.equals(requestCreationTime, that.requestCreationTime)
                && Objects.equals(requesterUsername, that.requesterUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                airavataInternalGatewayId,
                gatewayId,
                gatewayApprovalStatus,
                gatewayName,
                domain,
                emailAddress,
                gatewayAcronym,
                gatewayURL,
                gatewayPublicAbstract,
                reviewProposalDescription,
                gatewayAdminFirstName,
                gatewayAdminLastName,
                gatewayAdminEmail,
                identityServerUserName,
                identityServerPasswordToken,
                declinedReason,
                oauthClientId,
                oauthClientSecret,
                requestCreationTime,
                requesterUsername);
    }

    @Override
    public String toString() {
        return "Gateway{" + "airavataInternalGatewayId=" + airavataInternalGatewayId + ", gatewayId=" + gatewayId
                + ", gatewayApprovalStatus=" + gatewayApprovalStatus + ", gatewayName=" + gatewayName + ", domain="
                + domain + ", emailAddress=" + emailAddress + ", gatewayAcronym=" + gatewayAcronym + ", gatewayURL="
                + gatewayURL + ", gatewayPublicAbstract=" + gatewayPublicAbstract + ", reviewProposalDescription="
                + reviewProposalDescription + ", gatewayAdminFirstName=" + gatewayAdminFirstName
                + ", gatewayAdminLastName=" + gatewayAdminLastName + ", gatewayAdminEmail=" + gatewayAdminEmail
                + ", identityServerUserName=" + identityServerUserName + ", identityServerPasswordToken="
                + identityServerPasswordToken + ", declinedReason=" + declinedReason + ", oauthClientId="
                + oauthClientId + ", oauthClientSecret=" + oauthClientSecret + ", requestCreationTime="
                + requestCreationTime + ", requesterUsername=" + requesterUsername + "}";
    }
}
