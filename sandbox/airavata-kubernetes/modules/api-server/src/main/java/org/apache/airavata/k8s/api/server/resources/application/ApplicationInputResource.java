package org.apache.airavata.k8s.api.server.resources.application;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ApplicationInputResource {

    private long id;
    private String name;
    private int type;
    private String value;
    private String arguments;

    public long getId() {
        return id;
    }

    public ApplicationInputResource setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ApplicationInputResource setName(String name) {
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

    public ApplicationInputResource setValue(String value) {
        this.value = value;
        return this;
    }

    public String getArguments() {
        return arguments;
    }

    public ApplicationInputResource setArguments(String arguments) {
        this.arguments = arguments;
        return this;
    }
}
