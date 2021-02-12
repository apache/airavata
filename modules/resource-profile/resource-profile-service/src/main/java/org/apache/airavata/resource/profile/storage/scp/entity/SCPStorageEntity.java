/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.airavata.resource.profile.storage.scp.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "SCP_STORAGE_ENTITY")
public class SCPStorageEntity {

    @Id
    @Column(name = "SCP_STORAGE_ID")
    private String scpStorageId;

    @Column(name = "HOST_NAME")
    private String hostName;

    @Column(name = "PORT")
    private String port;

    public String getScpStorageId() {
        return scpStorageId;
    }

    public SCPStorageEntity setScpStorageId(String scpStorageId) {
        this.scpStorageId = scpStorageId;
        return this;
    }

    public String getHostName() {
        return hostName;
    }

    public SCPStorageEntity setHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    public String getPort() {
        return port;
    }

    public SCPStorageEntity setPort(String port) {
        this.port = port;
        return this;
    }
}
