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
 * Composite primary key for ProjectQueueAccessEntity.
 */
public class ProjectQueueAccessPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String computeResourceId;
    private String projectName;
    private String queueName;

    public ProjectQueueAccessPK() {}

    public ProjectQueueAccessPK(String computeResourceId, String projectName, String queueName) {
        this.computeResourceId = computeResourceId;
        this.projectName = projectName;
        this.queueName = queueName;
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
        ProjectQueueAccessPK that = (ProjectQueueAccessPK) o;
        return Objects.equals(computeResourceId, that.computeResourceId)
                && Objects.equals(projectName, that.projectName)
                && Objects.equals(queueName, that.queueName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(computeResourceId, projectName, queueName);
    }

    @Override
    public String toString() {
        return "ProjectQueueAccessPK{"
                + "computeResourceId='" + computeResourceId + '\''
                + ", projectName='" + projectName + '\''
                + ", queueName='" + queueName + '\''
                + '}';
    }
}
