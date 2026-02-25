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
package org.apache.airavata.compute.resource.model;

import java.util.Objects;

/**
 * Domain model: AwsComputeResourcePreference
 */
public class AwsComputeResourcePreference {
    private String region;
    private String preferredAmiId;
    private String preferredInstanceType;

    public AwsComputeResourcePreference() {}

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AwsComputeResourcePreference that = (AwsComputeResourcePreference) o;
        return Objects.equals(region, that.region)
                && Objects.equals(preferredAmiId, that.preferredAmiId)
                && Objects.equals(preferredInstanceType, that.preferredInstanceType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(region, preferredAmiId, preferredInstanceType);
    }

    @Override
    public String toString() {
        return "AwsComputeResourcePreference{" + "region='"
                + region + '\'' + ", preferredAmiId='"
                + preferredAmiId + '\'' + ", preferredInstanceType='"
                + preferredInstanceType + '\'' + '}';
    }
}
