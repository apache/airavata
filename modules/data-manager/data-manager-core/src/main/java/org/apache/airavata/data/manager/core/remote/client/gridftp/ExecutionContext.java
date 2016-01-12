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
package org.apache.airavata.data.manager.core.remote.client.gridftp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

@SuppressWarnings("UnusedDeclaration")
public class ExecutionContext {

    private String testingHost;

    private String loneStarGridFTP;
    private String rangerGridFTP;
    private String trestlesGridFTP;

    private String gridFTPServerSource;
    private String sourceDataLocation;
    private String gridFTPServerDestination;
    private String destinationDataLocation;
    private String uploadingFilePath;

    public static final String PROPERTY_FILE = "airavata-myproxy-client.properties";

    public ExecutionContext() throws IOException {
        loadConfigurations();
    }

    private void loadConfigurations() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream propertyStream = classLoader.getResourceAsStream(PROPERTY_FILE);

        Properties properties = new Properties();
        if (propertyStream != null) {
            properties.load(propertyStream);

            String testingHost = properties.getProperty(GridFTPConstants.TESTINGHOST);

            String loneStarGridFtp = properties.getProperty(GridFTPConstants.LONESTARGRIDFTPEPR);
            String rangerGridFtp = properties.getProperty(GridFTPConstants.RANGERGRIDFTPEPR);
            String trestlesGridFtp = properties.getProperty(GridFTPConstants.TRESTLESGRIDFTPEPR);

            String gridFTPServerSource = properties.getProperty(GridFTPConstants.GRIDFTPSERVERSOURCE);
            String gridFTPSourcePath = properties.getProperty(GridFTPConstants.GRIDFTPSOURCEPATH);
            String gridFTPServerDestination = properties.getProperty(GridFTPConstants.GRIDFTPSERVERDEST);
            String gridFTPDestinationPath = properties.getProperty(GridFTPConstants.GRIDFTPDESTPATH);
            String gridFTPUploadingPath = properties.getProperty(GridFTPConstants.UPLOADING_FILE_PATH);

            if (testingHost != null) {
                this.testingHost = testingHost;
            }

            if (loneStarGridFtp != null) {
                this.loneStarGridFTP = loneStarGridFtp;
            }
            if (rangerGridFtp != null) {
                this.rangerGridFTP= rangerGridFtp;
            }
            if (trestlesGridFtp != null) {
                this.trestlesGridFTP = trestlesGridFtp;
            }

            if (gridFTPServerSource != null && !gridFTPServerSource.isEmpty()) {
                this.gridFTPServerSource = gridFTPServerSource;
            }
            if (gridFTPSourcePath != null && !gridFTPSourcePath.isEmpty()) {
                this.sourceDataLocation = gridFTPSourcePath;
            }
            if (gridFTPServerDestination != null && !gridFTPServerDestination.isEmpty()) {
                this.gridFTPServerDestination = gridFTPServerDestination;
            }
            if (gridFTPDestinationPath != null && !gridFTPDestinationPath.isEmpty()) {
                this.destinationDataLocation = gridFTPDestinationPath;
            }
            if (gridFTPUploadingPath != null && !gridFTPUploadingPath.isEmpty()) {
                this.uploadingFilePath = gridFTPUploadingPath;
            }

        }
    }

    public String getTestingHost() {
        return testingHost;
    }

    public void setTestingHost(String testingHost) {
        this.testingHost = testingHost;
    }

    public String getLoneStarGridFTP() {
        return loneStarGridFTP;
    }

    public void setLoneStarGridFTP(String loneStarGridFTP) {
        this.loneStarGridFTP = loneStarGridFTP;
    }

    public String getRangerGridFTP() {
        return rangerGridFTP;
    }

    public void setRangerGridFTP(String rangerGridFTP) {
        this.rangerGridFTP = rangerGridFTP;
    }

    public String getTrestlesGridFTP() {
        return trestlesGridFTP;
    }

    public void setTrestlesGridFTP(String trestlesGridFTP) {
        this.trestlesGridFTP = trestlesGridFTP;
    }

    public String getGridFTPServerSource() {
        return gridFTPServerSource;
    }

    public void setGridFTPServerSource(String gridFTPServerSource) {
        this.gridFTPServerSource = gridFTPServerSource;
    }

    public URI getSourceDataFileUri() throws URISyntaxException {
        String file = gridFTPServerSource + getSourceDataLocation();
        return new URI(file);
    }

    public URI getUploadingFilePathUri() throws URISyntaxException {
        String file = gridFTPServerSource + getUploadingFilePath();
        return new URI(file);
    }

    public String getUploadingFilePath() {
        return uploadingFilePath;
    }

    public void setUploadingFilePath(String uploadingFilePath) {
        this.uploadingFilePath = uploadingFilePath;
    }

    public String getSourceDataLocation() {
        return sourceDataLocation;
    }

    public void setSourceDataLocation(String sourceDataLocation) {
        this.sourceDataLocation = sourceDataLocation;
    }

    public String getGridFTPServerDestination() {
        return gridFTPServerDestination;
    }

    public void setGridFTPServerDestination(String gridFTPServerDestination) {
        this.gridFTPServerDestination = gridFTPServerDestination;
    }

    public String getDestinationDataLocation() {
        return destinationDataLocation;
    }

    public void setDestinationDataLocation(String destinationDataLocation) {
        this.destinationDataLocation = destinationDataLocation;
    }
}