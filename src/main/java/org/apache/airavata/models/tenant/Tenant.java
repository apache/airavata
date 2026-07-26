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
package org.apache.airavata.models.tenant;

/**
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.tenant.proto.Tenant}.
 */
public record Tenant(
        String tenantId,
        TenantApprovalStatus tenantApprovalStatus,
        String tenantName,
        String domain,
        String emailAddress,
        String tenantAcronym,
        String tenantUrl,
        String tenantPublicAbstract,
        String reviewProposalDescription,
        String declinedReason,
        long requestCreationTime,
        String requesterUsername) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String tenantId = "";
        private TenantApprovalStatus tenantApprovalStatus = TenantApprovalStatus.TENANT_APPROVAL_STATUS_UNKNOWN;
        private String tenantName = "";
        private String domain = "";
        private String emailAddress = "";
        private String tenantAcronym = "";
        private String tenantUrl = "";
        private String tenantPublicAbstract = "";
        private String reviewProposalDescription = "";
        private String declinedReason = "";
        private long requestCreationTime;
        private String requesterUsername = "";

        private Builder() {}

        private Builder(Tenant source) {
            this.tenantId = source.tenantId;
            this.tenantApprovalStatus = source.tenantApprovalStatus;
            this.tenantName = source.tenantName;
            this.domain = source.domain;
            this.emailAddress = source.emailAddress;
            this.tenantAcronym = source.tenantAcronym;
            this.tenantUrl = source.tenantUrl;
            this.tenantPublicAbstract = source.tenantPublicAbstract;
            this.reviewProposalDescription = source.reviewProposalDescription;
            this.declinedReason = source.declinedReason;
            this.requestCreationTime = source.requestCreationTime;
            this.requesterUsername = source.requesterUsername;
        }

        public Builder setTenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder setTenantApprovalStatus(TenantApprovalStatus tenantApprovalStatus) {
            this.tenantApprovalStatus = tenantApprovalStatus;
            return this;
        }

        public Builder setTenantName(String tenantName) {
            this.tenantName = tenantName;
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

        public Builder setTenantAcronym(String tenantAcronym) {
            this.tenantAcronym = tenantAcronym;
            return this;
        }

        public Builder setTenantUrl(String tenantUrl) {
            this.tenantUrl = tenantUrl;
            return this;
        }

        public Builder setTenantPublicAbstract(String tenantPublicAbstract) {
            this.tenantPublicAbstract = tenantPublicAbstract;
            return this;
        }

        public Builder setReviewProposalDescription(String reviewProposalDescription) {
            this.reviewProposalDescription = reviewProposalDescription;
            return this;
        }

        public Builder setDeclinedReason(String declinedReason) {
            this.declinedReason = declinedReason;
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

        public Tenant build() {
            return new Tenant(
                    tenantId, tenantApprovalStatus, tenantName, domain, emailAddress, tenantAcronym, tenantUrl,
                    tenantPublicAbstract, reviewProposalDescription, declinedReason, requestCreationTime,
                    requesterUsername);
        }
    }
}
