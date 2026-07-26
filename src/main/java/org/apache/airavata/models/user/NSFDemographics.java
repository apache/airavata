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
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.user.proto.NSFDemographics}.
 */
public record NSFDemographics(
        String airavataInternalUserId,
        String gender,
        USCitizenship usCitizenship,
        List<Ethnicity> ethnicities,
        List<Race> races,
        List<Disability> disabilities) {

    public NSFDemographics {
        ethnicities = ethnicities == null ? List.of() : List.copyOf(ethnicities);
        races = races == null ? List.of() : List.copyOf(races);
        disabilities = disabilities == null ? List.of() : List.copyOf(disabilities);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String airavataInternalUserId = "";
        private String gender = "";
        private USCitizenship usCitizenship = USCitizenship.US_CITIZENSHIP_UNKNOWN;
        private List<Ethnicity> ethnicities = new ArrayList<>();
        private List<Race> races = new ArrayList<>();
        private List<Disability> disabilities = new ArrayList<>();

        private Builder() {}

        private Builder(NSFDemographics source) {
            this.airavataInternalUserId = source.airavataInternalUserId;
            this.gender = source.gender;
            this.usCitizenship = source.usCitizenship;
            this.ethnicities = new ArrayList<>(source.ethnicities);
            this.races = new ArrayList<>(source.races);
            this.disabilities = new ArrayList<>(source.disabilities);
        }

        public Builder setAiravataInternalUserId(String airavataInternalUserId) {
            this.airavataInternalUserId = airavataInternalUserId;
            return this;
        }

        public Builder setGender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder setUsCitizenship(USCitizenship usCitizenship) {
            this.usCitizenship = usCitizenship;
            return this;
        }

        public Builder addEthnicities(Ethnicity value) {
            this.ethnicities.add(value);
            return this;
        }

        public Builder addAllEthnicities(Iterable<Ethnicity> values) {
            values.forEach(this.ethnicities::add);
            return this;
        }

        public Builder clearEthnicities() {
            this.ethnicities.clear();
            return this;
        }

        public Builder addRaces(Race value) {
            this.races.add(value);
            return this;
        }

        public Builder addAllRaces(Iterable<Race> values) {
            values.forEach(this.races::add);
            return this;
        }

        public Builder clearRaces() {
            this.races.clear();
            return this;
        }

        public Builder addDisabilities(Disability value) {
            this.disabilities.add(value);
            return this;
        }

        public Builder addAllDisabilities(Iterable<Disability> values) {
            values.forEach(this.disabilities::add);
            return this;
        }

        public Builder clearDisabilities() {
            this.disabilities.clear();
            return this;
        }

        public NSFDemographics build() {
            return new NSFDemographics(
                    airavataInternalUserId, gender, usCitizenship, ethnicities, races, disabilities);
        }
    }
}
