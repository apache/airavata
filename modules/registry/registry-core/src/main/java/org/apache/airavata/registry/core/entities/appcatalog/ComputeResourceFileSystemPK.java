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

import java.io.Serializable;

/**
 * The primary key class for the compute_resource_file_system database table.
 * 
 */
public class ComputeResourceFileSystemPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	private String computeResourceId;
	private FileSystems fileSystem;

	public ComputeResourceFileSystemPK() {
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

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ComputeResourceFileSystemPK)) {
			return false;
		}
		ComputeResourceFileSystemPK castOther = (ComputeResourceFileSystemPK)other;
		return 
			this.computeResourceId.equals(castOther.computeResourceId)
			&& this.fileSystem.equals(castOther.fileSystem);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.computeResourceId.hashCode();
		hash = hash * prime + this.fileSystem.hashCode();
		
		return hash;
	}
}