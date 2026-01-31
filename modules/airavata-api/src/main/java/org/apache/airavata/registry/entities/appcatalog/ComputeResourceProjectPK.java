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
package org.apache.airavata.registry.entities.appcatalog;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for ComputeResourceProjectEntity.
 */
public class ComputeResourceProjectPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String computeResourceId;
    private String projectName;

    public ComputeResourceProjectPK() {}

    public ComputeResourceProjectPK(String computeResourceId, String projectName) {
        this.computeResourceId = computeResourceId;
        this.projectName = projectName;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComputeResourceProjectPK that = (ComputeResourceProjectPK) o;
        return Objects.equals(computeResourceId, that.computeResourceId)
                && Objects.equals(projectName, that.projectName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(computeResourceId, projectName);
    }

    @Override
    public String toString() {
        return "ComputeResourceProjectPK{"
                + "computeResourceId='" + computeResourceId + '\''
                + ", projectName='" + projectName + '\''
                + '}';
    }
}
