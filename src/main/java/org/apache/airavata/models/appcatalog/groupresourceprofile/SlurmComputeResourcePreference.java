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
package org.apache.airavata.models.appcatalog.groupresourceprofile;

import java.util.ArrayList;
import java.util.List;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.groupresourceprofile.proto.SlurmComputeResourcePreference}.
 */
public record SlurmComputeResourcePreference(
        String allocationProjectNumber,
        String preferredBatchQueue,
        String qualityOfService,
        String usageReportingGatewayId,
        String sshAccountProvisioner,
        List<GroupAccountSSHProvisionerConfig> groupSshAccountProvisionerConfigs,
        String sshAccountProvisionerAdditionalInfo,
        List<ComputeResourceReservation> reservations) {

    public SlurmComputeResourcePreference {
        groupSshAccountProvisionerConfigs =
                groupSshAccountProvisionerConfigs == null ? List.of() : List.copyOf(groupSshAccountProvisionerConfigs);
        reservations = reservations == null ? List.of() : List.copyOf(reservations);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String allocationProjectNumber = "";
        private String preferredBatchQueue = "";
        private String qualityOfService = "";
        private String usageReportingGatewayId = "";
        private String sshAccountProvisioner = "";
        private List<GroupAccountSSHProvisionerConfig> groupSshAccountProvisionerConfigs = new ArrayList<>();
        private String sshAccountProvisionerAdditionalInfo = "";
        private List<ComputeResourceReservation> reservations = new ArrayList<>();

        private Builder() {}

        private Builder(SlurmComputeResourcePreference source) {
            this.allocationProjectNumber = source.allocationProjectNumber;
            this.preferredBatchQueue = source.preferredBatchQueue;
            this.qualityOfService = source.qualityOfService;
            this.usageReportingGatewayId = source.usageReportingGatewayId;
            this.sshAccountProvisioner = source.sshAccountProvisioner;
            this.groupSshAccountProvisionerConfigs = new ArrayList<>(source.groupSshAccountProvisionerConfigs);
            this.sshAccountProvisionerAdditionalInfo = source.sshAccountProvisionerAdditionalInfo;
            this.reservations = new ArrayList<>(source.reservations);
        }

        public Builder setAllocationProjectNumber(String allocationProjectNumber) {
            this.allocationProjectNumber = allocationProjectNumber;
            return this;
        }

        public Builder setPreferredBatchQueue(String preferredBatchQueue) {
            this.preferredBatchQueue = preferredBatchQueue;
            return this;
        }

        public Builder setQualityOfService(String qualityOfService) {
            this.qualityOfService = qualityOfService;
            return this;
        }

        public Builder setUsageReportingGatewayId(String usageReportingGatewayId) {
            this.usageReportingGatewayId = usageReportingGatewayId;
            return this;
        }

        public Builder setSshAccountProvisioner(String sshAccountProvisioner) {
            this.sshAccountProvisioner = sshAccountProvisioner;
            return this;
        }

        public Builder addGroupSshAccountProvisionerConfigs(GroupAccountSSHProvisionerConfig value) {
            this.groupSshAccountProvisionerConfigs.add(value);
            return this;
        }

        public Builder addAllGroupSshAccountProvisionerConfigs(Iterable<GroupAccountSSHProvisionerConfig> values) {
            values.forEach(this.groupSshAccountProvisionerConfigs::add);
            return this;
        }

        public Builder clearGroupSshAccountProvisionerConfigs() {
            this.groupSshAccountProvisionerConfigs.clear();
            return this;
        }

        public Builder setSshAccountProvisionerAdditionalInfo(String sshAccountProvisionerAdditionalInfo) {
            this.sshAccountProvisionerAdditionalInfo = sshAccountProvisionerAdditionalInfo;
            return this;
        }

        public Builder addReservations(ComputeResourceReservation value) {
            this.reservations.add(value);
            return this;
        }

        public Builder addAllReservations(Iterable<ComputeResourceReservation> values) {
            values.forEach(this.reservations::add);
            return this;
        }

        public Builder clearReservations() {
            this.reservations.clear();
            return this;
        }

        public SlurmComputeResourcePreference build() {
            return new SlurmComputeResourcePreference(
                    allocationProjectNumber, preferredBatchQueue, qualityOfService, usageReportingGatewayId,
                    sshAccountProvisioner, groupSshAccountProvisionerConfigs, sshAccountProvisionerAdditionalInfo,
                    reservations);
        }
    }
}
