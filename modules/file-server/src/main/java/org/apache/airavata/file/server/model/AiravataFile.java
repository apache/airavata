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

import org.apache.airavata.agents.api.FileMetadata;

public class AiravataFile {
    private String fileName;
    private long fileSize;
    private long createdTime;
    private long updatedTime;

    public static AiravataFile fromMetadata(FileMetadata metadata) {
        // replace System.currentTimeMillis() with correct times
        return new AiravataFile(
                metadata.getName(), metadata.getSize(), System.currentTimeMillis(), System.currentTimeMillis());
    }

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
