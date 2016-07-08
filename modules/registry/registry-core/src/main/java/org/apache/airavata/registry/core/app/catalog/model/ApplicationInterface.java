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

package org.apache.airavata.registry.core.app.catalog.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "APPLICATION_INTERFACE")
public class ApplicationInterface implements Serializable {
    @Id
    @Column(name = "INTERFACE_ID")
    private String interfaceID;
    @Column(name = "APPLICATION_NAME")
    private String appName;
    @Column(name = "APPLICATION_DESCRIPTION")
    private String appDescription;
    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;
    @Column(name = "GATEWAY_ID")
    private String gatewayId;
    @Column(name = "ARCHIVE_WORKING_DIRECTORY")
    private boolean archiveWorkingDirectory;
    @Column(name = "HAS_OPTIONAL_FILE_INPUTS")
    private boolean hasOptionalFileInputs;
    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public boolean isArchiveWorkingDirectory() {
        return archiveWorkingDirectory;
    }

    public void setArchiveWorkingDirectory(boolean archiveWorkingDirectory) {
        this.archiveWorkingDirectory = archiveWorkingDirectory;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }


    public String getInterfaceID() {
        return interfaceID;
    }

    public void setInterfaceID(String interfaceID) {
        this.interfaceID = interfaceID;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppDescription() {
        return appDescription;
    }

    public void setAppDescription(String appDescription) {
        this.appDescription = appDescription;
    }

    public boolean isHasOptionalFileInputs() {
        return hasOptionalFileInputs;
    }

    public void setHasOptionalFileInputs(boolean hasOptionalFileInputs) {
        this.hasOptionalFileInputs = hasOptionalFileInputs;
    }
}
