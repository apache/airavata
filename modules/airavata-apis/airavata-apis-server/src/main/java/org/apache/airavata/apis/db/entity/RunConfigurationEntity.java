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
}
