package org.apache.airavata.file.server.model;

public class AiravataFile {
    private String fileName;
    private long fileSize;
    private long createdTime;
    private long updatedTime;

    public AiravataFile(String fileName, long fileSize, long createdTime, long updatedTime) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }
}
