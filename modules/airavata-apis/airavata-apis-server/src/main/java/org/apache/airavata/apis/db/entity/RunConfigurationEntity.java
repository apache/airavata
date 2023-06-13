package org.apache.airavata.apis.db.entity;

import org.apache.airavata.apis.db.entity.backend.ComputeBackendEntity;
import org.apache.airavata.apis.db.entity.backend.EC2BackendEntity;
import org.apache.airavata.apis.db.entity.backend.LocalBackendEntity;
import org.apache.airavata.apis.db.entity.backend.ServerBackendEntity;

import javax.persistence.*;

import java.util.List;

@Entity
public class RunConfigurationEntity extends BaseEntity {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "backend_id")
    ComputeBackendEntity computeBackend;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "app_runner_id")
    ApplicationRunInfoEntity appRunInfo;

    @OneToMany(mappedBy = "runConfiguration", cascade = CascadeType.ALL)
    List<DataMovementConfigurationEntity> dataMovementConfigs;

    @ManyToOne
    @JoinColumn(name = "experiment_id", nullable = false)
    ExperimentEntity experiment;

    public ApplicationRunInfoEntity getAppRunInfo() {
        return appRunInfo;
    }

    public void setAppRunInfo(ApplicationRunInfoEntity appRunInfo) {
        this.appRunInfo = appRunInfo;
    }

    public List<DataMovementConfigurationEntity> getDataMovementConfigs() {
        return dataMovementConfigs;
    }

    public void setDataMovementConfigs(List<DataMovementConfigurationEntity> dataMovementConfigs) {
        this.dataMovementConfigs = dataMovementConfigs;
        for (DataMovementConfigurationEntity dataMovementConfig : dataMovementConfigs) {
            dataMovementConfig.setRunConfiguration(this);
        }
    }

    public ComputeBackendEntity getComputeBackend() {
        return computeBackend;
    }

    public void setComputeBackend(ComputeBackendEntity computeBackend) {
        this.computeBackend = computeBackend;
    }

    public ExperimentEntity getExperiment() {
        return experiment;
    }

    public void setExperiment(ExperimentEntity experiment) {
        this.experiment = experiment;
    }

    /*
     * Helper getters/setters for mapping from/to protobuf messages
     */

    public ServerBackendEntity getServer() {
        return computeBackend instanceof ServerBackendEntity ? (ServerBackendEntity) computeBackend : null;
    }

    public void setServer(ServerBackendEntity serverBackendEntity) {
        this.computeBackend = serverBackendEntity;
    }

    public EC2BackendEntity getEc2() {
        return computeBackend instanceof EC2BackendEntity ? (EC2BackendEntity) computeBackend : null;
    }

    public void setEc2(EC2BackendEntity ec2BackendEntity) {
        this.computeBackend = ec2BackendEntity;
    }

    public LocalBackendEntity getLocal() {
        return computeBackend instanceof LocalBackendEntity ? (LocalBackendEntity) computeBackend : null;
    }

    public void setLocal(LocalBackendEntity localBackendEntity) {
        this.computeBackend = localBackendEntity;
    }
}
