package org.apache.airavata.apis.db.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import java.util.List;

@Entity
public class ExperimentEntity extends BaseEntity {

    @Column(name = "PROJECT_ID")
    String projectId;

    @Column(name = "GATEWAY_ID")
    String gatewayId;

    @Column(name = "EXPERIMENT_NAME")
    String experimentName;

    @Column(name = "CREATION_TIME")
    long creationTime;

    @Column(name = "DESCRIPTION")
    String description;

    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL)
    List<RunConfigurationEntity> runConfigs;

    public String getExperimentId() {
        return getId().toString();
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<RunConfigurationEntity> getRunConfigs() {
        return runConfigs;
    }

    public void setRunConfigs(List<RunConfigurationEntity> runConfigs) {
        this.runConfigs = runConfigs;
        if (runConfigs != null) {
            for (RunConfigurationEntity runConfig : runConfigs) {
                runConfig.setExperiment(this);
            }
        }

    }
}
