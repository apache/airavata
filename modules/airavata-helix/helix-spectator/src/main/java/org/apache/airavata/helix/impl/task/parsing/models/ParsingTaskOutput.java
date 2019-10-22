package org.apache.airavata.helix.impl.task.parsing.models;

public class ParsingTaskOutput {
    private String id;
    private String contextVariableName;
    private String storageResourceId;
    private String uploadDirectory;

    public String getContextVariableName() {
        return contextVariableName;
    }

    public void setContextVariableName(String contextVariableName) {
        this.contextVariableName = contextVariableName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public String getUploadDirectory() {
        return uploadDirectory;
    }

    public void setUploadDirectory(String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
    }
}
