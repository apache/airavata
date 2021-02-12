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

import java.io.Serializable;
import java.util.Objects;

public class S3StoGroupPreferenceId implements Serializable {
    private String s3StorageId;
    private String s3GroupResourceProfileId;

    public S3StoGroupPreferenceId(String s3StorageId, String s3GroupResourceProfileId) {
        this.s3StorageId = s3StorageId;
        this.s3GroupResourceProfileId = s3GroupResourceProfileId;
    }

    public String getS3StorageId() {
        return s3StorageId;
    }

    public S3StoGroupPreferenceId setS3StorageId(String s3StorageId) {
        this.s3StorageId = s3StorageId;
        return this;
    }

    public String getS3GroupResourceProfileId() {
        return s3GroupResourceProfileId;
    }

    public S3StoGroupPreferenceId setS3GroupResourceProfileId(String s3GroupResourceProfileId) {
        this.s3GroupResourceProfileId = s3GroupResourceProfileId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof S3StoGroupPreferenceId)) return false;
        S3StoGroupPreferenceId that = (S3StoGroupPreferenceId) o;
        return Objects.equals(getS3StorageId(), that.getS3StorageId()) &&
                Objects.equals(getS3GroupResourceProfileId(), that.getS3GroupResourceProfileId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getS3StorageId(), getS3GroupResourceProfileId());
    }
}
