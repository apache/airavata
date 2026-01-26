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
package org.apache.airavata.registry.entities.expcatalog;

import java.io.Serializable;
import java.util.Objects;

/**
 * The primary key class for the computational_resource_scheduling database table.
 */
public class ComputationalResourceSchedulingPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String experimentId;
    private String resourceHostId;
    private String queueName;

    public ComputationalResourceSchedulingPK() {}

    public ComputationalResourceSchedulingPK(String experimentId, String resourceHostId, String queueName) {
        this.experimentId = experimentId;
        this.resourceHostId = resourceHostId;
        this.queueName = queueName;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getResourceHostId() {
        return resourceHostId;
    }

    public void setResourceHostId(String resourceHostId) {
        this.resourceHostId = resourceHostId;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComputationalResourceSchedulingPK that = (ComputationalResourceSchedulingPK) o;
        return Objects.equals(experimentId, that.experimentId)
                && Objects.equals(resourceHostId, that.resourceHostId)
                && Objects.equals(queueName, that.queueName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(experimentId, resourceHostId, queueName);
    }

    @Override
    public String toString() {
        return "ComputationalResourceSchedulingPK{"
                + "experimentId='" + experimentId + '\''
                + ", resourceHostId='" + resourceHostId + '\''
                + ", queueName='" + queueName + '\''
                + '}';
    }
}
