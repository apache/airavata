package appcatlog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the compute_resource_preference database table.
 * 
 */
@Embeddable
public class ComputeResourcePreferencePK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="GATEWAY_ID", insertable=false, updatable=false)
	private String gatewayId;

	@Column(name="RESOURCE_ID", insertable=false, updatable=false)
	private String resourceId;

	public ComputeResourcePreferencePK() {
	}
	public String getGatewayId() {
		return this.gatewayId;
	}
	public void setGatewayId(String gatewayId) {
		this.gatewayId = gatewayId;
	}
	public String getResourceId() {
		return this.resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ComputeResourcePreferencePK)) {
			return false;
		}
		ComputeResourcePreferencePK castOther = (ComputeResourcePreferencePK)other;
		return 
			this.gatewayId.equals(castOther.gatewayId)
			&& this.resourceId.equals(castOther.resourceId);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.gatewayId.hashCode();
		hash = hash * prime + this.resourceId.hashCode();
		
		return hash;
	}
}