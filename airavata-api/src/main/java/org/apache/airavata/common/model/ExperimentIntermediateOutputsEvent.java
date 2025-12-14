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
 * Domain model: ExperimentIntermediateOutputsEvent
 */
public class ExperimentIntermediateOutputsEvent extends MessagingEvent {
    private String experimentId;
    private String gatewayId;
    private List<String> outputNames;

    public ExperimentIntermediateOutputsEvent() {}

    public ExperimentIntermediateOutputsEvent(String experimentId, String gatewayId, List<String> outputNames) {
        this.experimentId = experimentId;
        this.gatewayId = gatewayId;
        this.outputNames = outputNames;
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

    public List<String> getOutputNames() {
        return outputNames;
    }

    public void setOutputNames(List<String> outputNames) {
        this.outputNames = outputNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExperimentIntermediateOutputsEvent that = (ExperimentIntermediateOutputsEvent) o;
        return Objects.equals(experimentId, that.experimentId)
                && Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(outputNames, that.outputNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(experimentId, gatewayId, outputNames);
    }

    @Override
    public String toString() {
        return "ExperimentIntermediateOutputsEvent{" + "experimentId=" + experimentId + ", gatewayId=" + gatewayId
                + ", outputNames=" + outputNames + "}";
    }
}
