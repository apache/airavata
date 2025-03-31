package org.apache.airavata.research.service.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.apache.airavata.research.service.enums.ResourceTypeEnum;

@Entity
public class ModelResource extends Resource {
    @Column(nullable = false)
    private String applicationInterfaceId;

    @Column(nullable = false)
    private String version;

    public String getApplicationInterfaceId() {
        return applicationInterfaceId;
    }

    public void setApplicationInterfaceId(String applicationInterfaceId) {
        this.applicationInterfaceId = applicationInterfaceId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public ResourceTypeEnum getType() {
        return ResourceTypeEnum.MODEL;
    }
}
