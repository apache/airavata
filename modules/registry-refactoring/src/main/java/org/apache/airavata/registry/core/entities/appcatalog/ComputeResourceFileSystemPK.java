package appcatlog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the compute_resource_file_system database table.
 * 
 */
@Embeddable
public class ComputeResourceFileSystemPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="COMPUTE_RESOURCE_ID", insertable=false, updatable=false)
	private String computeResourceId;

	@Column(name="FILE_SYSTEM")
	private String fileSystem;

	public ComputeResourceFileSystemPK() {
	}
	public String getComputeResourceId() {
		return this.computeResourceId;
	}
	public void setComputeResourceId(String computeResourceId) {
		this.computeResourceId = computeResourceId;
	}
	public String getFileSystem() {
		return this.fileSystem;
	}
	public void setFileSystem(String fileSystem) {
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