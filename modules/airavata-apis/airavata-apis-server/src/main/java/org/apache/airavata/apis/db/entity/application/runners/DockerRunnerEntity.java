package org.apache.airavata.apis.db.entity.application.runners;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class DockerRunnerEntity extends RunnerEntity {

    @Column
    String imageName;

    @Column
    String imageTag;

    @Column
    String repository;

    @Column
    String dockerCredentialId;

    @Column
    String runCommand;

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageTag() {
        return imageTag;
    }

    public void setImageTag(String imageTag) {
        this.imageTag = imageTag;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getDockerCredentialId() {
        return dockerCredentialId;
    }

    public void setDockerCredentialId(String dockerCredentialId) {
        this.dockerCredentialId = dockerCredentialId;
    }

    public String getRunCommand() {
        return runCommand;
    }

    public void setRunCommand(String runCommand) {
        this.runCommand = runCommand;
    }
}
