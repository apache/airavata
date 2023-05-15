package org.apache.airavata.apis.db.entity;

import org.apache.airavata.apis.db.entity.application.ApplicationEntity;
import org.apache.airavata.apis.db.entity.application.runners.DockerRunnerEntity;
import org.apache.airavata.apis.db.entity.application.runners.SlurmRunnerEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class ApplicationRunInfoEntity {

    @Id
    @Column(name = "APP_RUNNER_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String appRunnerId;

    private ApplicationEntity application;

    DockerRunnerEntity dockerRunner;
    SlurmRunnerEntity slurmRunner;
}
