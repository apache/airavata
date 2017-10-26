package org.apache.airavata.k8s.api.resources.task;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class TaskParamResource {

    private long id;
    private String key;
    private String value;

    public long getId() {
        return id;
    }

    public TaskParamResource setId(long id) {
        this.id = id;
        return this;
    }

    public String getKey() {
        return key;
    }

    public TaskParamResource setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public TaskParamResource setValue(String value) {
        this.value = value;
        return this;
    }
}
