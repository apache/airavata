package org.apache.airavata.persistence.appcatalog.jpa.model;

import javax.persistence.*;

@Entity
@Table(name = "GATEWAY_PROFILE")
@IdClass(ApplicationDeploymentPK.class)
public class ApplicationDeployment {

    @Id
    @Column(name = "DEPLOYMENT_ID")
    private String deploymentID;

    @Id
    @Column(name = "APPLICATION_ID")
    private String applicationID;

    @Column(name = "DEPLOYMENT_HOST_NAME")
    private String deploymentHostName;


    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "DEPLOYMENT_ID")
    private Deployment deployment;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "APPLICATION_ID")
    private Application application;
}
