package org.apache.airavata.registry.core.entities.expcatalog;

import java.io.Serializable;

public class ComputationalResourceSchedulingPK implements Serializable {
    private static final long serialVersionUID = 1L;


    private String experimentId;
    private String resourceHostId;
    private String queueName;

    public ComputationalResourceSchedulingPK() {

    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getResourceHostId() {
        return resourceHostId;
    }

    public void setResourceHostId(String resourceHostId) {
        this.resourceHostId = resourceHostId;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ComputationalResourceSchedulingPK)) {
            return false;
        }
        ComputationalResourceSchedulingPK castOther = (ComputationalResourceSchedulingPK) other;
        return
                this.experimentId.equals(castOther.experimentId)
                        && this.resourceHostId.equals(castOther.resourceHostId)
                        && this.queueName.equals(castOther.queueName);
    }

    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.experimentId.hashCode();
        hash = hash * prime + this.resourceHostId.hashCode();
        hash = hash * prime + this.queueName.hashCode();

        return hash;
    }



}
