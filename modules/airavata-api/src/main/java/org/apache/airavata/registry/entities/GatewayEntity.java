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
package org.apache.airavata.registry.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;
import org.apache.airavata.common.model.GatewayApprovalStatus;
import org.apache.airavata.common.utils.AiravataUtils;

/**
 * Unified GatewayEntity that combines fields from ProfileGatewayEntity, expcatalog GatewayEntity,
 * and DomainEntity into a single entity. This entity serves as the single source of truth for
 * gateway data in the system, including sharing domain functionality.
 *
 * <p>The entity uses airavataInternalGatewayId as the primary key (UUID) and maintains
 * gatewayId as a unique indexed field for backward compatibility with systems that
 * reference gateways by their human-readable gatewayId.
 *
 * <p><strong>Sharing Domain:</strong>
 * The gatewayId serves as the domainId for the sharing registry. All sharing entities
 * (UserGroup, Entity, EntityType, PermissionType, GroupMembership, Sharing, GroupAdmin)
 * reference this gateway via its gatewayId for domain-scoped operations.
 *
 * <p>Domain-specific fields (source of truth for sharing domain metadata; used by DomainMapper and SharingRegistryService):
 * <ul>
 *   <li>{@code domainDescription} - Description for the sharing domain</li>
 *   <li>{@code domainCreatedTime} - When the sharing domain was created (Unix timestamp)</li>
 *   <li>{@code lastUpdatedTime} - When the domain was last updated (Unix timestamp)</li>
 *   <li>{@code initialUserGroupId} - Default group for new users in this domain</li>
 * </ul>
 *
 * @see org.apache.airavata.service.AiravataService#addGateway
 * @see org.apache.airavata.service.SharingRegistryService
 */
@Entity(name = "GatewayEntity")
@Table(
        name = "GATEWAY",
        indexes = {@Index(name = "idx_gateway_id", columnList = "GATEWAY_ID", unique = true)})
