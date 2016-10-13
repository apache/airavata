package appcatlog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the data_storage_preference database table.
 * 
 */
@Embeddable
public class DataStoragePreferencePK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="GATEWAY_ID", insertable=false, updatable=false)
	private String gatewayId;

	@Column(name="STORAGE_RESOURCE_ID")
	private String storageResourceId;

	public DataStoragePreferencePK() {
	}
	public String getGatewayId() {
		return this.gatewayId;
	}
	public void setGatewayId(String gatewayId) {
		this.gatewayId = gatewayId;
	}
	public String getStorageResourceId() {
		return this.storageResourceId;
	}
	public void setStorageResourceId(String storageResourceId) {
		this.storageResourceId = storageResourceId;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof DataStoragePreferencePK)) {
			return false;
		}
		DataStoragePreferencePK castOther = (DataStoragePreferencePK)other;
		return 
			this.gatewayId.equals(castOther.gatewayId)
			&& this.storageResourceId.equals(castOther.storageResourceId);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.gatewayId.hashCode();
		hash = hash * prime + this.storageResourceId.hashCode();
		
		return hash;
	}
}