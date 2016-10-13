package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the batch_queue database table.
 * 
 */
@Embeddable
public class BatchQueuePK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="COMPUTE_RESOURCE_ID", insertable=false, updatable=false)
	private String computeResourceId;

	@Column(name="QUEUE_NAME")
	private String queueName;

	public BatchQueuePK() {
	}

	public String getComputeResourceId() {
		return computeResourceId;
	}

	public void setComputeResourceId(String computeResourceId) {
		this.computeResourceId = computeResourceId;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BatchQueuePK)) {
			return false;
		}
		BatchQueuePK castOther = (BatchQueuePK)other;
		return 
			this.computeResourceId.equals(castOther.computeResourceId)
			&& this.queueName.equals(castOther.queueName);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.computeResourceId.hashCode();
		hash = hash * prime + this.queueName.hashCode();
		
		return hash;
	}
}