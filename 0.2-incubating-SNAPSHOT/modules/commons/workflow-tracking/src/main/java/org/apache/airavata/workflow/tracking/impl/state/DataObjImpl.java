/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.workflow.tracking.impl.state;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.apache.airavata.workflow.tracking.common.DataObj;

public class DataObjImpl implements DataObj {

    protected URI dataId;
    protected List<URI> locations;
    protected long sizeInBytes = -1;

    public DataObjImpl(URI dataId_, List<URI> location_) {

        dataId = dataId_;
        if (dataId == null || dataId.toString().length() == 0)
            throw new RuntimeException("Data ID cannot be NULL or empty");

        locations = location_;
    }

    public DataObjImpl(URI dataId_, List<URI> location_, long sizeInBytes_) {

        this(dataId_, location_);
        sizeInBytes = sizeInBytes_;
    }

    public URI getId() {

        return dataId;
    }

    public URI getLocalLocation() {

        return locations != null && locations.size() > 0 ? locations.get(0) : null;
    }

    public List<URI> getLocations() {

        return locations;
    }

    public long getSizeInBytes() {

        // skip getting bytes if already calculated or not possible to calculate
        if (sizeInBytes >= 0 || locations == null || locations.size() == 0)
            return sizeInBytes;

        // check if the location is a local file. If so, we calculate the size.
        URI location = locations.get(0);
        String scheme = location.getScheme();
        String authority = location.getAuthority();
        if ((scheme == null && authority == null) || "file".equals(scheme)) {
            sizeInBytes = getFileSize(new File(location.getPath()));
        }
        return sizeInBytes;
    }

    protected static final long getFileSize(File file) {
        if (file.isDirectory()) {
            return getDirSize(file, 0, true);
        } else {
            return file.length();
        }
    }

    private static final long getDirSize(File dir, long size, boolean recurse) {
        File[] files = dir.listFiles();
        if (files == null)
            return size;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                if (recurse)
                    size += getDirSize(files[i], size, recurse);
            } else {
                size += files[i].length();
            }
        }
        return size;
    }

}
