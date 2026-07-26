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
package org.apache.airavata.models.appcatalog.gatewayprofile;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.gatewayprofile.proto.ComputeResourcePreference}.
 */
public record ComputeResourcePreference(
        String computeResourceId,
        boolean overrideByAiravata,
        String loginUserName,
        String preferredBatchQueue,
        String scratchLocation,
        String allocationProjectNumber,
        String resourceSpecificCredentialStoreToken,
        String usageReportingGatewayId,
        String qualityOfService,
        String reservation,
        long reservationStartTime,
        long reservationEndTime,
        String sshAccountProvisioner,
        Map<String, String> sshAccountProvisionerConfig,
        String sshAccountProvisionerAdditionalInfo) {

    public ComputeResourcePreference {
        sshAccountProvisionerConfig =
                sshAccountProvisionerConfig == null ? Map.of() : Map.copyOf(sshAccountProvisionerConfig);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String computeResourceId = "";
        private boolean overrideByAiravata;
        private String loginUserName = "";
        private String preferredBatchQueue = "";
        private String scratchLocation = "";
        private String allocationProjectNumber = "";
        private String resourceSpecificCredentialStoreToken = "";
        private String usageReportingGatewayId = "";
        private String qualityOfService = "";
        private String reservation = "";
        private long reservationStartTime;
        private long reservationEndTime;
        private String sshAccountProvisioner = "";
        private Map<String, String> sshAccountProvisionerConfig = new LinkedHashMap<>();
        private String sshAccountProvisionerAdditionalInfo = "";

        private Builder() {}

        private Builder(ComputeResourcePreference source) {
            this.computeResourceId = source.computeResourceId;
            this.overrideByAiravata = source.overrideByAiravata;
            this.loginUserName = source.loginUserName;
            this.preferredBatchQueue = source.preferredBatchQueue;
            this.scratchLocation = source.scratchLocation;
            this.allocationProjectNumber = source.allocationProjectNumber;
            this.resourceSpecificCredentialStoreToken = source.resourceSpecificCredentialStoreToken;
            this.usageReportingGatewayId = source.usageReportingGatewayId;
            this.qualityOfService = source.qualityOfService;
            this.reservation = source.reservation;
            this.reservationStartTime = source.reservationStartTime;
            this.reservationEndTime = source.reservationEndTime;
            this.sshAccountProvisioner = source.sshAccountProvisioner;
            this.sshAccountProvisionerConfig = new LinkedHashMap<>(source.sshAccountProvisionerConfig);
            this.sshAccountProvisionerAdditionalInfo = source.sshAccountProvisionerAdditionalInfo;
        }

        public Builder setComputeResourceId(String computeResourceId) {
            this.computeResourceId = computeResourceId;
            return this;
        }

        public Builder setOverrideByAiravata(boolean overrideByAiravata) {
            this.overrideByAiravata = overrideByAiravata;
            return this;
        }

        public Builder setLoginUserName(String loginUserName) {
            this.loginUserName = loginUserName;
            return this;
        }

        public Builder setPreferredBatchQueue(String preferredBatchQueue) {
            this.preferredBatchQueue = preferredBatchQueue;
            return this;
        }

        public Builder setScratchLocation(String scratchLocation) {
            this.scratchLocation = scratchLocation;
            return this;
        }

        public Builder setAllocationProjectNumber(String allocationProjectNumber) {
            this.allocationProjectNumber = allocationProjectNumber;
            return this;
        }

        public Builder setResourceSpecificCredentialStoreToken(String resourceSpecificCredentialStoreToken) {
            this.resourceSpecificCredentialStoreToken = resourceSpecificCredentialStoreToken;
            return this;
        }

        public Builder setUsageReportingGatewayId(String usageReportingGatewayId) {
            this.usageReportingGatewayId = usageReportingGatewayId;
            return this;
        }

        public Builder setQualityOfService(String qualityOfService) {
            this.qualityOfService = qualityOfService;
            return this;
        }

        public Builder setReservation(String reservation) {
            this.reservation = reservation;
            return this;
        }

        public Builder setReservationStartTime(long reservationStartTime) {
            this.reservationStartTime = reservationStartTime;
            return this;
        }

        public Builder setReservationEndTime(long reservationEndTime) {
            this.reservationEndTime = reservationEndTime;
            return this;
        }

        public Builder setSshAccountProvisioner(String sshAccountProvisioner) {
            this.sshAccountProvisioner = sshAccountProvisioner;
            return this;
        }

        public Builder putSshAccountProvisionerConfig(String key, String value) {
            this.sshAccountProvisionerConfig.put(key, value);
            return this;
        }

        public Builder putAllSshAccountProvisionerConfig(Map<String, String> values) {
            this.sshAccountProvisionerConfig.putAll(values);
            return this;
        }

        public Builder clearSshAccountProvisionerConfig() {
            this.sshAccountProvisionerConfig.clear();
            return this;
        }

        public Builder setSshAccountProvisionerAdditionalInfo(String sshAccountProvisionerAdditionalInfo) {
            this.sshAccountProvisionerAdditionalInfo = sshAccountProvisionerAdditionalInfo;
            return this;
        }

        public ComputeResourcePreference build() {
            return new ComputeResourcePreference(
                    computeResourceId, overrideByAiravata, loginUserName, preferredBatchQueue, scratchLocation,
                    allocationProjectNumber, resourceSpecificCredentialStoreToken, usageReportingGatewayId,
                    qualityOfService, reservation, reservationStartTime, reservationEndTime,
                    sshAccountProvisioner, sshAccountProvisionerConfig, sshAccountProvisionerAdditionalInfo);
        }
    }
}
