package org.apache.airavata.research.service.model.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import org.apache.airavata.research.service.enums.ResourceTypeEnum;

@Entity
public class RepositoryResource extends Resource  {
    @Column(nullable = false)
    private String repositoryUrl;

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    @Override
    public ResourceTypeEnum getType() {
        return ResourceTypeEnum.REPOSITORY;
    }
}
