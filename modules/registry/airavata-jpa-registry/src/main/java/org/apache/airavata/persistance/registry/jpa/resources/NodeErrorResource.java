package org.apache.airavata.persistance.registry.jpa.resources;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.model.Experiment_Data;
import org.apache.airavata.persistance.registry.jpa.model.Node_Error;
import org.apache.airavata.persistance.registry.jpa.model.Workflow_Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.security.Timestamp;
import java.util.List;

public class NodeErrorResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(NodeErrorResource.class);
    private ExperimentDataResource experimentDataResource;
    private WorkflowDataResource workflowDataResource;
    private String nodeID;
    private String gfacJobID;
    private String sourceType;
    private Timestamp errorTime;
    private String errorMsg;
    private String errorDes;
    private String errorCode;
    private int errorID;

    @Override
    public Resource create(ResourceType type) {
        logger.error("Unsupported resource type for node error resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(ResourceType type, Object name) {
        logger.error("Unsupported resource type for node error resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    @Override
    public Resource get(ResourceType type, Object name) {
        logger.error("Unsupported resource type for node error resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Resource> get(ResourceType type) {
        logger.error("Unsupported resource type for node error resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    @Override
    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();

        Node_Error node_error = new Node_Error();
        node_error.setNode_id(nodeID);
        Experiment_Data experiment_data = em.find(Experiment_Data.class, experimentDataResource.getExperimentID());
        node_error.setExperiment_data(experiment_data);
        Workflow_Data workflow_data = em.find(Workflow_Data.class, workflowDataResource.getWorkflowInstanceID());
        node_error.setWorkflow_Data(workflow_data);
        node_error.setError_code(errorCode);
        node_error.setError_date(errorTime);
        node_error.setError_des(errorDes);
        node_error.setError_msg(errorMsg);
        node_error.setSource_type(sourceType);
        node_error.setGfacJobID(gfacJobID);

        em.getTransaction().commit();
        em.close();
    }


    public ExperimentDataResource getExperimentDataResource() {
        return experimentDataResource;
    }

    public WorkflowDataResource getWorkflowDataResource() {
        return workflowDataResource;
    }

    public String getNodeID() {
        return nodeID;
    }

    public String getGfacJobID() {
        return gfacJobID;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Timestamp getErrorTime() {
        return errorTime;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public String getErrorDes() {
        return errorDes;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setExperimentDataResource(ExperimentDataResource experimentDataResource) {
        this.experimentDataResource = experimentDataResource;
    }

    public void setWorkflowDataResource(WorkflowDataResource workflowDataResource) {
        this.workflowDataResource = workflowDataResource;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    public void setGfacJobID(String gfacJobID) {
        this.gfacJobID = gfacJobID;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public void setErrorTime(Timestamp errorTime) {
        this.errorTime = errorTime;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public void setErrorDes(String errorDes) {
        this.errorDes = errorDes;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorID() {
        return errorID;
    }

    public void setErrorID(int errorID) {
        this.errorID = errorID;
    }
}
