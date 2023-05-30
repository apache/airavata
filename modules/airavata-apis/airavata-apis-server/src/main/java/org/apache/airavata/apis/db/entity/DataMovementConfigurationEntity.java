package org.apache.airavata.apis.db.entity;

import org.apache.airavata.apis.db.entity.data.InDataMovementEntity;
import org.apache.airavata.apis.db.entity.data.OutDataMovementEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import java.util.Set;

@Entity
public class DataMovementConfigurationEntity {

    @Id
    @Column(name = "DM_CONFIG_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String dmConfig;

    @OneToMany(mappedBy = "dataMovementConfiguration")
    Set<InDataMovementEntity> inMovements;

    @OneToMany(mappedBy = "dataMovementConfiguration")
    Set<OutDataMovementEntity> outMovements;

    @ManyToOne
    @JoinColumn(name = "run_config_id")
    RunConfigurationEntity runConfiguration;

    public String getDmConfig() {
        return dmConfig;
    }

    public void setDmConfig(String dmConfig) {
        this.dmConfig = dmConfig;
    }

    public Set<InDataMovementEntity> getInMovements() {
        return inMovements;
    }

    public void setInMovements(Set<InDataMovementEntity> inMovements) {
        this.inMovements = inMovements;
    }

    public Set<OutDataMovementEntity> getOutMovements() {
        return outMovements;
    }

    public void setOutMovements(Set<OutDataMovementEntity> outMovements) {
        this.outMovements = outMovements;
    }

    public RunConfigurationEntity getRunConfiguration() {
        return runConfiguration;
    }

    public void setRunConfiguration(RunConfigurationEntity runConfiguration) {
        this.runConfiguration = runConfiguration;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dmConfig == null) ? 0 : dmConfig.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataMovementConfigurationEntity other = (DataMovementConfigurationEntity) obj;
        if (dmConfig == null) {
            if (other.dmConfig != null)
                return false;
        } else if (!dmConfig.equals(other.dmConfig))
            return false;
        return true;
    }

}
