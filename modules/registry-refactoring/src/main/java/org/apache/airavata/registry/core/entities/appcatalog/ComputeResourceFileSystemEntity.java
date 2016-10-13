package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the compute_resource_file_system database table.
 * 
 */
@Entity
@Table(name="compute_resource_file_system")
public class ComputeResourceFileSystemEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private ComputeResourceFileSystemPK id;

	@Column(name="PATH")
	private String path;


	public ComputeResourceFileSystemEntity() {
	}

	public ComputeResourceFileSystemPK getId() {
		return id;
	}

	public void setId(ComputeResourceFileSystemPK id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}