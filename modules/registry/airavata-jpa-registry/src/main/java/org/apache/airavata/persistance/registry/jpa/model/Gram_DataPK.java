package org.apache.airavata.persistance.registry.jpa.model;


public class Gram_DataPK {
	private String workflow_Data;
	private String node_id;

	public Gram_DataPK() {
		;
	}

    public Gram_DataPK(String workflow_Data, String node_id) {
        this.workflow_Data = workflow_Data;
        this.node_id = node_id;
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
