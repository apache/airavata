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
package org.apache.airavata.registry.core.entities.appcatalog;

import org.apache.airavata.model.data.movement.SecurityProtocol;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;


/**
 * The persistent class for the scp_data_movement database table.
 */
@Entity
@Table(name = "SCP_DATA_MOVEMENT")
public class ScpDataMovementEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "DATA_MOVEMENT_INTERFACE_ID")
    private String dataMovementInterfaceId;

    @Column(name = "ALTERNATIVE_SCP_HOSTNAME")
    private String alternativeSCPHostName;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "QUEUE_DESCRIPTION")
    private String queueDescription;

    @Column(name = "SECURITY_PROTOCOL")
    @Enumerated(EnumType.STRING)
    private SecurityProtocol securityProtocol;

    @Column(name = "SSH_PORT")
    private int sshPort;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    public ScpDataMovementEntity() {
    }

    public String getDataMovementInterfaceId() {
        return dataMovementInterfaceId;
    }

    public void setDataMovementInterfaceId(String dataMovementInterfaceId) {
        this.dataMovementInterfaceId = dataMovementInterfaceId;
    }

    public String getAlternativeSCPHostName() {
        return alternativeSCPHostName;
    }

    public void setAlternativeSCPHostName(String alternativeSCPHostName) {
        this.alternativeSCPHostName = alternativeSCPHostName;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getQueueDescription() {
        return queueDescription;
    }

    public void setQueueDescription(String queueDescription) {
        this.queueDescription = queueDescription;
    }

    public SecurityProtocol getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(SecurityProtocol securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public int getSshPort() {
        return sshPort;
    }

    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}