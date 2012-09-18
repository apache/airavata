package org.apache.airavata.persistance.registry.jpa.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class Experiment_Metadata {
    @Id
    private String experiment_ID;
    @Lob
    private String metadata;

    public String getExperiment_ID() {
        return experiment_ID;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public void setExperiment_ID(String experiment_ID) {
        this.experiment_ID = experiment_ID;
    }
}
