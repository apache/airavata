package org.apache.airavata.agent.connection.service.models;

import java.util.List;

public class ExperimentStorageResponse {

    private boolean isDir;
    private List<DirectoryInfo> directories;
    private List<FileInfo> files;
    private List<Object> parts;

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }

    public List<DirectoryInfo> getDirectories() {
        return directories;
    }

    public void setDirectories(List<DirectoryInfo> directories) {
        this.directories = directories;
    }

    public List<FileInfo> getFiles() {
        return files;
    }

    public void setFiles(List<FileInfo> files) {
        this.files = files;
    }

    public List<Object> getParts() {
        return parts;
    }

    public void setParts(List<Object> parts) {
        this.parts = parts;
    }
}
