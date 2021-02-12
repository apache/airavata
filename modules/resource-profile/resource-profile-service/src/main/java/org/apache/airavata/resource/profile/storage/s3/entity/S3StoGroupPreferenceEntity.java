/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.airavata.resource.profile.storage.s3.entity;

import javax.persistence.*;

@Entity
@Table(name = "S3_STORAGE_GROUP_PREFERENCE")
@IdClass(S3StoGroupPreferenceId.class)
public class S3StoGroupPreferenceEntity {

    @Id
    @Column(name = "S3_STORAGE_ID")
    private String s3StorageId;

    @Id
    @Column(name = "S3_GROUP_RESOURCE_PROFILE_ID")
    private String s3GroupResourceProfileId;

    @Column(name = "RESOURCE_CS_TOKEN")
    private String resourceSpecificCredentialToken;

    @ManyToOne(targetEntity = S3StoGroupResourceProfileEntity.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "S3_GROUP_RESOURCE_PROFILE_ID", insertable = false, updatable = false)
    private S3StoGroupResourceProfileEntity s3GroupResourceProfile;

    @ManyToOne(targetEntity = S3StorageEntity.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "S3_STORAGE_ID", insertable = false, updatable = false)
    private  S3StorageEntity s3Storage;

    public String getS3StorageId() {
        return s3StorageId;
    }

    public S3StoGroupPreferenceEntity setS3StorageId(String s3StorageId) {
        this.s3StorageId = s3StorageId;
        return this;
    }

    public String getS3GroupResourceProfileId() {
        return s3GroupResourceProfileId;
    }

    public S3StoGroupPreferenceEntity setS3GroupResourceProfileId(String s3GroupResourceProfileId) {
        this.s3GroupResourceProfileId = s3GroupResourceProfileId;
        return this;
    }

    public String getResourceSpecificCredentialToken() {
        return resourceSpecificCredentialToken;
    }

    public S3StoGroupPreferenceEntity setResourceSpecificCredentialToken(String resourceSpecificCredentialToken) {
        this.resourceSpecificCredentialToken = resourceSpecificCredentialToken;
        return this;
    }

    public S3StoGroupResourceProfileEntity getS3GroupResourceProfile() {
        return s3GroupResourceProfile;
    }

    public S3StoGroupPreferenceEntity setS3GroupResourceProfile(S3StoGroupResourceProfileEntity s3GroupResourceProfile) {
        this.s3GroupResourceProfile = s3GroupResourceProfile;
        return this;
    }

    public S3StorageEntity getS3Storage() {
        return s3Storage;
    }

    public S3StoGroupPreferenceEntity setS3Storage(S3StorageEntity s3Storage) {
        this.s3Storage = s3Storage;
        return this;
    }
}
