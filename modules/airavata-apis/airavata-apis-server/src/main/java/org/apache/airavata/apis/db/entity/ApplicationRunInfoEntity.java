package org.apache.airavata.apis.db.entity;

import org.apache.airavata.apis.db.entity.application.ApplicationEntity;
import org.apache.airavata.apis.db.entity.application.runners.DockerRunnerEntity;
import org.apache.airavata.apis.db.entity.application.runners.SlurmRunnerEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class ApplicationRunInfoEntity {

    @Id
    @Column(name = "APP_RUNNER_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String appRunnerId;

    @OneToOne
    @JoinColumn(name = "application_id", referencedColumnName = "application_id")
    private ApplicationEntity application;

    @OneToOne
    @JoinColumn(name = "docker_runner_id", referencedColumnName = "docker_runner_id")
    DockerRunnerEntity dockerRunner;

    @OneToOne
    @JoinColumn(name = "slurm_runner_id", referencedColumnName = "slurm_runner_id")
    SlurmRunnerEntity slurmRunner;

    public String getAppRunnerId() {
        return appRunnerId;
    }

    public void setAppRunnerId(String appRunnerId) {
        this.appRunnerId = appRunnerId;
    }

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((appRunnerId == null) ? 0 : appRunnerId.hashCode());
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
        ApplicationRunInfoEntity other = (ApplicationRunInfoEntity) obj;
        if (appRunnerId == null) {
            if (other.appRunnerId != null)
                return false;
        } else if (!appRunnerId.equals(other.appRunnerId))
            return false;
        return true;
    }

}
