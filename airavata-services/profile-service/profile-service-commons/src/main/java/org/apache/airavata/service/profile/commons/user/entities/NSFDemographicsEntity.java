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
import java.util.List;

@Entity
@Table(name = "NSF_DEMOGRAPHIC")
public class NSFDemographicsEntity {
    private String airavataInternalUserId;
    private String gender;
    private List<String> ethnicities;
    private List<String> races;
    private List<String> disabilities;
    private UserProfileEntity userProfile;

    @Id
    @Column(name = "AIRAVATA_INTERNAL_USER_ID")
    public String getAiravataInternalUserId() {
        return airavataInternalUserId;
    }

    public void setAiravataInternalUserId(String userId) {
        this.airavataInternalUserId = userId;
    }

    @Column(name = "GENDER")
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @ElementCollection
    @CollectionTable(name="NSF_DEMOGRAPHIC_ETHNICITY", joinColumns = @JoinColumn(name="AIRAVATA_INTERNAL_USER_ID"))
    @Column(name = "ETHNICITY")
    public List<String> getEthnicities() {
        return ethnicities;
    }

    public void setEthnicities(List<String> ethnicities) {
        this.ethnicities = ethnicities;
    }

    @ElementCollection
    @CollectionTable(name="NSF_DEMOGRAPHIC_RACE", joinColumns = @JoinColumn(name="AIRAVATA_INTERNAL_USER_ID"))
    @Column(name = "RACE")
    public List<String> getRaces() {
        return races;
    }

    public void setRaces(List<String> races) {
        this.races = races;
    }

    @ElementCollection
    @CollectionTable(name="NSF_DEMOGRAPHIC_DISABILITY", joinColumns = @JoinColumn(name="AIRAVATA_INTERNAL_USER_ID"))
    @Column(name = "DISABILITY")
    public List<String> getDisabilities() {
        return disabilities;
    }

    public void setDisabilities(List<String> disabilities) {
        this.disabilities = disabilities;
    }

    @OneToOne(targetEntity = UserProfileEntity.class, cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn(name = "AIRAVATA_INTERNAL_USER_ID", referencedColumnName = "AIRAVATA_INTERNAL_USER_ID")
    public UserProfileEntity getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfileEntity userProfile) {
        this.userProfile = userProfile;
    }

    @Override
    public String toString() {
        return "NSFDemographicsEntity{" +
                "airavataInternalUserId='" + airavataInternalUserId + '\'' +
                ", gender='" + gender + '\'' +
                ", ethnicities=" + ethnicities +
                ", races=" + races +
                ", disabilities=" + disabilities +
                '}';
    }
}