public class GatewayEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "AIRAVATA_INTERNAL_GATEWAY_ID", nullable = false)
    private String airavataInternalGatewayId;

    @Column(name = "GATEWAY_ID", unique = true)
    private String gatewayId;

    @Column(name = "GATEWAY_NAME")
    private String gatewayName;

    @Column(name = "GATEWAY_DOMAIN")
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

    @Column(name = "DECLINED_REASON")
    private String declinedReason;

    @Column(name = "REQUEST_CREATION_TIME")
    private Timestamp requestCreationTime;

    @Column(name = "REQUESTER_USERNAME")
    private String requesterUsername;

    // ========== Domain-related fields (consolidated from DomainEntity) ==========
    // These fields were merged from the former DomainEntity to eliminate redundancy.
    // The gatewayId serves as the domainId for sharing registry operations.

    /**
     * Description for the sharing domain associated with this gateway.
     * Used for administrative and informational purposes.
     */
    @Column(name = "DOMAIN_DESCRIPTION")
    private String domainDescription;

    /**
     * Timestamp when the sharing domain was created (Unix timestamp in milliseconds).
     * This may differ from requestCreationTime if the domain was created after the gateway.
     */
    @Column(name = "DOMAIN_CREATED_TIME")
    private Long domainCreatedTime;

    /**
     * Timestamp when the gateway/domain was last updated (Unix timestamp in milliseconds).
     */
    @Column(name = "LAST_UPDATED_TIME")
    private Long lastUpdatedTime;

    /**
     * The initial user group ID for this gateway's sharing domain.
     * New users added to this gateway will automatically be added to this group.
     */
    @Column(name = "INITIAL_USER_GROUP_ID")
    private String initialUserGroupId;

    public GatewayEntity() {
        this.airavataInternalGatewayId = UUID.randomUUID().toString();
    }

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

    public String getDeclinedReason() {
        return declinedReason;
    }

    public void setDeclinedReason(String declinedReason) {
        this.declinedReason = declinedReason;
    }

    public Timestamp getRequestCreationTime() {
        return requestCreationTime;
    }

    public void setRequestCreationTime(Timestamp requestCreationTime) {
        this.requestCreationTime = requestCreationTime;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public void setRequesterUsername(String requesterUsername) {
        this.requesterUsername = requesterUsername;
    }

    /**
     * Returns the description for this gateway's sharing domain.
     *
     * @return the domain description, or null if not set
     */
    public String getDomainDescription() {
        return domainDescription;
    }

    /**
     * Sets the description for this gateway's sharing domain.
     *
     * @param domainDescription the domain description
     */
    public void setDomainDescription(String domainDescription) {
        this.domainDescription = domainDescription;
    }

    /**
     * Returns the creation time for this gateway's sharing domain.
     *
     * @return the domain creation time as a Unix timestamp in milliseconds, or null if not set
     */
    public Long getDomainCreatedTime() {
        return domainCreatedTime;
    }

    /**
     * Sets the creation time for this gateway's sharing domain.
     *
     * @param domainCreatedTime the domain creation time as a Unix timestamp in milliseconds
     */
    public void setDomainCreatedTime(Long domainCreatedTime) {
        this.domainCreatedTime = domainCreatedTime;
    }

    /**
     * Returns the last updated time for this gateway.
     *
     * @return the last updated time as a Unix timestamp in milliseconds, or null if not set
     */
    public Long getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    /**
     * Sets the last updated time for this gateway.
     *
     * @param lastUpdatedTime the last updated time as a Unix timestamp in milliseconds
     */
    public void setLastUpdatedTime(Long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    /**
     * Returns the initial user group ID for this gateway's sharing domain.
     * New users added to this gateway will automatically be added to this group.
     *
     * @return the initial user group ID, or null if not set
     */
    public String getInitialUserGroupId() {
        return initialUserGroupId;
    }

    /**
     * Sets the initial user group ID for this gateway's sharing domain.
     *
     * @param initialUserGroupId the initial user group ID
     */
    public void setInitialUserGroupId(String initialUserGroupId) {
        this.initialUserGroupId = initialUserGroupId;
    }

    @PrePersist
    void createdAt() {
        if (this.requestCreationTime == null) {
            this.requestCreationTime = AiravataUtils.getUniqueTimestamp();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        GatewayEntity that = (GatewayEntity) obj;
        return Objects.equals(airavataInternalGatewayId, that.airavataInternalGatewayId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(airavataInternalGatewayId);
    }

    @Override
    public String toString() {
        return "GatewayEntity{"
                + "airavataInternalGatewayId='"
                + airavataInternalGatewayId
                + '\''
                + ", gatewayId='"
                + gatewayId
                + '\''
                + ", gatewayName='"
                + gatewayName
                + '\''
                + ", domain='"
                + domain
                + '\''
                + ", emailAddress='"
                + emailAddress
                + '\''
                + ", gatewayApprovalStatus="
                + gatewayApprovalStatus
                + ", gatewayAcronym='"
                + gatewayAcronym
                + '\''
                + ", gatewayUrl='"
                + gatewayUrl
                + '\''
                + ", gatewayPublicAbstract='"
                + gatewayPublicAbstract
                + '\''
                + ", reviewProposalDescription='"
                + reviewProposalDescription
                + '\''
                + ", gatewayAdminFirstName='"
                + gatewayAdminFirstName
                + '\''
                + ", gatewayAdminLastName='"
                + gatewayAdminLastName
                + '\''
                + ", gatewayAdminEmail='"
                + gatewayAdminEmail
                + '\''
                + ", declinedReason='"
                + declinedReason
                + '\''
                + ", requestCreationTime="
                + requestCreationTime
                + ", requesterUsername='"
                + requesterUsername
                + '\''
                + ", domainDescription='"
                + domainDescription
                + '\''
                + ", domainCreatedTime="
                + domainCreatedTime
                + ", lastUpdatedTime="
                + lastUpdatedTime
                + ", initialUserGroupId='"
                + initialUserGroupId
                + '\''
                + '}';
    }
}
