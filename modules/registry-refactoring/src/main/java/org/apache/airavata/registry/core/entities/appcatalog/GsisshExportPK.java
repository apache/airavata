package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the gsissh_export database table.
 * 
 */
@Embeddable
public class GsisshExportPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="SUBMISSION_ID", insertable=false, updatable=false)
	private String submissionId;

	@Column(name="EXPORT")
	private String export;

	public GsisshExportPK() {
	}

	public String getSubmissionId() {
		return submissionId;
	}

	public void setSubmissionId(String submissionId) {
		this.submissionId = submissionId;
	}

	public String getExport() {
		return export;
	}

	public void setExport(String export) {
		this.export = export;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof GsisshExportPK)) {
			return false;
		}
		GsisshExportPK castOther = (GsisshExportPK)other;
		return 
			this.submissionId.equals(castOther.submissionId)
			&& this.export.equals(castOther.export);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.submissionId.hashCode();
		hash = hash * prime + this.export.hashCode();
		
		return hash;
	}
}