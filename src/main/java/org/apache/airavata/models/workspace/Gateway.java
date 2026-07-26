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
package org.apache.airavata.models.workspace;

/**
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.workspace.proto.Gateway}.
 */
public record Gateway(
        String airavataInternalGatewayId,
        String gatewayId,
        GatewayApprovalStatus gatewayApprovalStatus,
        String gatewayName,
        String domain,
        String emailAddress,
        String gatewayAcronym,
        String gatewayUrl,
        String gatewayPublicAbstract,
        String reviewProposalDescription,
        String gatewayAdminFirstName,
        String gatewayAdminLastName,
        String gatewayAdminEmail,
        String identityServerUserName,
        String identityServerPasswordToken,
        String declinedReason,
        String oauthClientId,
        String oauthClientSecret,
        long requestCreationTime,
        String requesterUsername) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String airavataInternalGatewayId = "";
        private String gatewayId = "";
        private GatewayApprovalStatus gatewayApprovalStatus = GatewayApprovalStatus.GATEWAY_APPROVAL_STATUS_UNKNOWN;
        private String gatewayName = "";
        private String domain = "";
        private String emailAddress = "";
        private String gatewayAcronym = "";
        private String gatewayUrl = "";
        private String gatewayPublicAbstract = "";
        private String reviewProposalDescription = "";
        private String gatewayAdminFirstName = "";
        private String gatewayAdminLastName = "";
        private String gatewayAdminEmail = "";
        private String identityServerUserName = "";
        private String identityServerPasswordToken = "";
        private String declinedReason = "";
        private String oauthClientId = "";
        private String oauthClientSecret = "";
        private long requestCreationTime;
        private String requesterUsername = "";

        private Builder() {}

        private Builder(Gateway source) {
            this.airavataInternalGatewayId = source.airavataInternalGatewayId;
            this.gatewayId = source.gatewayId;
            this.gatewayApprovalStatus = source.gatewayApprovalStatus;
            this.gatewayName = source.gatewayName;
            this.domain = source.domain;
            this.emailAddress = source.emailAddress;
            this.gatewayAcronym = source.gatewayAcronym;
            this.gatewayUrl = source.gatewayUrl;
            this.gatewayPublicAbstract = source.gatewayPublicAbstract;
            this.reviewProposalDescription = source.reviewProposalDescription;
            this.gatewayAdminFirstName = source.gatewayAdminFirstName;
            this.gatewayAdminLastName = source.gatewayAdminLastName;
            this.gatewayAdminEmail = source.gatewayAdminEmail;
            this.identityServerUserName = source.identityServerUserName;
            this.identityServerPasswordToken = source.identityServerPasswordToken;
            this.declinedReason = source.declinedReason;
            this.oauthClientId = source.oauthClientId;
            this.oauthClientSecret = source.oauthClientSecret;
            this.requestCreationTime = source.requestCreationTime;
            this.requesterUsername = source.requesterUsername;
        }

        public Builder setAiravataInternalGatewayId(String airavataInternalGatewayId) {
            this.airavataInternalGatewayId = airavataInternalGatewayId;
            return this;
        }

        public Builder setGatewayId(String gatewayId) {
            this.gatewayId = gatewayId;
            return this;
        }

        public Builder setGatewayApprovalStatus(GatewayApprovalStatus gatewayApprovalStatus) {
            this.gatewayApprovalStatus = gatewayApprovalStatus;
            return this;
        }

        public Builder setGatewayName(String gatewayName) {
            this.gatewayName = gatewayName;
            return this;
        }

        public Builder setDomain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        public Builder setGatewayAcronym(String gatewayAcronym) {
            this.gatewayAcronym = gatewayAcronym;
            return this;
        }

        public Builder setGatewayUrl(String gatewayUrl) {
            this.gatewayUrl = gatewayUrl;
            return this;
        }

        public Builder setGatewayPublicAbstract(String gatewayPublicAbstract) {
            this.gatewayPublicAbstract = gatewayPublicAbstract;
            return this;
        }

        public Builder setReviewProposalDescription(String reviewProposalDescription) {
            this.reviewProposalDescription = reviewProposalDescription;
            return this;
        }

        public Builder setGatewayAdminFirstName(String gatewayAdminFirstName) {
            this.gatewayAdminFirstName = gatewayAdminFirstName;
            return this;
        }

        public Builder setGatewayAdminLastName(String gatewayAdminLastName) {
            this.gatewayAdminLastName = gatewayAdminLastName;
            return this;
        }

        public Builder setGatewayAdminEmail(String gatewayAdminEmail) {
            this.gatewayAdminEmail = gatewayAdminEmail;
            return this;
        }

        public Builder setIdentityServerUserName(String identityServerUserName) {
            this.identityServerUserName = identityServerUserName;
            return this;
        }

        public Builder setIdentityServerPasswordToken(String identityServerPasswordToken) {
            this.identityServerPasswordToken = identityServerPasswordToken;
            return this;
        }

        public Builder setDeclinedReason(String declinedReason) {
            this.declinedReason = declinedReason;
            return this;
        }

        public Builder setOauthClientId(String oauthClientId) {
            this.oauthClientId = oauthClientId;
            return this;
        }

        public Builder setOauthClientSecret(String oauthClientSecret) {
            this.oauthClientSecret = oauthClientSecret;
            return this;
        }

        public Builder setRequestCreationTime(long requestCreationTime) {
            this.requestCreationTime = requestCreationTime;
            return this;
        }

        public Builder setRequesterUsername(String requesterUsername) {
            this.requesterUsername = requesterUsername;
            return this;
        }

        public Gateway build() {
            return new Gateway(
                    airavataInternalGatewayId, gatewayId, gatewayApprovalStatus, gatewayName, domain,
                    emailAddress, gatewayAcronym, gatewayUrl, gatewayPublicAbstract, reviewProposalDescription,
                    gatewayAdminFirstName, gatewayAdminLastName, gatewayAdminEmail, identityServerUserName,
                    identityServerPasswordToken, declinedReason, oauthClientId, oauthClientSecret,
                    requestCreationTime, requesterUsername);
        }
    }
}
