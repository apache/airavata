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
package org.apache.airavata.service.profile.commons.user.entities;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="USER_PROFILE")
public class UserProfileEntity {
    private String airavataInternalUserId;
    private String userId;
    private String gatewayId;
    private String userModelVersion;
    private String firstName;
    private String lastName;
    private String middleName;
    private String namePrefix;
    private String nameSuffix;
    private String orcidId;
    private String country;
    private String homeOrganization;
    private String orginationAffiliation;
    private Date creationTime;
    private Date lastAccessTime;
    private Date validUntil;
    private String state;
    private String comments;
    private List<String> labeledURI;
    private String gpgKey;
    private String timeZone;

    private List<String> nationality;
    private List<String> emails;
    private List<String> phones;
    private NSFDemographicsEntity nsfDemographics;
    private CustomizedDashboardEntity customizedDashboardEntity;

    @Id
    @Column(name = "AIRAVATA_INTERNAL_USER_ID")
    public String getAiravataInternalUserId() {
        return airavataInternalUserId;
    }

    public void setAiravataInternalUserId(String id) {
        this.airavataInternalUserId = id;
    }

    @Column(name = "USER_ID")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Column(name = "GATEWAY_ID")
    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Column(name = "USER_MODEL_VERSION")
    public String getUserModelVersion() {
        return userModelVersion;
    }

    public void setUserModelVersion(String userModelVersion) {
        this.userModelVersion = userModelVersion;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="USER_PROFILE_EMAIL", joinColumns = @JoinColumn(name="AIRAVATA_INTERNAL_USER_ID"))
    @Column(name = "EMAIL")
    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    @Column(name = "FIRST_NAME")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Column(name = "LAST_NAME")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Column(name = "MIDDLE_NAME")
    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    @Column(name = "NAME_PREFIX")
    public String getNamePrefix() {
        return namePrefix;
    }

    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    @Column(name = "NAME_SUFFIX")
    public String getNameSuffix() {
        return nameSuffix;
    }

    public void setNameSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
    }

    @Column(name = "ORCID_ID")
    public String getOrcidId() {
        return orcidId;
    }

    public void setOrcidId(String orcidId) {
        this.orcidId = orcidId;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="USER_PROFILE_PHONE", joinColumns = @JoinColumn(name="AIRAVATA_INTERNAL_USER_ID"))
    @Column(name = "PHONE")
    public List<String> getPhones() {
        return phones;
    }

    public void setPhones(List<String> phones) {
        this.phones = phones;
    }

    @Column(name = "COUNTRY")
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="USER_PROFILE_NATIONALITY", joinColumns = @JoinColumn(name="AIRAVATA_INTERNAL_USER_ID"))
    @Column(name = "NATIONALITY")
    public List<String> getNationality() {
        return nationality;
    }

    public void setNationality(List<String> nationality) {
        this.nationality = nationality;
    }

    @Column(name = "HOME_ORGANIZATION")
    public String getHomeOrganization() {
        return homeOrganization;
    }

    public void setHomeOrganization(String homeOrganization) {
        this.homeOrganization = homeOrganization;
    }

    @Column(name = "ORIGINATION_AFFILIATION")
    public String getOrginationAffiliation() {
        return orginationAffiliation;
    }

    public void setOrginationAffiliation(String orginationAffiliation) {
        this.orginationAffiliation = orginationAffiliation;
    }

    @Column(name="CREATION_TIME")
    public Date getCreationTime() {
        return creationTime;
    }

    private void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @Column(name = "LAST_ACCESS_TIME")
    public Date getLastAccessTime() {
        return lastAccessTime;
    }

    private void setLastAccessTime(Date lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    @Column(name = "VALID_UNTIL")
    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    @Column(name = "STATE")
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Lob
    @Column(name = "COMMENTS")
    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="USER_PROFILE_LABELED_URI", joinColumns = @JoinColumn(name="AIRAVATA_INTERNAL_USER_ID"))
    @Column(name = "LABELED_URI")
    public List<String> getLabeledURI() {
        return labeledURI;
    }

    public void setLabeledURI(List<String> labeledURI) {
        this.labeledURI = labeledURI;
    }

    @Lob
    @Column(name = "GPG_KEY")
    public String getGpgKey() {
        return gpgKey;
    }

    public void setGpgKey(String gpgKey) {
        this.gpgKey = gpgKey;
    }

    @Column(name = "TIME_ZONE")
    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @OneToOne(targetEntity = NSFDemographicsEntity.class, cascade = CascadeType.ALL,
            mappedBy = "userProfile", fetch = FetchType.EAGER)
    public NSFDemographicsEntity getNsfDemographics() {
        return nsfDemographics;
    }

    public void setNsfDemographics(NSFDemographicsEntity nsfDemographics) {
        this.nsfDemographics = nsfDemographics;
    }

    @OneToOne(targetEntity = CustomizedDashboardEntity.class, cascade = CascadeType.ALL,
            mappedBy = "userProfileEntity", fetch = FetchType.EAGER)
    public CustomizedDashboardEntity getCustomizedDashboardEntity() {
        return customizedDashboardEntity;
    }

    public void setCustomizedDashboardEntity(CustomizedDashboardEntity customizedDashboardEntity) {
        this.customizedDashboardEntity = customizedDashboardEntity;
    }

    @PrePersist
    void createdAt() {
        this.creationTime = this.lastAccessTime = new Date();
    }

    @PreUpdate
    void updatedAt() {
        this.lastAccessTime = new Date();
    }

    @Override
    public String toString() {
        return "UserProfileEntity{" +
                "airavataInternalUserId='" + airavataInternalUserId + '\'' +
                ", userId='" + userId + '\'' +
                ", gatewayId='" + gatewayId + '\'' +
                ", userModelVersion='" + userModelVersion + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", namePrefix='" + namePrefix + '\'' +
                ", nameSuffix='" + nameSuffix + '\'' +
                ", orcidId='" + orcidId + '\'' +
                ", country='" + country + '\'' +
                ", homeOrganization='" + homeOrganization + '\'' +
                ", orginationAffiliation='" + orginationAffiliation + '\'' +
                ", creationTime=" + creationTime +
                ", lastAccessTime=" + lastAccessTime +
                ", validUntil=" + validUntil +
                ", state='" + state + '\'' +
                ", comments='" + comments + '\'' +
                ", labeledURI=" + labeledURI +
                ", gpgKey='" + gpgKey + '\'' +
                ", timeZone='" + timeZone + '\'' +
                ", nationality=" + nationality +
                ", emails=" + emails +
                ", phones=" + phones +
                ", nsfDemographics=" + nsfDemographics +
                '}';
    }
}
