package org.apache.airavata.apis.db.entity;

import org.apache.airavata.apis.db.entity.backend.ComputeBackendEntity;
import org.apache.airavata.apis.db.entity.backend.EC2BackendEntity;
import org.apache.airavata.apis.db.entity.backend.LocalBackendEntity;
import org.apache.airavata.apis.db.entity.backend.ServerBackendEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import java.util.List;

@Entity
public class RunConfigurationEntity {

    @Id
    @Column(name = "RUN_CONFIG_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String runConfigId;

    @OneToOne
    @JoinColumn(name = "backend_id")
    ComputeBackendEntity computeBackend;

    @OneToOne
    @JoinColumn(name = "app_runner_id")
    ApplicationRunInfoEntity appRunInfo;

    @OneToMany(mappedBy = "runConfiguration")
    List<DataMovementConfigurationEntity> dataMovementConfigs;

    @ManyToOne
    @JoinColumn(name = "experiment_id")
    ExperimentEntity experiment;

    public String getRunConfigId() {
        return runConfigId;
    }

    public void setRunConfigId(String runConfigId) {
        this.runConfigId = runConfigId;
    }


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
    }

    public ComputeBackendEntity getComputeBackend() {
        return computeBackend;
    }

    public void setComputeBackend(ComputeBackendEntity computeBackend) {
        this.computeBackend = computeBackend;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((runConfigId == null) ? 0 : runConfigId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RunConfigurationEntity other = (RunConfigurationEntity) obj;
        if (runConfigId == null) {
            if (other.runConfigId != null)
                return false;
        } else if (!runConfigId.equals(other.runConfigId))
            return false;
        return true;
    }

}
