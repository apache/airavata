package org.apache.airavata.k8s.api.server.resources.application;

import org.apache.airavata.k8s.api.server.model.application.ApplicationOutput;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ApplicationOutputResource {
    private long id;
    private String name;
    private int type;
    private String value;

    public long getId() {
        return id;
    }

    public ApplicationOutputResource setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ApplicationOutputResource setName(String name) {
        this.name = name;
        return this;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public ApplicationOutputResource setValue(String value) {
        this.value = value;
        return this;
    }
}
