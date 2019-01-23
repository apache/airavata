package org.apache.airavata.registry.core.experiment.catalog.resources;

import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.model.Job;
import org.apache.airavata.registry.core.experiment.catalog.model.JobPK;
import org.apache.airavata.registry.core.experiment.catalog.model.ProcessWorkflow;
import org.apache.airavata.registry.core.experiment.catalog.model.ProcessWorkflowPK;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.List;

public class ProcessWorkflowResource extends AbstractExpCatResource {

    private static final Logger logger = LoggerFactory.getLogger(ProcessWorkflowResource.class);

    private String processId;
    private String workflowId;
    private Timestamp creationTime;
    private String type;

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public ExperimentCatResource create(ResourceType type) throws RegistryException, RegistryException {
        throw new UnsupportedOperationException("Create operation is not supported for process workflow resource");
    }

    @Override
    public void remove(ResourceType type, Object name) throws RegistryException {
        throw new UnsupportedOperationException("Remove operation is not supported for process workflow resource");
    }

    @Override
    public ExperimentCatResource get(ResourceType type, Object name) throws RegistryException {
        throw new UnsupportedOperationException("Get operation is not supported for process workflow resource");
    }

    @Override
    public List<ExperimentCatResource> get(ResourceType type) throws RegistryException {
        throw new UnsupportedOperationException("List operation is not supported for process workflow resource");
    }

    @Override
    public void save() throws RegistryException {
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            ProcessWorkflowPK pwPK = new ProcessWorkflowPK();
            pwPK.setProcessId(processId);
            pwPK.setWorkflowId(workflowId);
            ProcessWorkflow existingPW = em.find(ProcessWorkflow.class, pwPK);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            ProcessWorkflow pw;
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            if(existingPW == null){
                pw = new ProcessWorkflow();
            }else {
                pw = existingPW;
            }
            pw.setProcessId(processId);
            pw.setWorkflowId(workflowId);
            if (creationTime != null) {
                pw.setCreationTime(creationTime);
            }

            if (type != null) {
                pw.setType(type);
            }

            if (existingPW == null){
                em.persist(pw);
            }else {
                em.merge(pw);
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }
}
