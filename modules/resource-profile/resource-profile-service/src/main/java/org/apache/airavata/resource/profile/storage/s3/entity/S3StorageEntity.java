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

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "S3_STORAGE_ENTITY")
public class S3StorageEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "S3_STORAGE_ID")
    private String s3StorageId;

    @Column(name = "REGION")
    private String region;

    @Column(name = "BUCKET_NAME")
    private String bucketName;

    public String getS3StorageId() {
        return s3StorageId;
    }

    public S3StorageEntity setS3StorageId(String s3StorageId) {
        this.s3StorageId = s3StorageId;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public S3StorageEntity setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getBucketName() {
        return bucketName;
    }

    public S3StorageEntity setBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }
}

