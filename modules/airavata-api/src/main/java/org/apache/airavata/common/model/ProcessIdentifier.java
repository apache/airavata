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
 * Domain model: ProcessIdentifier
 */
public class ProcessIdentifier {
    private String processId;
    private String experimentId;
    private String gatewayId;

    public ProcessIdentifier() {}

    public ProcessIdentifier(String processId, String experimentId, String gatewayId) {
        this.processId = processId;
        this.experimentId = experimentId;
        this.gatewayId = gatewayId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessIdentifier that = (ProcessIdentifier) o;
        return Objects.equals(processId, that.processId)
                && Objects.equals(experimentId, that.experimentId)
                && Objects.equals(gatewayId, that.gatewayId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processId, experimentId, gatewayId);
    }

    @Override
    public String toString() {
        return "ProcessIdentifier{" + "processId=" + processId + ", experimentId=" + experimentId + ", gatewayId="
                + gatewayId + "}";
    }
}
