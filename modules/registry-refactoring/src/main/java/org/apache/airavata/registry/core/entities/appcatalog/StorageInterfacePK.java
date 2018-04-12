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

import java.io.Serializable;

/**
 * The primary key class for the storage_interface database table.
 * 
 */
public class StorageInterfacePK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	private String storageResourceId;

	private String dataMovementInterfaceId;

	public StorageInterfacePK() {
	}

	public String getStorageResourceId() {
		return storageResourceId;
	}

	public void setStorageResourceId(String storageResourceId) {
		this.storageResourceId = storageResourceId;
	}

	public String getDataMovementInterfaceId() {
		return dataMovementInterfaceId;
	}

	public void setDataMovementInterfaceId(String dataMovementInterfaceId) {
		this.dataMovementInterfaceId = dataMovementInterfaceId;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof StorageInterfacePK)) {
			return false;
		}
		StorageInterfacePK castOther = (StorageInterfacePK)other;
		return 
			this.storageResourceId.equals(castOther.storageResourceId)
			&& this.dataMovementInterfaceId.equals(castOther.dataMovementInterfaceId);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.storageResourceId.hashCode();
		hash = hash * prime + this.dataMovementInterfaceId.hashCode();
		
		return hash;
	}
}