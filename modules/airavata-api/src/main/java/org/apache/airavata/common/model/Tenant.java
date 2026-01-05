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
 * Domain model: Tenant
 */
public class Tenant {
    private String tenantId;
    private TenantApprovalStatus tenantApprovalStatus;
    private String tenantName;
    private String domain;
    private String emailAddress;
    private String tenantAcronym;
    private String tenantURL;
    private String tenantPublicAbstract;
    private String reviewProposalDescription;
    private String declinedReason;
    private long requestCreationTime;
    private String requesterUsername;

    public Tenant() {}

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public TenantApprovalStatus getTenantApprovalStatus() {
        return tenantApprovalStatus;
    }

    public void setTenantApprovalStatus(TenantApprovalStatus tenantApprovalStatus) {
        this.tenantApprovalStatus = tenantApprovalStatus;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
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

    public String getTenantAcronym() {
        return tenantAcronym;
    }

    public void setTenantAcronym(String tenantAcronym) {
        this.tenantAcronym = tenantAcronym;
    }

    public String getTenantURL() {
        return tenantURL;
    }

    public void setTenantURL(String tenantURL) {
        this.tenantURL = tenantURL;
    }

    public String getTenantPublicAbstract() {
        return tenantPublicAbstract;
    }

    public void setTenantPublicAbstract(String tenantPublicAbstract) {
        this.tenantPublicAbstract = tenantPublicAbstract;
    }

    public String getReviewProposalDescription() {
        return reviewProposalDescription;
    }

    public void setReviewProposalDescription(String reviewProposalDescription) {
        this.reviewProposalDescription = reviewProposalDescription;
    }

    public String getDeclinedReason() {
        return declinedReason;
    }

    public void setDeclinedReason(String declinedReason) {
        this.declinedReason = declinedReason;
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
        Tenant that = (Tenant) o;
        return Objects.equals(tenantId, that.tenantId)
                && Objects.equals(tenantApprovalStatus, that.tenantApprovalStatus)
                && Objects.equals(tenantName, that.tenantName)
                && Objects.equals(domain, that.domain)
                && Objects.equals(emailAddress, that.emailAddress)
                && Objects.equals(tenantAcronym, that.tenantAcronym)
                && Objects.equals(tenantURL, that.tenantURL)
                && Objects.equals(tenantPublicAbstract, that.tenantPublicAbstract)
                && Objects.equals(reviewProposalDescription, that.reviewProposalDescription)
                && Objects.equals(declinedReason, that.declinedReason)
                && Objects.equals(requestCreationTime, that.requestCreationTime)
                && Objects.equals(requesterUsername, that.requesterUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                tenantId,
                tenantApprovalStatus,
                tenantName,
                domain,
                emailAddress,
                tenantAcronym,
                tenantURL,
                tenantPublicAbstract,
                reviewProposalDescription,
                declinedReason,
                requestCreationTime,
                requesterUsername);
    }

    @Override
    public String toString() {
        return "Tenant{" + "tenantId=" + tenantId + ", tenantApprovalStatus=" + tenantApprovalStatus + ", tenantName="
                + tenantName + ", domain=" + domain + ", emailAddress=" + emailAddress + ", tenantAcronym="
                + tenantAcronym + ", tenantURL=" + tenantURL + ", tenantPublicAbstract=" + tenantPublicAbstract
                + ", reviewProposalDescription=" + reviewProposalDescription + ", declinedReason=" + declinedReason
                + ", requestCreationTime=" + requestCreationTime + ", requesterUsername=" + requesterUsername + "}";
    }
}
