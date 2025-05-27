/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
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
