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
package org.apache.airavata.status.model;

import java.util.Objects;
import org.apache.airavata.core.model.ResourceIdentifier;
import org.apache.airavata.execution.model.ProcessState;

public class ProcessStatusChangedEvent {
    private ProcessState state;
    private ResourceIdentifier processIdentity;

    public ProcessStatusChangedEvent() {}

    public ProcessStatusChangedEvent(ProcessState state, ResourceIdentifier processIdentity) {
        this.state = state;
        this.processIdentity = processIdentity;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public ResourceIdentifier getProcessIdentity() {
        return processIdentity;
    }

    public void setProcessIdentity(ResourceIdentifier processIdentity) {
        this.processIdentity = processIdentity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessStatusChangedEvent that = (ProcessStatusChangedEvent) o;
        return Objects.equals(state, that.state) && Objects.equals(processIdentity, that.processIdentity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, processIdentity);
    }

    @Override
    public String toString() {
        return "ProcessStatusChangedEvent{" + "state=" + state + ", processIdentity=" + processIdentity + "}";
    }
}
