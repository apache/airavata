package org.apache.airavata.k8s.api.server.resources.application;

import org.apache.airavata.k8s.api.server.model.application.ApplicationModule;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ApplicationIfaceResource {
    private long id;
    private String name;
    private String description;
    private long applicationModuleId;
    private List<ApplicationInputResource> inputs = new ArrayList<>();
    private List<ApplicationOutputResource> outputs = new ArrayList<>();

    public long getId() {
        return id;
    }

    public ApplicationIfaceResource setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ApplicationIfaceResource setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ApplicationIfaceResource setDescription(String description) {
        this.description = description;
        return this;
    }

    public long getApplicationModuleId() {
        return applicationModuleId;
    }

    public ApplicationIfaceResource setApplicationModuleId(long applicationModuleId) {
        this.applicationModuleId = applicationModuleId;
        return this;
    }

    public List<ApplicationInputResource> getInputs() {
        return inputs;
    }

    public ApplicationIfaceResource setInputs(List<ApplicationInputResource> inputs) {
        this.inputs = inputs;
        return this;
    }

    public List<ApplicationOutputResource> getOutputs() {
        return outputs;
    }

    public ApplicationIfaceResource setOutputs(List<ApplicationOutputResource> outputs) {
        this.outputs = outputs;
        return this;
    }
}
