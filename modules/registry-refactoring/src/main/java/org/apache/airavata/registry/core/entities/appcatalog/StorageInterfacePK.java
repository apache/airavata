package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the storage_interface database table.
 * 
 */
@Embeddable
public class StorageInterfacePK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="STORAGE_RESOURCE_ID", insertable=false, updatable=false)
	private String storageResourceId;

	@Column(name="DATA_MOVEMENT_INTERFACE_ID")
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