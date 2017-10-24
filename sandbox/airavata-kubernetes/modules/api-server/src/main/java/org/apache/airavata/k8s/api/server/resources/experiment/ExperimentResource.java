package org.apache.airavata.k8s.api.server.resources.experiment;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ExperimentResource {

    private long id;
    private String experimentName;
    private long creationTime;
    private String description;
    private long applicationInterfaceId;
    private long applicationDeploymentId;

    private List<ExperimentInputResource> experimentInputs = new ArrayList<>();

    private List<ExperimentOutputResource> experimentOutputs = new ArrayList<>();

    private List<Long> experimentStatusIds = new ArrayList<>();

    private List<Long> errorsIds = new ArrayList<>();

    private List<Long> processIds = new ArrayList<>();

    public long getId() {
        return id;
    }

    public ExperimentResource setId(long id) {
        this.id = id;
        return this;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public ExperimentResource setExperimentName(String experimentName) {
        this.experimentName = experimentName;
        return this;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public ExperimentResource setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ExperimentResource setDescription(String description) {
        this.description = description;
        return this;
    }

    public long getApplicationInterfaceId() {
        return applicationInterfaceId;
    }

    public ExperimentResource setApplicationInterfaceId(long applicationInterfaceId) {
        this.applicationInterfaceId = applicationInterfaceId;
        return this;
    }

    public long getApplicationDeploymentId() {
        return applicationDeploymentId;
    }

    public ExperimentResource setApplicationDeploymentId(long applicationDeploymentId) {
        this.applicationDeploymentId = applicationDeploymentId;
        return this;
    }

    public List<ExperimentInputResource> getExperimentInputs() {
        return experimentInputs;
    }

    public ExperimentResource setExperimentInputs(List<ExperimentInputResource> experimentInputs) {
        this.experimentInputs = experimentInputs;
        return this;
    }

    public List<ExperimentOutputResource> getExperimentOutputs() {
        return experimentOutputs;
    }

    public ExperimentResource setExperimentOutputs(List<ExperimentOutputResource> experimentOutputs) {
        this.experimentOutputs = experimentOutputs;
        return this;
    }

    public List<Long> getExperimentStatusIds() {
        return experimentStatusIds;
    }

    public ExperimentResource setExperimentStatusIds(List<Long> experimentStatusIds) {
        this.experimentStatusIds = experimentStatusIds;
        return this;
    }

    public List<Long> getErrorsIds() {
        return errorsIds;
    }

    public ExperimentResource setErrorsIds(List<Long> errorsIds) {
        this.errorsIds = errorsIds;
        return this;
    }

    public List<Long> getProcessIds() {
        return processIds;
    }

    public ExperimentResource setProcessIds(List<Long> processIds) {
        this.processIds = processIds;
        return this;
    }
}
