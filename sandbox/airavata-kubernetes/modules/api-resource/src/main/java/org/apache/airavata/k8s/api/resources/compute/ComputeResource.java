package org.apache.airavata.k8s.api.resources.compute;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ComputeResource {

    private long id;
    private String name;
    private String host;
    private String userName;
    private String password;
    private String communicationType;

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

    public String getHost() {
        return host;
    }

    public ComputeResource setHost(String host) {
        this.host = host;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public ComputeResource setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public ComputeResource setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getCommunicationType() {
        return communicationType;
    }

    public ComputeResource setCommunicationType(String communicationType) {
        this.communicationType = communicationType;
        return this;
    }
}
