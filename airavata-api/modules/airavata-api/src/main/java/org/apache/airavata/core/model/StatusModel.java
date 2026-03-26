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
package org.apache.airavata.core.model;

import java.util.Objects;
import org.apache.airavata.core.util.IdGenerator;

/**
 * Generic domain model for status records.
 *
 * <p>Replaces the four type-specific status classes:
 * <ul>
 *   <li>{@code ExperimentStatus} → {@code StatusModel<ExperimentState>}</li>
 *   <li>{@code ProcessStatus}    → {@code StatusModel<ProcessState>}</li>
 *   <li>{@code TaskStatus}       → {@code StatusModel<TaskState>}</li>
 *   <li>{@code JobStatus}        → {@code StatusModel<JobState>}</li>
 * </ul>
 *
 * @param <S> the specific state enum type (must be an enum)
 */
public class StatusModel<S extends Enum<S>> {

    private S state;
    private long timeOfStateChange;
    private String reason;
    private String statusId;

    public StatusModel() {}

    public StatusModel(S state) {
        this.state = state;
        this.timeOfStateChange = System.currentTimeMillis();
    }

    public static <S extends Enum<S>> StatusModel<S> of(S state) {
        StatusModel<S> m = new StatusModel<>();
        m.state = state;
        m.timeOfStateChange = IdGenerator.getCurrentTimestamp().toEpochMilli();
        return m;
    }

    public static <S extends Enum<S>> StatusModel<S> of(S state, String reason) {
        StatusModel<S> m = of(state);
        m.reason = reason;
        return m;
    }

    public S getState() {
        return state;
    }

    public void setState(S state) {
        this.state = state;
    }

    public long getTimeOfStateChange() {
        return timeOfStateChange;
    }

    public void setTimeOfStateChange(long timeOfStateChange) {
        this.timeOfStateChange = timeOfStateChange;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatusModel<?> that = (StatusModel<?>) o;
        return timeOfStateChange == that.timeOfStateChange
                && Objects.equals(state, that.state)
                && Objects.equals(reason, that.reason)
                && Objects.equals(statusId, that.statusId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, timeOfStateChange, reason, statusId);
    }

    @Override
    public String toString() {
        return "StatusModel{" + "state=" + state + ", timeOfStateChange=" + timeOfStateChange + ", reason=" + reason
                + ", statusId=" + statusId + "}";
    }
}
