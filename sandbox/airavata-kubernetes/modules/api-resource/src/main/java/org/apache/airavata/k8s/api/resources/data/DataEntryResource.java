package org.apache.airavata.k8s.api.resources.data;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class DataEntryResource {

    private long id;
    private String dataType;
    private String name;

    public long getId() {
        return id;
    }

    public DataEntryResource setId(long id) {
        this.id = id;
        return this;
    }

    public String getDataType() {
        return dataType;
    }

    public DataEntryResource setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }

    public String getName() {
        return name;
    }

    public DataEntryResource setName(String name) {
        this.name = name;
        return this;
    }
}
