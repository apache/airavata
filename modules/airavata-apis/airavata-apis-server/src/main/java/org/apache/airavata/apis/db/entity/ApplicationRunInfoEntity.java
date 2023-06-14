package org.apache.airavata.apis.db.entity;

import org.apache.airavata.apis.db.entity.application.ApplicationEntity;
import org.apache.airavata.apis.db.entity.application.runners.DockerRunnerEntity;
import org.apache.airavata.apis.db.entity.application.runners.RunnerEntity;
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
    @JoinColumn(name = "runner_id")
    RunnerEntity runner;

    public ApplicationEntity getApplication() {
        return application;
    }

    public void setApplication(ApplicationEntity application) {
        this.application = application;
    }

    public RunnerEntity getRunner() {
        return runner;
    }

    public void setRunner(RunnerEntity runner) {
        this.runner = runner;
    }

    /*
     * Helper getters/setters for mapping from/to protobuf messages
     */
    public DockerRunnerEntity getDockerRunner() {
        return runner instanceof DockerRunnerEntity ? (DockerRunnerEntity) runner : null;
    }

    public void setDockerRunner(DockerRunnerEntity dockerRunner) {
        this.runner = dockerRunner;
    }

    public SlurmRunnerEntity getSlurmRunner() {
        return runner instanceof SlurmRunnerEntity ? (SlurmRunnerEntity) runner : null;
    }

    public void setSlurmRunner(SlurmRunnerEntity slurmRunner) {
        this.runner = slurmRunner;
    }

}
