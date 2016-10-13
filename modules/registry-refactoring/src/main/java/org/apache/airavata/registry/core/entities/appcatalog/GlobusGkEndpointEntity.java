package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the globus_gk_endpoint database table.
 * 
 */
@Entity
@Table(name="globus_gk_endpoint")
public class GlobusGkEndpoint implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private GlobusGkEndpointPK id;

	

	public GlobusGkEndpoint() {
	}

	public GlobusGkEndpointPK getId() {
		return id;
	}

	public void setId(GlobusGkEndpointPK id) {
		this.id = id;
	}
}