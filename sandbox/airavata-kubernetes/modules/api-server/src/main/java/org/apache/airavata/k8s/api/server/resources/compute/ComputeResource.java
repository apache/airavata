package org.apache.airavata.k8s.api.server.resources.compute;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ComputeResource {

    private long id;
    private String name;

    public long getId() {
        return id;
    }

    public ComputeResource setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ComputeResource setName(String name) {
        this.name = name;
        return this;
    }
}
