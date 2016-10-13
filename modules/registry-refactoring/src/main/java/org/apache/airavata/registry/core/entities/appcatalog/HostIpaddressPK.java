package appcatlog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the host_ipaddress database table.
 * 
 */
@Embeddable
public class HostIpaddressPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="RESOURCE_ID", insertable=false, updatable=false)
	private String resourceId;

	@Column(name="IP_ADDRESS")
	private String ipAddress;

	public HostIpaddressPK() {
	}
	public String getResourceId() {
		return this.resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public String getIpAddress() {
		return this.ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof HostIpaddressPK)) {
			return false;
		}
		HostIpaddressPK castOther = (HostIpaddressPK)other;
		return 
			this.resourceId.equals(castOther.resourceId)
			&& this.ipAddress.equals(castOther.ipAddress);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.resourceId.hashCode();
		hash = hash * prime + this.ipAddress.hashCode();
		
		return hash;
	}
}