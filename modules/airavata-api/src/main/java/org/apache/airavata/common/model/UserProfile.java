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

import java.util.List;
import java.util.Objects;

/**
 * Domain model: minimal user identity for research context.
 * Extended user info (demographics, dashboard prefs, contact details) lives in the identity provider (Keycloak).
 * This model and AIRAVATA_USER store only what is needed to associate users with gateways and resource profiles.
 */
public class UserProfile {
    private String userModelVersion;
    private String airavataInternalUserId;
    private String userId;
    private String gatewayId;
    private List<String> emails;
    private String firstName;
    private String lastName;
    private String middleName;
    private String namePrefix;
    private String nameSuffix;
    private String orcidId;
    private List<String> phones;
    private String country;
    private List<String> nationality;
    private String homeOrganization;
    private String orginationAffiliation;
    private long creationTime;
    private long lastAccessTime;
    private long validUntil;
    private Status State;
    private String comments;
    private List<String> labeledURI;
    private String gpgKey;
    private String timeZone;
    private NSFDemographics nsfDemographics;
    private CustomDashboard customDashboard;

    public UserProfile() {}

    public String getUserModelVersion() {
        return userModelVersion;
    }

    public void setUserModelVersion(String userModelVersion) {
        this.userModelVersion = userModelVersion;
    }

    public String getAiravataInternalUserId() {
        return airavataInternalUserId;
    }

    public void setAiravataInternalUserId(String airavataInternalUserId) {
        this.airavataInternalUserId = airavataInternalUserId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public String getNameSuffix() {
        return nameSuffix;
    }

    public void setNameSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
    }

    public String getOrcidId() {
        return orcidId;
    }

    public void setOrcidId(String orcidId) {
        this.orcidId = orcidId;
    }

    public List<String> getPhones() {
        return phones;
    }

    public void setPhones(List<String> phones) {
        this.phones = phones;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<String> getNationality() {
        return nationality;
    }

    public void setNationality(List<String> nationality) {
        this.nationality = nationality;
    }

    public String getHomeOrganization() {
        return homeOrganization;
    }

    public void setHomeOrganization(String homeOrganization) {
        this.homeOrganization = homeOrganization;
    }

    public String getOrginationAffiliation() {
        return orginationAffiliation;
    }

    public void setOrginationAffiliation(String orginationAffiliation) {
        this.orginationAffiliation = orginationAffiliation;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public long getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(long validUntil) {
        this.validUntil = validUntil;
    }

    public Status getState() {
        return State;
    }

    public void setState(Status State) {
        this.State = State;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<String> getLabeledURI() {
        return labeledURI;
    }

    public void setLabeledURI(List<String> labeledURI) {
        this.labeledURI = labeledURI;
    }

    public String getGpgKey() {
        return gpgKey;
    }

    public void setGpgKey(String gpgKey) {
        this.gpgKey = gpgKey;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfile that = (UserProfile) o;
        return Objects.equals(userModelVersion, that.userModelVersion)
                && Objects.equals(airavataInternalUserId, that.airavataInternalUserId)
                && Objects.equals(userId, that.userId)
                && Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(emails, that.emails)
                && Objects.equals(firstName, that.firstName)
                && Objects.equals(lastName, that.lastName)
                && Objects.equals(middleName, that.middleName)
                && Objects.equals(namePrefix, that.namePrefix)
                && Objects.equals(nameSuffix, that.nameSuffix)
                && Objects.equals(orcidId, that.orcidId)
                && Objects.equals(phones, that.phones)
                && Objects.equals(country, that.country)
                && Objects.equals(nationality, that.nationality)
                && Objects.equals(homeOrganization, that.homeOrganization)
                && Objects.equals(orginationAffiliation, that.orginationAffiliation)
                && Objects.equals(creationTime, that.creationTime)
                && Objects.equals(lastAccessTime, that.lastAccessTime)
                && Objects.equals(validUntil, that.validUntil)
                && Objects.equals(State, that.State)
                && Objects.equals(comments, that.comments)
                && Objects.equals(labeledURI, that.labeledURI)
                && Objects.equals(gpgKey, that.gpgKey)
                && Objects.equals(timeZone, that.timeZone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                userModelVersion,
                airavataInternalUserId,
                userId,
                gatewayId,
                emails,
                firstName,
                lastName,
                middleName,
                namePrefix,
                nameSuffix,
                orcidId,
                phones,
                country,
                nationality,
                homeOrganization,
                orginationAffiliation,
                creationTime,
                lastAccessTime,
                validUntil,
                State,
                comments,
                labeledURI,
                gpgKey,
                timeZone,
                nsfDemographics,
                customDashboard);
    }

    @Override
    public String toString() {
        return "UserProfile{" + "userModelVersion=" + userModelVersion + ", airavataInternalUserId="
                + airavataInternalUserId + ", userId=" + userId + ", gatewayId=" + gatewayId + ", emails=" + emails
                + ", firstName=" + firstName + ", lastName=" + lastName + ", middleName=" + middleName + ", namePrefix="
                + namePrefix + ", nameSuffix=" + nameSuffix + ", orcidId=" + orcidId + ", phones=" + phones
                + ", country=" + country + ", nationality=" + nationality + ", homeOrganization=" + homeOrganization
                + ", orginationAffiliation=" + orginationAffiliation + ", creationTime=" + creationTime
                + ", lastAccessTime=" + lastAccessTime + ", validUntil=" + validUntil + ", State=" + State
                + ", comments=" + comments + ", labeledURI=" + labeledURI + ", gpgKey=" + gpgKey + ", timeZone="
                + timeZone + "}";
    }
}
