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
 * Domain model: ProcessSubmitEvent
 */
public class ProcessSubmitEvent extends MessagingEvent {
    private String processId;
    private String gatewayId;
    private String experimentId;
    private String tokenId;

    public ProcessSubmitEvent() {}

    public ProcessSubmitEvent(String processId, String gatewayId, String experimentId, String tokenId) {
        this.processId = processId;
        this.gatewayId = gatewayId;
        this.experimentId = experimentId;
        this.tokenId = tokenId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessSubmitEvent that = (ProcessSubmitEvent) o;
        return Objects.equals(processId, that.processId)
                && Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(experimentId, that.experimentId)
                && Objects.equals(tokenId, that.tokenId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processId, gatewayId, experimentId, tokenId);
    }

    @Override
    public String toString() {
        return "ProcessSubmitEvent{" + "processId=" + processId + ", gatewayId=" + gatewayId + ", experimentId="
                + experimentId + ", tokenId=" + tokenId + "}";
    }
}
