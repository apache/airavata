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

import java.util.Objects;

/**
 * Domain model: HandlerStatus
 */
public class HandlerStatus {
    private String id;
    private HandlerState state;
    private String description;
    private long updatedAt;

    public HandlerStatus() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HandlerState getState() {
        return state;
    }

    public void setState(HandlerState state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HandlerStatus that = (HandlerStatus) o;
        return Objects.equals(id, that.id)
                && Objects.equals(state, that.state)
                && Objects.equals(description, that.description)
                && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, state, description, updatedAt);
    }

    @Override
    public String toString() {
        return "HandlerStatus{" + "id=" + id + ", state=" + state + ", description=" + description + ", updatedAt="
                + updatedAt + "}";
    }
}
