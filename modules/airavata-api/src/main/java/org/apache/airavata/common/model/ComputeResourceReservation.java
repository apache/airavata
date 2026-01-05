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
package org.apache.airavata.common.model;

import java.util.List;
import java.util.Objects;

/**
 * Domain model: ComputeResourceReservation
 */
public class ComputeResourceReservation {
    private String reservationId;
    private String reservationName;
    private List<String> queueNames;
    private long startTime;
    private long endTime;

    public ComputeResourceReservation() {}

    public ComputeResourceReservation(
            String reservationId, String reservationName, List<String> queueNames, long startTime, long endTime) {
        this.reservationId = reservationId;
        this.reservationName = reservationName;
        this.queueNames = queueNames;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getReservationName() {
        return reservationName;
    }

    public void setReservationName(String reservationName) {
        this.reservationName = reservationName;
    }

    public List<String> getQueueNames() {
        return queueNames;
    }

    public void setQueueNames(List<String> queueNames) {
        this.queueNames = queueNames;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComputeResourceReservation that = (ComputeResourceReservation) o;
        return Objects.equals(reservationId, that.reservationId)
                && Objects.equals(reservationName, that.reservationName)
                && Objects.equals(queueNames, that.queueNames)
                && Objects.equals(startTime, that.startTime)
                && Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId, reservationName, queueNames, startTime, endTime);
    }

    @Override
    public String toString() {
        return "ComputeResourceReservation{" + "reservationId=" + reservationId + ", reservationName=" + reservationName
                + ", queueNames=" + queueNames + ", startTime=" + startTime + ", endTime=" + endTime + "}";
    }
}
