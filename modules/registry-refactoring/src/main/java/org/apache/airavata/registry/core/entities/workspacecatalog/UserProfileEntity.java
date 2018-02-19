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
package org.apache.airavata.registry.core.entities.workspacecatalog;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="WORKSPACE_USER_PROFILE")
public class UserProfileEntity {
    private String airavataInternalUserId;
    private String userId;
    private String gatewayId;
    private String userModelVersion;
    private String userName;
    private String orcidId;
    private String country;
    private String homeOrganization;
    private String orginationAffiliation;
    private long creationTime;
    private long lastAccessTime;
    private long validUntil;
    private String state;
    private String comments;
    private List<String> labeledURI;
    private String gpgKey;
    private String timeZone;

    private List<String> nationality;
    private List<String> emails;
    private List<String> phones;
    private NSFDemographicsEntity nsfDemographics;

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

    @ElementCollection
    @CollectionTable(name="USER_PROFILE_EMAIL", joinColumns = @JoinColumn(name="AIRAVATA_INTERNAL_USER_ID"))
    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    @Column(name = "USER_NAME")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "ORCID_ID")
    public String getOrcidId() {
        return orcidId;
    }

    public void setOrcidId(String orcidId) {
        this.orcidId = orcidId;
    }

    @ElementCollection
    @CollectionTable(name="USER_PROFILE_PHONE", joinColumns = @JoinColumn(name="AIRAVATA_INTERNAL_USER_ID"))
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

    @ElementCollection
    @CollectionTable(name="USER_PROFILE_NATIONALITY", joinColumns = @JoinColumn(name="AIRAVATA_INTERNAL_USER_ID"))
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
    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    @Column(name = "LAST_ACCESS_TIME")
    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    @Column(name = "VALID_UNTIL")
    public long getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(long validUntil) {
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

    @ElementCollection
    @CollectionTable(name="USER_PROFILE_LABELED_URI", joinColumns = @JoinColumn(name="AIRAVATA_INTERNAL_USER_ID"))
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

    @OneToOne(targetEntity = NSFDemographicsEntity.class, cascade = CascadeType.ALL, mappedBy = "userProfile")
    public NSFDemographicsEntity getNsfDemographics() {
        return nsfDemographics;
    }

    public void setNsfDemographics(NSFDemographicsEntity nsfDemographics) {
        this.nsfDemographics = nsfDemographics;
    }
}