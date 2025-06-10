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
package org.apache.airavata.registry.core.entities.expcatalog;

import java.io.Serializable;

public class ComputationalResourceSchedulingPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String experimentId;
    private String resourceHostId;
    private String queueName;

    public ComputationalResourceSchedulingPK() {}

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

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ComputationalResourceSchedulingPK)) {
            return false;
        }
        ComputationalResourceSchedulingPK castOther = (ComputationalResourceSchedulingPK) other;
        return this.experimentId.equals(castOther.experimentId)
                && this.resourceHostId.equals(castOther.resourceHostId)
                && this.queueName.equals(castOther.queueName);
    }

    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.experimentId.hashCode();
        hash = hash * prime + this.resourceHostId.hashCode();
        hash = hash * prime + this.queueName.hashCode();

        return hash;
    }
}
