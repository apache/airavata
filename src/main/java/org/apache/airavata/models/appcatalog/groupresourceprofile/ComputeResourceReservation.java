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
 * {@code org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ComputeResourceReservation}.
 */
public record ComputeResourceReservation(
        String reservationId, String reservationName, List<String> queueNames, long startTime, long endTime) {

    public ComputeResourceReservation {
        queueNames = queueNames == null ? List.of() : List.copyOf(queueNames);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String reservationId = "";
        private String reservationName = "";
        private List<String> queueNames = new ArrayList<>();
        private long startTime;
        private long endTime;

        private Builder() {}

        private Builder(ComputeResourceReservation source) {
            this.reservationId = source.reservationId;
            this.reservationName = source.reservationName;
            this.queueNames = new ArrayList<>(source.queueNames);
            this.startTime = source.startTime;
            this.endTime = source.endTime;
        }

        public Builder setReservationId(String reservationId) {
            this.reservationId = reservationId;
            return this;
        }

        public Builder setReservationName(String reservationName) {
            this.reservationName = reservationName;
            return this;
        }

        public Builder addQueueNames(String value) {
            this.queueNames.add(value);
            return this;
        }

        public Builder addAllQueueNames(Iterable<String> values) {
            values.forEach(this.queueNames::add);
            return this;
        }

        public Builder clearQueueNames() {
            this.queueNames.clear();
            return this;
        }

        public Builder setStartTime(long startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder setEndTime(long endTime) {
            this.endTime = endTime;
            return this;
        }

        public ComputeResourceReservation build() {
            return new ComputeResourceReservation(reservationId, reservationName, queueNames, startTime, endTime);
        }
    }
}
