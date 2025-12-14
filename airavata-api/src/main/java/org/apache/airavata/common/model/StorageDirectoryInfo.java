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
package org.apache.airavata.common.model;

import java.util.Objects;

/**
 * Domain model: StorageDirectoryInfo
 */
public class StorageDirectoryInfo {
    private String totalSize;
    private long totalSizeBytes;

    public StorageDirectoryInfo() {}

    public String getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(String totalSize) {
        this.totalSize = totalSize;
    }

    public long getTotalSizeBytes() {
        return totalSizeBytes;
    }

    public void setTotalSizeBytes(long totalSizeBytes) {
        this.totalSizeBytes = totalSizeBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorageDirectoryInfo that = (StorageDirectoryInfo) o;
        return Objects.equals(totalSize, that.totalSize) && Objects.equals(totalSizeBytes, that.totalSizeBytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalSize, totalSizeBytes);
    }

    @Override
    public String toString() {
        return "StorageDirectoryInfo{" + "totalSize=" + totalSize + ", totalSizeBytes=" + totalSizeBytes + "}";
    }
}
