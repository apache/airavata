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

import org.apache.openjpa.persistence.jdbc.ForeignKey;
import org.apache.openjpa.persistence.jdbc.ForeignKeyAction;

import javax.persistence.*;
import java.io.Serializable;


/**
 * The persistent class for the library_apend_path database table.
 * 
 */
@Entity
@Table(name="LIBRARY_PREPAND_PATH")
@IdClass(LibraryPrependPathPK.class)
public class LibraryPrependPathEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="DEPLOYMENT_ID")
	private String deploymentId;

	@Column(name="VALUE")
	private String value;

	@Id
	@Column(name="NAME")
	private String name;

	@ManyToOne(targetEntity = ApplicationDeploymentEntity.class, cascade = CascadeType.MERGE)
	@JoinColumn(name = "DEPLOYMENT_ID")
	@ForeignKey(deleteAction = ForeignKeyAction.CASCADE)
	private ApplicationDeploymentEntity applicationDeployment;

	public LibraryPrependPathEntity() {
	}

	public String getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ApplicationDeploymentEntity getApplicationDeployment() {
		return applicationDeployment;
	}

	public void setApplicationDeployment(ApplicationDeploymentEntity applicationDeployment) {
		this.applicationDeployment = applicationDeployment;
	}

}