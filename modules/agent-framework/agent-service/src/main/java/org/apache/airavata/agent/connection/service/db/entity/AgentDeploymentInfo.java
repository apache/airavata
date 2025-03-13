package org.apache.airavata.agent.connection.service.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.hibernate.annotations.GenericGenerator;


@Entity(name = "AGENT_DEPLOYMENT_INFO")
public class AgentDeploymentInfo {

    @Id
    @Column(name = "AGENT_DEPLOYMENT_INFO_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(unique = true, name = "USER_FRINEDLY_NAME")
    private String userFriendlyName;

    @Column(name= "COMPUTE_RESOURCE_ID")
    private String computeResourceId;

    @Column(name ="AGENT_APPLICATION_ID")
    private String agentApplicationId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserFriendlyName() {
        return userFriendlyName;
    }

    public void setUserFriendlyName(String userFriendlyName) {
        this.userFriendlyName = userFriendlyName;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getAgentApplicationId() {
        return agentApplicationId;
    }

    public void setAgentApplicationId(String agentApplicationId) {
        this.agentApplicationId = agentApplicationId;
    }
}
