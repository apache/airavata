/**
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
 * Composite primary key for {@link ProjectResourceAccountEntity}.
 */
public class ProjectResourceAccountPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String projectId;
    private String computeResourceId;

    public ProjectResourceAccountPK() {}

    public ProjectResourceAccountPK(String projectId, String computeResourceId) {
        this.projectId = projectId;
        this.computeResourceId = computeResourceId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectResourceAccountPK that = (ProjectResourceAccountPK) o;
        return Objects.equals(projectId, that.projectId)
                && Objects.equals(computeResourceId, that.computeResourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, computeResourceId);
    }
}
