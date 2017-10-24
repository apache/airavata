package org.apache.airavata.k8s.api.server.resources.experiment;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ExperimentOutputResource {

    private long id;
    private String name;
    private String value;
    private int type;

    public long getId() {
        return id;
    }

    public ExperimentOutputResource setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ExperimentOutputResource setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ExperimentOutputResource setValue(String value) {
        this.value = value;
        return this;
    }

    public int getType() {
        return type;
    }

    public ExperimentOutputResource setType(int type) {
        this.type = type;
        return this;
    }
}
