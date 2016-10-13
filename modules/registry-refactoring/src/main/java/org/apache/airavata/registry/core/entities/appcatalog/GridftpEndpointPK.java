package appcatlog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the gridftp_endpoint database table.
 * 
 */
@Embeddable
public class GridftpEndpointPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="DATA_MOVEMENT_INTERFACE_ID", insertable=false, updatable=false)
	private String dataMovementInterfaceId;

	private String endpoint;

	public GridftpEndpointPK() {
	}
	public String getDataMovementInterfaceId() {
		return this.dataMovementInterfaceId;
	}
	public void setDataMovementInterfaceId(String dataMovementInterfaceId) {
		this.dataMovementInterfaceId = dataMovementInterfaceId;
	}
	public String getEndpoint() {
		return this.endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof GridftpEndpointPK)) {
			return false;
		}
		GridftpEndpointPK castOther = (GridftpEndpointPK)other;
		return 
			this.dataMovementInterfaceId.equals(castOther.dataMovementInterfaceId)
			&& this.endpoint.equals(castOther.endpoint);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.dataMovementInterfaceId.hashCode();
		hash = hash * prime + this.endpoint.hashCode();
		
		return hash;
	}
}