package org.apache.airavata.persistance.registry.jpa.resources;


import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.model.Node_Data;
import org.apache.airavata.persistance.registry.jpa.model.Node_DataPK;
import org.apache.airavata.persistance.registry.jpa.model.Workflow_Data;

import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.List;

public class NodeDataResource extends AbstractResource{
    private WorkflowDataResource workflowDataResource;
    private String nodeID;
    private String nodeType;
    private String inputs;
    private String outputs;
    private String status;
    private Timestamp startTime;
    private Timestamp lastUpdateTime;

    public WorkflowDataResource getWorkflowDataResource() {
        return workflowDataResource;
    }

    public String getNodeID() {
        return nodeID;
    }

    public String getNodeType() {
        return nodeType;
    }

    public String getInputs() {
        return inputs;
    }

    public String getOutputs() {
        return outputs;
    }

    public String getStatus() {
        return status;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public Timestamp getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setWorkflowDataResource(WorkflowDataResource workflowDataResource) {
        this.workflowDataResource = workflowDataResource;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public void setInputs(String inputs) {
        this.inputs = inputs;
    }

    public void setOutputs(String outputs) {
        this.outputs = outputs;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public void setLastUpdateTime(Timestamp lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Resource create(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    public void remove(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    public Resource get(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    public List<Resource> get(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        Node_Data existingNodeData = em.find(Node_Data.class, new Node_DataPK(workflowDataResource.getWorkflowInstanceID(), nodeID));
        em.close();

        em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Node_Data nodeData = new Node_Data();
        nodeData.setNode_id(nodeID);
        Workflow_Data workflow_data = em.find(Workflow_Data.class, workflowDataResource.getWorkflowInstanceID());
        nodeData.setWorkflow_Data(workflow_data);
        byte[] inputsByte = inputs.getBytes();
        nodeData.setInputs(inputsByte);
        byte[] outputsByte = outputs.getBytes();
        nodeData.setOutputs(outputsByte);
        nodeData.setNode_type(nodeType);
        nodeData.setLast_update_time(lastUpdateTime);
        nodeData.setStart_time(startTime);
        nodeData.setStatus(status);
        if(existingNodeData != null){
            existingNodeData.setInputs(inputsByte);
            existingNodeData.setOutputs(outputsByte);
            existingNodeData.setLast_update_time(lastUpdateTime);
            existingNodeData.setNode_type(nodeType);
            existingNodeData.setStart_time(startTime);
            existingNodeData.setStatus(status);
            nodeData = em.merge(existingNodeData);
        }  else {
            em.persist(nodeData);
        }
        em.getTransaction().commit();
        em.close();
    }
}
