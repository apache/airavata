package org.apache.airavata.k8s.api.resources.application;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ApplicationModuleResource {

    private long id;
    private String name;
    private String version;
    private String description;

    public long getId() {
        return id;
    }

    public ApplicationModuleResource setId(long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ApplicationModuleResource setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public ApplicationModuleResource setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ApplicationModuleResource setDescription(String description) {
        this.description = description;
        return this;
    }
}
