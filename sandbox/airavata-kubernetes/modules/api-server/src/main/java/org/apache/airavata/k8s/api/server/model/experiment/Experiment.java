package org.apache.airavata.k8s.api.server.model.experiment;

import org.apache.airavata.k8s.api.server.model.application.ApplicationDeployment;
import org.apache.airavata.k8s.api.server.model.application.ApplicationInterface;
import org.apache.airavata.k8s.api.server.model.commons.ErrorModel;
import org.apache.airavata.k8s.api.server.model.process.ProcessModel;

import javax.persistence.*;
import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Entity
@Table(name = "EXPERIMENT")
public class Experiment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String experimentName;
    private long creationTime;
    private String description;

    @ManyToOne
    private ApplicationInterface applicationInterface; // optional

    @ManyToOne
    private ApplicationDeployment applicationDeployment;

    @OneToMany
    private List<ExperimentInputData> experimentInputs; // optional

    @OneToMany
    private List<ExperimentOutputData> experimentOutputs; // optional

    @OneToMany
    private List<ExperimentStatus> experimentStatus; // optional

    @OneToMany
    private List<ErrorModel> errors; // optional

    @OneToMany
    private List<ProcessModel> processes; // optional

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ApplicationInterface getApplicationInterface() {
        return applicationInterface;
    }

    public void setApplicationInterface(ApplicationInterface applicationInterface) {
        this.applicationInterface = applicationInterface;
    }

    public List<ExperimentInputData> getExperimentInputs() {
        return experimentInputs;
    }

    public void setExperimentInputs(List<ExperimentInputData> experimentInputs) {
        this.experimentInputs = experimentInputs;
    }

    public List<ExperimentOutputData> getExperimentOutputs() {
        return experimentOutputs;
    }

    public void setExperimentOutputs(List<ExperimentOutputData> experimentOutputs) {
        this.experimentOutputs = experimentOutputs;
    }

    public List<ExperimentStatus> getExperimentStatus() {
        return experimentStatus;
    }

    public void setExperimentStatus(List<ExperimentStatus> experimentStatus) {
        this.experimentStatus = experimentStatus;
    }

    public List<ErrorModel> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorModel> errors) {
        this.errors = errors;
    }

    public List<ProcessModel> getProcesses() {
        return processes;
    }

    public void setProcesses(List<ProcessModel> processes) {
        this.processes = processes;
    }

    public ApplicationDeployment getApplicationDeployment() {
        return applicationDeployment;
    }

    public void setApplicationDeployment(ApplicationDeployment applicationDeployment) {
        this.applicationDeployment = applicationDeployment;
    }
}
