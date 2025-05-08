package org.apache.airavata.research.service.dto;

public class ModifyResourceRequest extends CreateResourceRequest{
    public String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
