/**
*
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
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.file.server.model;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.agents.api.FileMetadata;

public class AiravataDirectory {
    private String directoryName;
    private long size;
    private long createdTime;

    private List<AiravataFile> innerFiles = new ArrayList<>();
    private List<AiravataDirectory> innerDirectories = new ArrayList<>();

    public static AiravataDirectory fromMetadata(FileMetadata metadata) {
        // replace System.currentTimeMillis() with correct times
        return new AiravataDirectory(metadata.getName(), metadata.getSize(), System.currentTimeMillis());
    }

    public AiravataDirectory(String directoryName, long size, long createdTime) {
        this.directoryName = directoryName;
        this.size = size;
        this.createdTime = createdTime;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
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
