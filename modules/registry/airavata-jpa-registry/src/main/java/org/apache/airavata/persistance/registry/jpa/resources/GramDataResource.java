package org.apache.airavata.persistance.registry.jpa.resources;


import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.model.Gram_Data;
import org.apache.airavata.persistance.registry.jpa.model.Gram_DataPK;
import org.apache.airavata.persistance.registry.jpa.model.Node_Data;
import org.apache.airavata.persistance.registry.jpa.model.Workflow_Data;

import javax.persistence.EntityManager;
import java.util.List;

public class GramDataResource extends AbstractResource{
    private WorkflowDataResource workflowDataResource;
    private String nodeID;
    private String rsl;
    private String invokedHost;
    private String localJobID;

    public String getNodeID() {
        return nodeID;
    }

    public String getRsl() {
        return rsl;
    }

    public String getInvokedHost() {
        return invokedHost;
    }

    public String getLocalJobID() {
        return localJobID;
    }

    public WorkflowDataResource getWorkflowDataResource() {
        return workflowDataResource;
    }

    public void setWorkflowDataResource(WorkflowDataResource workflowDataResource) {
        this.workflowDataResource = workflowDataResource;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    public void setRsl(String rsl) {
        this.rsl = rsl;
    }

    public void setInvokedHost(String invokedHost) {
        this.invokedHost = invokedHost;
    }

    public void setLocalJobID(String localJobID) {
        this.localJobID = localJobID;
    }

    @Override
    public Resource create(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Resource get(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Resource> get(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        Gram_Data existingGramData = em.find(Gram_Data.class, new Gram_DataPK(workflowDataResource.getWorkflowInstanceID(), nodeID));
        em.close();

        em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Gram_Data gramData = new Gram_Data();
        gramData.setNode_id(nodeID);
        Workflow_Data workflow_data = em.find(Workflow_Data.class, workflowDataResource.getWorkflowInstanceID());
        gramData.setWorkflow_Data(workflow_data);
        gramData.setNode_id(nodeID);
        gramData.setInvoked_host(invokedHost);
        gramData.setLocal_Job_ID(localJobID);
        gramData.setRsl(rsl);
        if(existingGramData != null){
            existingGramData.setInvoked_host(invokedHost);
            existingGramData.setLocal_Job_ID(localJobID);
            existingGramData.setRsl(rsl);
            gramData = em.merge(existingGramData);
        }  else {
            em.persist(gramData);
        }
        em.getTransaction().commit();
        em.close();
    }
}
