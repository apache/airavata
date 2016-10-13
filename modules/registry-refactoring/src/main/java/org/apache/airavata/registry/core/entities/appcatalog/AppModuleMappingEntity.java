package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the app_module_mapping database table.
 * 
 */
@Entity
@Table(name="app_module_mapping")
public class AppModuleMappingEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private AppModuleMappingPK id;

	public AppModuleMappingEntity() {
	}

	public AppModuleMappingPK getId() {
		return id;
	}

	public void setId(AppModuleMappingPK id) {
		this.id = id;
	}
}