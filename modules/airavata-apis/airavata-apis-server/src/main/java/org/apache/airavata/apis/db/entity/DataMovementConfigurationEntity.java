package org.apache.airavata.apis.db.entity;

import org.apache.airavata.apis.db.entity.data.InDataMovementEntity;
import org.apache.airavata.apis.db.entity.data.OutDataMovementEntity;

import javax.persistence.*;

import java.util.Set;

@Entity
public class DataMovementConfigurationEntity extends BaseEntity {

    @OneToMany(mappedBy = "dataMovementConfiguration", cascade = CascadeType.ALL)
    Set<InDataMovementEntity> inMovements;

    @OneToMany(mappedBy = "dataMovementConfiguration", cascade = CascadeType.ALL)
    Set<OutDataMovementEntity> outMovements;

    @ManyToOne
    @JoinColumn(name = "run_config_id", nullable = false)
    RunConfigurationEntity runConfiguration;

    public Set<InDataMovementEntity> getInMovements() {
        return inMovements;
    }

    public void setInMovements(Set<InDataMovementEntity> inMovements) {
        this.inMovements = inMovements;
        for (InDataMovementEntity inMovement : inMovements) {
            inMovement.setDataMovementConfiguration(this);
        }
    }

    public Set<OutDataMovementEntity> getOutMovements() {
        return outMovements;
    }

    public void setOutMovements(Set<OutDataMovementEntity> outMovements) {
        this.outMovements = outMovements;
        for (OutDataMovementEntity outMovement : outMovements) {
            outMovement.setDataMovementConfiguration(this);
        }
    }

    public RunConfigurationEntity getRunConfiguration() {
        return runConfiguration;
    }

    public void setRunConfiguration(RunConfigurationEntity runConfiguration) {
        this.runConfiguration = runConfiguration;
    }

}
