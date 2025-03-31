package org.apache.airavata.research.service.ResponseTypes;

import org.apache.airavata.research.service.enums.ResourceTypeEnum;
import org.apache.airavata.research.service.model.entity.Resource;

public class ResourceResponse {
    private ResourceTypeEnum type;
    private Resource resource;

    // Getters and Setters
    public ResourceTypeEnum getType() {
        return type;
    }

    public void setType(ResourceTypeEnum type) {
        this.type = type;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }
}
