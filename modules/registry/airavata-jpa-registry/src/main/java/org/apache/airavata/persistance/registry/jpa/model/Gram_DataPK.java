package org.apache.airavata.persistance.registry.jpa.model;


public class Gram_DataPK {
	private String workflow_instanceID;
	private String node_id;

	public Gram_DataPK() {
		;
	}

    public Gram_DataPK(String workflow_instanceID, String node_id) {
        this.workflow_instanceID = workflow_instanceID;
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

    public String getWorkflow_instanceID() {
        return workflow_instanceID;
    }

    public void setWorkflow_instanceID(String workflow_instanceID) {
        this.workflow_instanceID = workflow_instanceID;
    }

    public String getNode_id() {
		return node_id;
	}

	public void setNode_id(String node_id) {
		this.node_id = node_id;
	}
}
