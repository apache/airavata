package org.apache.airavata.agent.connection.service.db.entity;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;


@Entity(name = "AGENT_EXECUTION")
public class AgentExecution {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "AGENT_EXECUTION_ID")
    private String id;

    @Column(name = "AGENT_ID")
    private String agentId;

    @Column(name = "AIRAVATA_EXPERIMENT_ID")
    private String airavataExperimentId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAiravataExperimentId() {
        return airavataExperimentId;
    }

    public void setAiravataExperimentId(String airavataExperimentId) {
        this.airavataExperimentId = airavataExperimentId;
    }
}
