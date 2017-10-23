package org.apache.airavata.k8s.api.server.resources.application;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ApplicationDeploymentResource {

    private long id;
    private long applicationModuleId;
    private long computeResourceId;
    private String executablePath;
    private String preJobCommand;
    private String postJobCommand;

    public long getId() {
        return id;
    }

    public ApplicationDeploymentResource setId(long id) {
        this.id = id;
        return this;
    }

    public long getApplicationModuleId() {
        return applicationModuleId;
    }

    public ApplicationDeploymentResource setApplicationModuleId(long applicationModuleId) {
        this.applicationModuleId = applicationModuleId;
        return this;
    }

    public long getComputeResourceId() {
        return computeResourceId;
    }

    public ApplicationDeploymentResource setComputeResourceId(long computeResourceId) {
        this.computeResourceId = computeResourceId;
        return this;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public ApplicationDeploymentResource setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
        return this;
    }

    public String getPreJobCommand() {
        return preJobCommand;
    }

    public ApplicationDeploymentResource setPreJobCommand(String preJobCommand) {
        this.preJobCommand = preJobCommand;
        return this;
    }

    public String getPostJobCommand() {
        return postJobCommand;
    }

    public ApplicationDeploymentResource setPostJobCommand(String postJobCommand) {
        this.postJobCommand = postJobCommand;
        return this;
    }
}
