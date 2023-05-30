package org.apache.airavata.apis.db.entity.backend;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("LOCAL")
public class LocalBackendEntity extends ComputeBackendEntity {


    @Column
    String agentId;
    @Column
    String agentTokenId;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAgentTokenId() {
        return agentTokenId;
    }

    public void setAgentTokenId(String agentTokenId) {
        this.agentTokenId = agentTokenId;
    }

}
