package org.apache.airavata.k8s.api.server.resources.experiment;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ExperimentInputResource {

    private long id;
    private String name;
    private int type;
    private String value;
    private String arguments;

    public long getId() {
        return id;
    }

    public ExperimentInputResource setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ExperimentInputResource setName(String name) {
        this.name = name;
        return this;
    }

    public int getType() {
        return type;
    }

    public ExperimentInputResource setType(int type) {
        this.type = type;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ExperimentInputResource setValue(String value) {
        this.value = value;
        return this;
    }

    public String getArguments() {
        return arguments;
    }

    public ExperimentInputResource setArguments(String arguments) {
        this.arguments = arguments;
        return this;
    }
}
