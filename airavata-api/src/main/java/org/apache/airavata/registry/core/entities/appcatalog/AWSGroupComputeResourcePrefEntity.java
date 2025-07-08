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
package org.apache.airavata.registry.core.entities.appcatalog;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.PrimaryKeyJoinColumns;
import jakarta.persistence.Table;

/**
 * The persistent class for the aws_group_compute_resource_preference database table.
 */
@Entity
@DiscriminatorValue("AWS")
@Table(name = "AWS_GROUP_COMPUTE_RESOURCE_PREFERENCE")
@PrimaryKeyJoinColumns({
    @PrimaryKeyJoinColumn(name = "RESOURCE_ID", referencedColumnName = "RESOURCE_ID"),
    @PrimaryKeyJoinColumn(name = "GROUP_RESOURCE_PROFILE_ID", referencedColumnName = "GROUP_RESOURCE_PROFILE_ID")
})
public class AWSGroupComputeResourcePrefEntity extends GroupComputeResourcePrefEntity {

    @Column(name = "AWS_REGION", nullable = false)
    private String region;

    @Column(name = "PREFERRED_AMI_ID", nullable = false)
    private String preferredAmiId;

    @Column(name = "PREFERRED_INSTANCE_TYPE", nullable = false)
    private String preferredInstanceType;

    public AWSGroupComputeResourcePrefEntity() {}

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPreferredAmiId() {
        return preferredAmiId;
    }

    public void setPreferredAmiId(String preferredAmiId) {
        this.preferredAmiId = preferredAmiId;
    }

    public String getPreferredInstanceType() {
        return preferredInstanceType;
    }

    public void setPreferredInstanceType(String preferredInstanceType) {
        this.preferredInstanceType = preferredInstanceType;
    }
}
