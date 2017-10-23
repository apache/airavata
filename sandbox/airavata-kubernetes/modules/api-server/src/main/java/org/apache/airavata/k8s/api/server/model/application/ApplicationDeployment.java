package org.apache.airavata.k8s.api.server.model.application;

import javax.persistence.*;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Entity
@Table(name = "APPLICATION_DEPLOYMENT")
public class ApplicationDeployment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    @JoinColumn(name = "APPLICATION_MODULE_ID")
    private ApplicationModule applicationModule;

    @ManyToOne
    @JoinColumn(name = "COMPUTE_RESOURCE_ID")
    private ComputeResourceModel computeResource;

    private String executablePath;
    private String preJobCommand;
    private String postJobCommand;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ApplicationModule getApplicationModule() {
        return applicationModule;
    }

    public void setApplicationModule(ApplicationModule applicationModule) {
        this.applicationModule = applicationModule;
    }

    public ComputeResourceModel getComputeResource() {
        return computeResource;
    }

    public void setComputeResource(ComputeResourceModel computeResourceModel) {
        this.computeResource = computeResourceModel;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public String getPreJobCommand() {
        return preJobCommand;
    }

    public void setPreJobCommand(String preJobCommand) {
        this.preJobCommand = preJobCommand;
    }

    public String getPostJobCommand() {
        return postJobCommand;
    }

    public void setPostJobCommand(String postJobCommand) {
        this.postJobCommand = postJobCommand;
    }
}
