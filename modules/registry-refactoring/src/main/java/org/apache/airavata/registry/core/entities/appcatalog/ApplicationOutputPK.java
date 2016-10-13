package appcatlog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the application_output database table.
 * 
 */
@Embeddable
public class ApplicationOutputPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="INTERFACE_ID", insertable=false, updatable=false)
	private String interfaceId;

	@Column(name="OUTPUT_KEY")
	private String outputKey;

	public ApplicationOutputPK() {
	}
	public String getInterfaceId() {
		return this.interfaceId;
	}
	public void setInterfaceId(String interfaceId) {
		this.interfaceId = interfaceId;
	}
	public String getOutputKey() {
		return this.outputKey;
	}
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ApplicationOutputPK)) {
			return false;
		}
		ApplicationOutputPK castOther = (ApplicationOutputPK)other;
		return 
			this.interfaceId.equals(castOther.interfaceId)
			&& this.outputKey.equals(castOther.outputKey);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.interfaceId.hashCode();
		hash = hash * prime + this.outputKey.hashCode();
		
		return hash;
	}
}