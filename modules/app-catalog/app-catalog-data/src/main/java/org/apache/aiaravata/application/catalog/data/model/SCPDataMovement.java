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

package org.apache.aiaravata.application.catalog.data.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "SCP_DATAMOVEMENT")
public class SCPDataMovement implements Serializable {
    @Column(name = "RESOURCE_ID")
    private String resourceID;
    @Id
    @Column(name = "DATA_MOVE_ID")
    private String dataMoveID;
    @Column(name = "SECURITY_PROTOCOL")
    private String securityProtocol;
    @Column(name = "SSH_PORT")
    private int sshPort;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "RESOURCE_ID")
    private ComputeResource computeResource;

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public String getDataMoveID() {
        return dataMoveID;
    }

    public void setDataMoveID(String dataMoveID) {
        this.dataMoveID = dataMoveID;
    }

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public int getSshPort() {
        return sshPort;
    }

    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    public ComputeResource getComputeResource() {
        return computeResource;
    }

    public void setComputeResource(ComputeResource computeResource) {
        this.computeResource = computeResource;
    }
}
