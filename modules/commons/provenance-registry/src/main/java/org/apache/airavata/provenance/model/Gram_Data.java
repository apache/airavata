package org.apache.airavata.provenance.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@IdClass(Gram_DataPK.class)
public class Gram_Data {

	@Id
	@ManyToOne()
	@JoinColumn(name = "workflow_instanceID")
	private Workflow_Data workflow_Data;

	@Id
	private String node_id;

	private String rsl;
	private String invoked_host;

	public Workflow_Data getWorkflow_Data() {
		return workflow_Data;
	}

	public void setWorkflow_Data(Workflow_Data workflow_Data) {
		this.workflow_Data = workflow_Data;
	}

	public String getNode_id() {
		return node_id;
	}

	public void setNode_id(String node_id) {
		this.node_id = node_id;
	}

	public String getRsl() {
		return rsl;
	}

	public void setRsl(String rsl) {
		this.rsl = rsl;
	}

	public String getInvoked_host() {
		return invoked_host;
	}

	public void setInvoked_host(String invoked_host) {
		this.invoked_host = invoked_host;
	}

}

class Gram_DataPK {
	private String workflow_Data;
	private String node_id;

	public Gram_DataPK() {
		;
	}

	@Override
	public boolean equals(Object o) {
		return false;
	}

	@Override
	public int hashCode() {
		return 1;
	}

	public String getWorkflow_Data() {
		return workflow_Data;
	}

	public void setWorkflow_Data(String workflow_Data) {
		this.workflow_Data = workflow_Data;
	}

	public String getNode_id() {
		return node_id;
	}

	public void setNode_id(String node_id) {
		this.node_id = node_id;
	}
}