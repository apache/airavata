package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;

/**
 * The persistent class for the job_submission_interface database table.
 */
@Entity
@Table(name = "job_submission_interface")
public class JobSubmissionInterfaceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private JobSubmissionInterfacePK id;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "JOB_SUBMISSION_PROTOCOL")
    private String jobSubmissionProtocol;

    @Column(name = "PRIORITY_ORDER")
    private int priorityOrder;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    public JobSubmissionInterfaceEntity() {
    }

    public JobSubmissionInterfacePK getId() {
        return id;
    }

    public void setId(JobSubmissionInterfacePK id) {
        this.id = id;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getJobSubmissionProtocol() {
        return jobSubmissionProtocol;
    }

    public void setJobSubmissionProtocol(String jobSubmissionProtocol) {
        this.jobSubmissionProtocol = jobSubmissionProtocol;
    }

    public int getPriorityOrder() {
        return priorityOrder;
    }

    public void setPriorityOrder(int priorityOrder) {
        this.priorityOrder = priorityOrder;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}