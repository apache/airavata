package org.apache.airavata.persistance.registry.jpa.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class Experiment_Metadata {
    @Id
    private String experiment_ID;
    @Lob
    private byte[] metadata;

    public String getExperiment_ID() {
        return experiment_ID;
    }

    public byte[] getMetadata() {
        return metadata;
    }

    public void setMetadata(byte[] metadata) {
        this.metadata = metadata;
    }

    public void setExperiment_ID(String experiment_ID) {
        this.experiment_ID = experiment_ID;
    }
}
