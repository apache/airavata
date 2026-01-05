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
 * Domain model: NSFDemographics
 */
public class NSFDemographics {
    private String airavataInternalUserId;
    private String gender;
    private USCitizenship usCitizenship;
    private List<ethnicity> ethnicities;
    private List<race> races;
    private List<disability> disabilities;

    public NSFDemographics() {}

    public String getAiravataInternalUserId() {
        return airavataInternalUserId;
    }

    public void setAiravataInternalUserId(String airavataInternalUserId) {
        this.airavataInternalUserId = airavataInternalUserId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public USCitizenship getUsCitizenship() {
        return usCitizenship;
    }

    public void setUsCitizenship(USCitizenship usCitizenship) {
        this.usCitizenship = usCitizenship;
    }

    public List<ethnicity> getEthnicities() {
        return ethnicities;
    }

    public void setEthnicities(List<ethnicity> ethnicities) {
        this.ethnicities = ethnicities;
    }

    public List<race> getRaces() {
        return races;
    }

    public void setRaces(List<race> races) {
        this.races = races;
    }

    public List<disability> getDisabilities() {
        return disabilities;
    }

    public void setDisabilities(List<disability> disabilities) {
        this.disabilities = disabilities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NSFDemographics that = (NSFDemographics) o;
        return Objects.equals(airavataInternalUserId, that.airavataInternalUserId)
                && Objects.equals(gender, that.gender)
                && Objects.equals(usCitizenship, that.usCitizenship)
                && Objects.equals(ethnicities, that.ethnicities)
                && Objects.equals(races, that.races)
                && Objects.equals(disabilities, that.disabilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(airavataInternalUserId, gender, usCitizenship, ethnicities, races, disabilities);
    }

    @Override
    public String toString() {
        return "NSFDemographics{" + "airavataInternalUserId=" + airavataInternalUserId + ", gender=" + gender
                + ", usCitizenship=" + usCitizenship + ", ethnicities=" + ethnicities + ", races=" + races
                + ", disabilities=" + disabilities + "}";
    }
}
