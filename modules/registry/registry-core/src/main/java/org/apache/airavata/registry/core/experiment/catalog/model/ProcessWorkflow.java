package org.apache.airavata.registry.core.experiment.catalog.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "PROCESS_WORKFLOW")
@IdClass(ProcessWorkflowPK.class)
public class ProcessWorkflow {

    private String processId;
    private String workflowId;
    private Timestamp creationTime;
    private String type;
    private Process process;

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

    @Column(name = "CREATION_TIME")
    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    @Column(name = "TYPE")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @ManyToOne
    @JoinColumn(name = "PROCESS_ID", referencedColumnName = "PROCESS_ID")
    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }
}
