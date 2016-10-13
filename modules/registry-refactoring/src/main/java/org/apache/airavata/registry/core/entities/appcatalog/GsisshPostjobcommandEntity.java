package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The persistent class for the gsissh_postjobcommand database table.
 * 
 */
@Entity
@Table(name = "gsissh_postjobcommand")
public class GsisshPostjobcommandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private GsisshPostjobcommandPK id;

	public GsisshPostjobcommandEntity() {
	}

	public GsisshPostjobcommandPK getId() {
		return id;
	}

	public void setId(GsisshPostjobcommandPK id) {
		this.id = id;
	}
}