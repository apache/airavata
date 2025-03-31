package org.apache.airavata.research.service.model.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import org.apache.airavata.research.service.enums.ResourceTypeEnum;

@Entity
public class DatasetResource extends Resource {
    @Column(nullable = false)
    private String datasetUrl;

    public String getDatasetUrl() {
        return datasetUrl;
    }

    public void setDatasetUrl(String datasetUrl) {
        this.datasetUrl = datasetUrl;
    }

    @Override
    public ResourceTypeEnum getType() {
        return ResourceTypeEnum.DATASET;
    }
}
