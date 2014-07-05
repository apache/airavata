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

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.openjpa.persistence.DataCache;

@DataCache
@Entity
@Table(name = "GRIDFTP_ENDPOINT")
@IdClass(GridftpEndpoint_PK.class)
public class GridftpEndpoint implements Serializable {
	
	@Id
	@Column(name = "ENDPOINT")
	private String endpoint;
	
	@Id
	@Column(name = "DATA_MOVEMENT_INTERFACE_ID")
	private String dataMovementInterfaceId;
	
	@ManyToOne(cascade= CascadeType.MERGE)
	@JoinColumn(name = "DATA_MOVEMENT_INTERFACE_ID")
	private GridftpDataMovement gridftpDataMovement;
	
	public String getEndpoint() {
		return endpoint;
	}
	
	public String getDataMovementInterfaceId() {
		return dataMovementInterfaceId;
	}
	
	public GridftpDataMovement getGridftpDataMovement() {
		return gridftpDataMovement;
	}
	
	public void setEndpoint(String endpoint) {
		this.endpoint=endpoint;
	}
	
	public void setDataMovementInterfaceId(String dataMovementInterfaceId) {
		this.dataMovementInterfaceId=dataMovementInterfaceId;
	}
	
	public void setGridftpDataMovement(GridftpDataMovement gridftpDataMovement) {
		this.gridftpDataMovement=gridftpDataMovement;
	}
}