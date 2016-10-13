package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The persistent class for the host_alias database table.
 * 
 */
@Entity
@Table(name = "host_alias")
public class HostAlias implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private HostAliasPK id;

	@Column(name = "RESOURCE_ID")
	private String resourceId;

	public HostAlias() {
	}

	public HostAliasPK getId() {
		return id;
	}

	public void setId(HostAliasPK id) {
		this.id = id;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
}