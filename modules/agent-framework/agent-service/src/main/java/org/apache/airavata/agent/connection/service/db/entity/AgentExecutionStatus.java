package org.apache.airavata.agent.connection.service.db.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;


@Entity(name = "AGENT_EXECUTION_STATUS")
public class AgentExecutionStatus {

    public static enum ExecutionStatus {
        SUBMITTED_TO_CLUSTER,
        FAILED,
        CONNECTED,
        CONNECTION_BROKEN,
        TERMINATING,
        TERMINATED,
    }

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "AGENT_EXECUTION_STATUS_ID")
    private String id;

    @ManyToOne(targetEntity = AgentExecution.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private AgentExecution agentExecution;

    @Column(name = "UPDATED_TIME")
    private long updateTime;

    @Column(name = "STATUS")
    private ExecutionStatus status;

    @Column(name = "ADDITIONAL_INFO", length = 2000)
    private String additionalInfo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AgentExecution getAgentExecution() {
        return agentExecution;
    }

    public void setAgentExecution(AgentExecution agentExecution) {
        this.agentExecution = agentExecution;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
