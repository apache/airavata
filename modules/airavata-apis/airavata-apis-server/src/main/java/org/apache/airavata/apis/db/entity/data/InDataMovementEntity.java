package org.apache.airavata.apis.db.entity.data;


import org.apache.airavata.apis.db.entity.BaseEntity;
import org.apache.airavata.apis.db.entity.DataMovementConfigurationEntity;

import javax.persistence.*;

@Entity
public class InDataMovementEntity extends BaseEntity {

    @Column
    int inputIndex;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "source_location_id")
    FileLocationEntity sourceLocation;

    @ManyToOne
    @JoinColumn(name = "dm_config_id", nullable = false)
    DataMovementConfigurationEntity dataMovementConfiguration;

    public int getInputIndex() {
        return inputIndex;
    }

    public void setInputIndex(int inputIndex) {
        this.inputIndex = inputIndex;
    }

    public FileLocationEntity getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(FileLocationEntity sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public DataMovementConfigurationEntity getDataMovementConfiguration() {
        return dataMovementConfiguration;
    }

    public void setDataMovementConfiguration(DataMovementConfigurationEntity dataMovementConfiguration) {
        this.dataMovementConfiguration = dataMovementConfiguration;
    }

}
