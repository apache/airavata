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
 * Domain model: ComputeResourceProject
 * Represents a SLURM project/account and its access to queues.
 */
public class ComputeResourceProject {
    private String projectName;
    private String description;
    private List<String> allowedQueues; // List of queue names this project has access to

    public ComputeResourceProject() {}

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getAllowedQueues() {
        return allowedQueues;
    }

    public void setAllowedQueues(List<String> allowedQueues) {
        this.allowedQueues = allowedQueues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComputeResourceProject that = (ComputeResourceProject) o;
        return Objects.equals(projectName, that.projectName)
                && Objects.equals(description, that.description)
                && Objects.equals(allowedQueues, that.allowedQueues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectName, description, allowedQueues);
    }

    @Override
    public String toString() {
        return "ComputeResourceProject{"
                + "projectName='" + projectName + '\''
                + ", description='" + description + '\''
                + ", allowedQueues=" + allowedQueues
                + '}';
    }
}
