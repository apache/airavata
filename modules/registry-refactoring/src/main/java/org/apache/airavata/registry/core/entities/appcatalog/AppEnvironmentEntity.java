package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The persistent class for the app_environment database table.
 */
@Entity
@Table(name = "app_environment")
public class AppEnvironmentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "DEPLOYMENT_ID")
    private String deploymentId;

    @Column(name = "value")
    private String value;

    @Column(name = "name")
    private String name;

    public AppEnvironmentEntity() {
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}