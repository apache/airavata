package org.apache.airavata.apis.db.entity;

import org.apache.airavata.apis.db.entity.backend.EC2BackendEntity;
import org.apache.airavata.apis.db.entity.backend.LocalBackendEntity;
import org.apache.airavata.apis.db.entity.backend.ServerBackendEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.List;

@Entity
public class RunConfigurationEntity {

    @Id
    @Column(name = "RUN_CONFIG_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String runConfigId;
    ServerBackendEntity server;
    EC2BackendEntity ec2;
    LocalBackendEntity local;
    ApplicationRunInfoEntity appRunInfo;
    List<DataMovementConfigurationEntity> dataMovementConfigs;

    public String getRunConfigId() {
        return runConfigId;
    }

    public void setRunConfigId(String runConfigId) {
        this.runConfigId = runConfigId;
    }

    public ServerBackendEntity getServer() {
        return server;
    }

    public void setServer(ServerBackendEntity server) {
        this.server = server;
    }

    public EC2BackendEntity getEc2() {
        return ec2;
    }

    public void setEc2(EC2BackendEntity ec2) {
        this.ec2 = ec2;
    }

    public LocalBackendEntity getLocal() {
        return local;
    }

    public void setLocal(LocalBackendEntity local) {
        this.local = local;
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
