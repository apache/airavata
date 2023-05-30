package org.apache.airavata.apis.db.entity;

import org.apache.airavata.apis.db.entity.backend.ComputeBackendEntity;
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
}
