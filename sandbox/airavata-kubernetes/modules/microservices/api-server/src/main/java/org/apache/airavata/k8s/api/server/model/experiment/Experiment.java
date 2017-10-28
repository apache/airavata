package org.apache.airavata.k8s.api.server.model.experiment;

import org.apache.airavata.k8s.api.server.model.application.ApplicationDeployment;
import org.apache.airavata.k8s.api.server.model.application.ApplicationInterface;
import org.apache.airavata.k8s.api.server.model.commons.ErrorModel;
import org.apache.airavata.k8s.api.server.model.process.ProcessModel;

import javax.persistence.*;
import java.util.ArrayList;
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
    private ApplicationInterface applicationInterface;

    @ManyToOne
    private ApplicationDeployment applicationDeployment;

    @OneToMany
    private List<ExperimentInputData> experimentInputs = new ArrayList<>();

    @OneToMany
    private List<ExperimentOutputData> experimentOutputs = new ArrayList<>();

    @OneToMany
    private List<ExperimentStatus> experimentStatus = new ArrayList<>();

    @OneToMany
    private List<ErrorModel> errors = new ArrayList<>();

    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL)
    private List<ProcessModel> processes = new ArrayList<>();

    public long getId() {
        return id;
    }

    public Experiment setId(long id) {
        this.id = id;
        return this;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public Experiment setExperimentName(String experimentName) {
        this.experimentName = experimentName;
        return this;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public Experiment setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Experiment setDescription(String description) {
        this.description = description;
        return this;
    }

    public ApplicationInterface getApplicationInterface() {
        return applicationInterface;
    }

    public Experiment setApplicationInterface(ApplicationInterface applicationInterface) {
        this.applicationInterface = applicationInterface;
        return this;
    }

    public ApplicationDeployment getApplicationDeployment() {
        return applicationDeployment;
    }

    public Experiment setApplicationDeployment(ApplicationDeployment applicationDeployment) {
        this.applicationDeployment = applicationDeployment;
        return this;
    }

    public List<ExperimentInputData> getExperimentInputs() {
        return experimentInputs;
    }

    public Experiment setExperimentInputs(List<ExperimentInputData> experimentInputs) {
        this.experimentInputs = experimentInputs;
        return this;
    }

    public List<ExperimentOutputData> getExperimentOutputs() {
        return experimentOutputs;
    }

    public Experiment setExperimentOutputs(List<ExperimentOutputData> experimentOutputs) {
        this.experimentOutputs = experimentOutputs;
        return this;
    }

    public List<ExperimentStatus> getExperimentStatus() {
        return experimentStatus;
    }

    public Experiment setExperimentStatus(List<ExperimentStatus> experimentStatus) {
        this.experimentStatus = experimentStatus;
        return this;
    }

    public List<ErrorModel> getErrors() {
        return errors;
    }

    public Experiment setErrors(List<ErrorModel> errors) {
        this.errors = errors;
        return this;
    }

    public List<ProcessModel> getProcesses() {
        return processes;
    }

    public Experiment setProcesses(List<ProcessModel> processes) {
        this.processes = processes;
        return this;
    }
}
