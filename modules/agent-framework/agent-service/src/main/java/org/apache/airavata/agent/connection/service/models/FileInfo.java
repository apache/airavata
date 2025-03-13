package org.apache.airavata.agent.connection.service.models;

import java.time.Instant;

public class FileInfo {

    private boolean userHasWriteAccess;
    private String name;
    private String downloadURL;
    private String dataProductURI;
    private Instant createdTime;
    private Instant modifiedTime;
    private String mimeType;
    private long size;
    private boolean hidden;

    public boolean isUserHasWriteAccess() {
        return userHasWriteAccess;
    }

    public void setUserHasWriteAccess(boolean userHasWriteAccess) {
        this.userHasWriteAccess = userHasWriteAccess;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public String getDataProductURI() {
        return dataProductURI;
    }

    public void setDataProductURI(String dataProductURI) {
        this.dataProductURI = dataProductURI;
    }

    public Instant getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Instant createdTime) {
        this.createdTime = createdTime;
    }

    public Instant getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Instant modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
