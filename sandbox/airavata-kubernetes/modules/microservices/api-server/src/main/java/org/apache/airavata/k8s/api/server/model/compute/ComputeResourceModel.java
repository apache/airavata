package org.apache.airavata.k8s.api.server.model.compute;

import javax.persistence.*;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Entity
@Table(name = "COMPUTE_RESOURCE")
public class ComputeResourceModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;
    private String host;
    private String userName;
    private String password;
    private String communicationType;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public ComputeResourceModel setHost(String host) {
        this.host = host;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public ComputeResourceModel setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public ComputeResourceModel setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getCommunicationType() {
        return communicationType;
    }

    public ComputeResourceModel setCommunicationType(String communicationType) {
        this.communicationType = communicationType;
        return this;
    }
}
