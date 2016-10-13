package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * The persistent class for the resource_job_manager database table.
 */
@Entity
@Table(name = "resource_job_manager")
public class ResourceJobManager implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "RESOURCE_JOB_MANAGER_ID")
    private String resourceJobManagerId;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "JOB_MANAGER_BIN_PATH")
    private String jobManagerBinPath;

    @Column(name = "PUSH_MONITORING_ENDPOINT")
    private String pushMonitoringEndpoint;

    @Column(name = "RESOURCE_JOB_MANAGER_TYPE")
    private String resourceJobManagerType;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    public ResourceJobManager() {
    }

    public String getResourceJobManagerId() {
        return resourceJobManagerId;
    }

    public void setResourceJobManagerId(String resourceJobManagerId) {
        this.resourceJobManagerId = resourceJobManagerId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getJobManagerBinPath() {
        return jobManagerBinPath;
    }

    public void setJobManagerBinPath(String jobManagerBinPath) {
        this.jobManagerBinPath = jobManagerBinPath;
    }

    public String getPushMonitoringEndpoint() {
        return pushMonitoringEndpoint;
    }

    public void setPushMonitoringEndpoint(String pushMonitoringEndpoint) {
        this.pushMonitoringEndpoint = pushMonitoringEndpoint;
    }

    public String getResourceJobManagerType() {
        return resourceJobManagerType;
    }

    public void setResourceJobManagerType(String resourceJobManagerType) {
        this.resourceJobManagerType = resourceJobManagerType;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}