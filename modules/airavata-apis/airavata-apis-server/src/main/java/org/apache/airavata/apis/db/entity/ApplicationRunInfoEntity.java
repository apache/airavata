package org.apache.airavata.apis.db.entity;

import org.apache.airavata.apis.db.entity.application.ApplicationEntity;
import org.apache.airavata.apis.db.entity.application.runners.DockerRunnerEntity;
import org.apache.airavata.apis.db.entity.application.runners.SlurmRunnerEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class ApplicationRunInfoEntity extends BaseEntity {


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "application_id")
    private ApplicationEntity application;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "docker_runner_id")
    DockerRunnerEntity dockerRunner;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "slurm_runner_id")
    SlurmRunnerEntity slurmRunner;


    public ApplicationEntity getApplication() {
        return application;
    }

    public void setApplication(ApplicationEntity application) {
        this.application = application;
    }

    public DockerRunnerEntity getDockerRunner() {
        return dockerRunner;
    }

    public void setDockerRunner(DockerRunnerEntity dockerRunner) {
        this.dockerRunner = dockerRunner;
    }

    public SlurmRunnerEntity getSlurmRunner() {
        return slurmRunner;
    }

    public void setSlurmRunner(SlurmRunnerEntity slurmRunner) {
        this.slurmRunner = slurmRunner;
    }

}
