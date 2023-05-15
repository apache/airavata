package org.apache.airavata.apis.db.entity.application.runners;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class DockerRunnerEntity {

    @Id
    @Column(name = "DOCKER_RUNNER_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String dockerRunnerId;
}
