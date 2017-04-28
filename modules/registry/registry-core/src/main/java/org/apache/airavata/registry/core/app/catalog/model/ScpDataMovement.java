/**
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
 */
package org.apache.airavata.registry.core.app.catalog.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "SCP_DATA_MOVEMENT")
public class ScpDataMovement implements Serializable {
	
	@Column(name = "QUEUE_DESCRIPTION")
	private String queueDescription;
	
	@Id
	@Column(name = "DATA_MOVEMENT_INTERFACE_ID")
	private String dataMovementInterfaceId;
	
	@Column(name = "SECURITY_PROTOCOL")
	private String securityProtocol;
	
	@Column(name = "ALTERNATIVE_SCP_HOSTNAME")
	private String alternativeScpHostname;
	
	@Column(name = "SSH_PORT")
	private int sshPort;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

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


    public String getQueueDescription() {
		return queueDescription;
	}
	
	public String getDataMovementInterfaceId() {
		return dataMovementInterfaceId;
	}
	
	public String getSecurityProtocol() {
		return securityProtocol;
	}
	
	public String getAlternativeScpHostname() {
		return alternativeScpHostname;
	}
	
	public int getSshPort() {
		return sshPort;
	}
	
	public void setQueueDescription(String queueDescription) {
		this.queueDescription=queueDescription;
	}
	
	public void setDataMovementInterfaceId(String dataMovementInterfaceId) {
		this.dataMovementInterfaceId=dataMovementInterfaceId;
	}
	
	public void setSecurityProtocol(String securityProtocol) {
		this.securityProtocol=securityProtocol;
	}
	
	public void setAlternativeScpHostname(String alternativeScpHostname) {
		this.alternativeScpHostname=alternativeScpHostname;
	}
	
	public void setSshPort(int sshPort) {
		this.sshPort=sshPort;
	}
}
