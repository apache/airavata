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
package org.apache.airavata.research.experiment.model;

import java.util.Objects;

/**
 * Reference to a artifact made available in experiment scope.
 * One or more artifacts can be copied, mounted, etc. at specified locations when running an experiment.
 */
public class ExperimentArtifactModel {
    private String experimentArtifactId;
    private String artifactUri;
    private String repositoryId;
    /** Path/location in experiment scope where this artifact is made available (e.g. mount path, copy destination). */
    private String locationPath;

    private Integer orderIndex;

    public String getExperimentArtifactId() {
        return experimentArtifactId;
    }

    public void setExperimentArtifactId(String experimentArtifactId) {
        this.experimentArtifactId = experimentArtifactId;
    }

    public String getArtifactUri() {
        return artifactUri;
    }

    public void setArtifactUri(String artifactUri) {
        this.artifactUri = artifactUri;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getLocationPath() {
        return locationPath;
    }

    public void setLocationPath(String locationPath) {
        this.locationPath = locationPath;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExperimentArtifactModel that = (ExperimentArtifactModel) o;
        return Objects.equals(experimentArtifactId, that.experimentArtifactId)
                && Objects.equals(artifactUri, that.artifactUri)
                && Objects.equals(repositoryId, that.repositoryId)
                && Objects.equals(locationPath, that.locationPath)
                && Objects.equals(orderIndex, that.orderIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(experimentArtifactId, artifactUri, repositoryId, locationPath, orderIndex);
    }

    @Override
    public String toString() {
        return "ExperimentArtifactModel{experimentArtifactId='" + experimentArtifactId
                + "', artifactUri='" + artifactUri + "', repositoryId='" + repositoryId
                + "', locationPath='" + locationPath + "', orderIndex=" + orderIndex + "}";
    }
}
