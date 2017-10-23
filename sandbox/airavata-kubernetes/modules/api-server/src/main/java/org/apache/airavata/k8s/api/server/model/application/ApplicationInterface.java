package org.apache.airavata.k8s.api.server.model.application;

import org.apache.airavata.k8s.api.server.model.experiment.ExperimentInputData;
import org.apache.airavata.k8s.api.server.model.experiment.ExperimentOutputData;

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
@Table(name = "APPLICATION_INTERFACE")
public class ApplicationInterface {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "APP_MODULE_ID")
    private ApplicationModule applicationModule;

    @OneToMany
    private List<ApplicationInput> inputs = new ArrayList<>();

    @OneToMany
    private List<ApplicationOutput> outputs = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ApplicationModule getApplicationModule() {
        return applicationModule;
    }

    public void setApplicationModule(ApplicationModule applicationModule) {
        this.applicationModule = applicationModule;
    }

    public List<ApplicationInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<ApplicationInput> inputs) {
        this.inputs = inputs;
    }

    public List<ApplicationOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<ApplicationOutput> outputs) {
        this.outputs = outputs;
    }
}
