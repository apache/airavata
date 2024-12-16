package org.apache.airavata.file.server.model;

import java.util.ArrayList;
import java.util.List;

public class AiravataDirectory {
    private String directoryName;
    private long createdTime;

    private List<AiravataFile> innerFiles = new ArrayList<>();
    private List<AiravataDirectory> innerDirectories = new ArrayList<>();

    public AiravataDirectory(String directoryName, long createdTime) {
        this.directoryName = directoryName;
        this.createdTime = createdTime;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public List<AiravataFile> getInnerFiles() {
        return innerFiles;
    }

    public void setInnerFiles(List<AiravataFile> innerFiles) {
        this.innerFiles = innerFiles;
    }

    public List<AiravataDirectory> getInnerDirectories() {
        return innerDirectories;
    }

    public void setInnerDirectories(List<AiravataDirectory> innerDirectories) {
        this.innerDirectories = innerDirectories;
    }
}
