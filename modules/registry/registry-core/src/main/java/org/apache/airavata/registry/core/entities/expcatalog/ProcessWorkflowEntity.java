package org.apache.airavata.registry.core.entities.expcatalog;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "PROCESS_WORKFLOW")
@IdClass(ProcessWorkflowPK.class)
public class ProcessWorkflowEntity {

    @Id
    @Column(name = "PROCESS_ID")
    private String processId;

    @Id
    @Column(name = "WORKFLOW_ID")
    private String workflowId;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "TYPE")
    private String type;

    @ManyToOne(targetEntity = ProcessEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID", referencedColumnName = "PROCESS_ID", nullable = false, updatable = false)
    private ProcessEntity process;

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

    public ProcessEntity getProcess() {
        return process;
    }

    public void setProcess(ProcessEntity process) {
        this.process = process;
    }
}
