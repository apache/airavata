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
package org.apache.airavata.models.user;

import java.util.ArrayList;
import java.util.List;

/**
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.user.proto.UserProfile}.
 */
public record UserProfile(
        String userModelVersion,
        String airavataInternalUserId,
        String userId,
        String gatewayId,
        List<String> emails,
        String firstName,
        String lastName,
        String middleName,
        String namePrefix,
        String nameSuffix,
        String orcidId,
        List<String> phones,
        String country,
        List<String> nationality,
        String homeOrganization,
        String originationAffiliation,
        long creationTime,
        long lastAccessTime,
        long validUntil,
        Status state,
        String comments,
        List<String> labeledUri,
        String gpgKey,
        String timeZone,
        NSFDemographics nsfDemographics,
        CustomDashboard customDashboard) {

    public UserProfile {
        emails = emails == null ? List.of() : List.copyOf(emails);
        phones = phones == null ? List.of() : List.copyOf(phones);
        nationality = nationality == null ? List.of() : List.copyOf(nationality);
        labeledUri = labeledUri == null ? List.of() : List.copyOf(labeledUri);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String userModelVersion = "";
        private String airavataInternalUserId = "";
        private String userId = "";
        private String gatewayId = "";
        private List<String> emails = new ArrayList<>();
        private String firstName = "";
        private String lastName = "";
        private String middleName = "";
        private String namePrefix = "";
        private String nameSuffix = "";
        private String orcidId = "";
        private List<String> phones = new ArrayList<>();
        private String country = "";
        private List<String> nationality = new ArrayList<>();
        private String homeOrganization = "";
        private String originationAffiliation = "";
        private long creationTime;
        private long lastAccessTime;
        private long validUntil;
        private Status state = Status.STATUS_UNKNOWN;
        private String comments = "";
        private List<String> labeledUri = new ArrayList<>();
        private String gpgKey = "";
        private String timeZone = "";
        private NSFDemographics nsfDemographics = NSFDemographics.newBuilder().build();
        private CustomDashboard customDashboard = CustomDashboard.newBuilder().build();

        private Builder() {}

        private Builder(UserProfile source) {
            this.userModelVersion = source.userModelVersion;
            this.airavataInternalUserId = source.airavataInternalUserId;
            this.userId = source.userId;
            this.gatewayId = source.gatewayId;
            this.emails = new ArrayList<>(source.emails);
            this.firstName = source.firstName;
            this.lastName = source.lastName;
            this.middleName = source.middleName;
            this.namePrefix = source.namePrefix;
            this.nameSuffix = source.nameSuffix;
            this.orcidId = source.orcidId;
            this.phones = new ArrayList<>(source.phones);
            this.country = source.country;
            this.nationality = new ArrayList<>(source.nationality);
            this.homeOrganization = source.homeOrganization;
            this.originationAffiliation = source.originationAffiliation;
            this.creationTime = source.creationTime;
            this.lastAccessTime = source.lastAccessTime;
            this.validUntil = source.validUntil;
            this.state = source.state;
            this.comments = source.comments;
            this.labeledUri = new ArrayList<>(source.labeledUri);
            this.gpgKey = source.gpgKey;
            this.timeZone = source.timeZone;
            this.nsfDemographics = source.nsfDemographics;
            this.customDashboard = source.customDashboard;
        }

        public Builder setUserModelVersion(String userModelVersion) {
            this.userModelVersion = userModelVersion;
            return this;
        }

        public Builder setAiravataInternalUserId(String airavataInternalUserId) {
            this.airavataInternalUserId = airavataInternalUserId;
            return this;
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder setGatewayId(String gatewayId) {
            this.gatewayId = gatewayId;
            return this;
        }

        public Builder addEmails(String value) {
            this.emails.add(value);
            return this;
        }

        public Builder addAllEmails(Iterable<String> values) {
            values.forEach(this.emails::add);
            return this;
        }

        public Builder clearEmails() {
            this.emails.clear();
            return this;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setMiddleName(String middleName) {
            this.middleName = middleName;
            return this;
        }

        public Builder setNamePrefix(String namePrefix) {
            this.namePrefix = namePrefix;
            return this;
        }

        public Builder setNameSuffix(String nameSuffix) {
            this.nameSuffix = nameSuffix;
            return this;
        }

        public Builder setOrcidId(String orcidId) {
            this.orcidId = orcidId;
            return this;
        }

        public Builder addPhones(String value) {
            this.phones.add(value);
            return this;
        }

        public Builder addAllPhones(Iterable<String> values) {
            values.forEach(this.phones::add);
            return this;
        }

        public Builder clearPhones() {
            this.phones.clear();
            return this;
        }

        public Builder setCountry(String country) {
            this.country = country;
            return this;
        }

        public Builder addNationality(String value) {
            this.nationality.add(value);
            return this;
        }

        public Builder addAllNationality(Iterable<String> values) {
            values.forEach(this.nationality::add);
            return this;
        }

        public Builder clearNationality() {
            this.nationality.clear();
            return this;
        }

        public Builder setHomeOrganization(String homeOrganization) {
            this.homeOrganization = homeOrganization;
            return this;
        }

        public Builder setOriginationAffiliation(String originationAffiliation) {
            this.originationAffiliation = originationAffiliation;
            return this;
        }

        public Builder setCreationTime(long creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder setLastAccessTime(long lastAccessTime) {
            this.lastAccessTime = lastAccessTime;
            return this;
        }

        public Builder setValidUntil(long validUntil) {
            this.validUntil = validUntil;
            return this;
        }

        public Builder setState(Status state) {
            this.state = state;
            return this;
        }

        public Builder setComments(String comments) {
            this.comments = comments;
            return this;
        }

        public Builder addLabeledUri(String value) {
            this.labeledUri.add(value);
            return this;
        }

        public Builder addAllLabeledUri(Iterable<String> values) {
            values.forEach(this.labeledUri::add);
            return this;
        }

        public Builder clearLabeledUri() {
            this.labeledUri.clear();
            return this;
        }

        public Builder setGpgKey(String gpgKey) {
            this.gpgKey = gpgKey;
            return this;
        }

        public Builder setTimeZone(String timeZone) {
            this.timeZone = timeZone;
            return this;
        }

        public Builder setNsfDemographics(NSFDemographics nsfDemographics) {
            this.nsfDemographics = nsfDemographics;
            return this;
        }

        public Builder setCustomDashboard(CustomDashboard customDashboard) {
            this.customDashboard = customDashboard;
            return this;
        }

        public UserProfile build() {
            return new UserProfile(
                    userModelVersion, airavataInternalUserId, userId, gatewayId, emails, firstName, lastName,
                    middleName, namePrefix, nameSuffix, orcidId, phones, country, nationality, homeOrganization,
                    originationAffiliation, creationTime, lastAccessTime, validUntil, state, comments, labeledUri,
                    gpgKey, timeZone, nsfDemographics, customDashboard);
        }
    }
}
