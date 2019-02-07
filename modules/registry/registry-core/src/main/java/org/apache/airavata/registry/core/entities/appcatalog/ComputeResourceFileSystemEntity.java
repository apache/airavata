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

import org.apache.airavata.model.appcatalog.computeresource.FileSystems;

import javax.persistence.*;
import java.io.Serializable;


/**
 * The persistent class for the compute_resource_file_system database table.
 * 
 */
@Entity
@Table(name="COMPUTE_RESOURCE_FILE_SYSTEM")
@IdClass(ComputeResourceFileSystemPK.class)
public class ComputeResourceFileSystemEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name="COMPUTE_RESOURCE_ID")
	@Id
	private String computeResourceId;

	@Column(name="FILE_SYSTEM")
	@Id
	@Enumerated(EnumType.STRING)
	private FileSystems fileSystem;

	@ManyToOne(cascade= CascadeType.MERGE)
	@JoinColumn(name = "COMPUTE_RESOURCE_ID")
	private ComputeResourceEntity computeResource;

	@Column(name="PATH")
	private String path;


	public ComputeResourceFileSystemEntity() {
	}

	public String getComputeResourceId() {
		return computeResourceId;
	}

	public void setComputeResourceId(String computeResourceId) {
		this.computeResourceId = computeResourceId;
	}

	public FileSystems getFileSystem() {
		return fileSystem;
	}

	public void setFileSystem(FileSystems fileSystem) {
		this.fileSystem = fileSystem;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public ComputeResourceEntity getComputeResource() {
		return computeResource;
	}

	public void setComputeResource(ComputeResourceEntity computeResource) {
		this.computeResource = computeResource;
	}
}