package appcatlog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the app_module_mapping database table.
 * 
 */
@Embeddable
public class AppModuleMappingPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="INTERFACE_ID", insertable=false, updatable=false)
	private String interfaceId;

	@Column(name="MODULE_ID", insertable=false, updatable=false)
	private String moduleId;

	public AppModuleMappingPK() {
	}
	public String getInterfaceId() {
		return this.interfaceId;
	}
	public void setInterfaceId(String interfaceId) {
		this.interfaceId = interfaceId;
	}
	public String getModuleId() {
		return this.moduleId;
	}
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AppModuleMappingPK)) {
			return false;
		}
		AppModuleMappingPK castOther = (AppModuleMappingPK)other;
		return 
			this.interfaceId.equals(castOther.interfaceId)
			&& this.moduleId.equals(castOther.moduleId);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.interfaceId.hashCode();
		hash = hash * prime + this.moduleId.hashCode();
		
		return hash;
	}
}