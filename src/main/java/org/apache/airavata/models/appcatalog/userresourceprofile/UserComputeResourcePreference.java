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
package org.apache.airavata.models.appcatalog.userresourceprofile;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserComputeResourcePreference}.
 */
public record UserComputeResourcePreference(
        String computeResourceId,
        String loginUserName,
        String preferredBatchQueue,
        String scratchLocation,
        String allocationProjectNumber,
        String resourceSpecificCredentialStoreToken,
        String qualityOfService,
        String reservation,
        long reservationStartTime,
        long reservationEndTime,
        boolean validated) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String computeResourceId = "";
        private String loginUserName = "";
        private String preferredBatchQueue = "";
        private String scratchLocation = "";
        private String allocationProjectNumber = "";
        private String resourceSpecificCredentialStoreToken = "";
        private String qualityOfService = "";
        private String reservation = "";
        private long reservationStartTime;
        private long reservationEndTime;
        private boolean validated;

        private Builder() {}

        private Builder(UserComputeResourcePreference source) {
            this.computeResourceId = source.computeResourceId;
            this.loginUserName = source.loginUserName;
            this.preferredBatchQueue = source.preferredBatchQueue;
            this.scratchLocation = source.scratchLocation;
            this.allocationProjectNumber = source.allocationProjectNumber;
            this.resourceSpecificCredentialStoreToken = source.resourceSpecificCredentialStoreToken;
            this.qualityOfService = source.qualityOfService;
            this.reservation = source.reservation;
            this.reservationStartTime = source.reservationStartTime;
            this.reservationEndTime = source.reservationEndTime;
            this.validated = source.validated;
        }

        public Builder setComputeResourceId(String computeResourceId) {
            this.computeResourceId = computeResourceId;
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

        public Builder setValidated(boolean validated) {
            this.validated = validated;
            return this;
        }

        public UserComputeResourcePreference build() {
            return new UserComputeResourcePreference(
                    computeResourceId, loginUserName, preferredBatchQueue, scratchLocation,
                    allocationProjectNumber, resourceSpecificCredentialStoreToken, qualityOfService, reservation,
                    reservationStartTime, reservationEndTime, validated);
        }
    }
}
