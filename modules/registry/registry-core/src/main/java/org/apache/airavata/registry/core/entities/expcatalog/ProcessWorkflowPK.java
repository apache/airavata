package org.apache.airavata.registry.core.entities.expcatalog;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

public class ProcessWorkflowPK implements Serializable {

    private String processId;
    private String workflowId;

    @Id
    @Column(name = "PROCESS_ID")
    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    @Id
    @Column(name = "WORKFLOW_ID")
    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcessWorkflowPK that = (ProcessWorkflowPK) o;

        return (getProcessId() != null ? getProcessId().equals(that.getProcessId()) : that.getProcessId() == null)
                && (getWorkflowId() != null ? getWorkflowId().equals(that.getWorkflowId()) : that.getWorkflowId() == null);
    }

    @Override
    public int hashCode() {
        int result = getProcessId() != null ? getProcessId().hashCode() : 0;
        result = 31 * result + (getWorkflowId() != null ? getWorkflowId().hashCode() : 0);
        return result;
    }
}
