package org.apache.airavata.research.service.dto;

import java.util.Set;

public class CreateProjectRequest {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public Set<String> getDatasetIds() {
        return datasetIds;
    }

    public void setDatasetIds(Set<String> datasetIds) {
        this.datasetIds = datasetIds;
    }

    public String name;
    public String ownerId;
    public String repositoryId;
    public Set<String> datasetIds;
}
