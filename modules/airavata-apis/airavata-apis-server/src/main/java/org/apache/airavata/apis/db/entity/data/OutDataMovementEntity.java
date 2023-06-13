package org.apache.airavata.apis.db.entity.data;

import org.apache.airavata.apis.db.entity.BaseEntity;
import org.apache.airavata.apis.db.entity.DataMovementConfigurationEntity;

import javax.persistence.*;

@Entity
public class OutDataMovementEntity extends BaseEntity {

    @Column
    int outputIndex = 1;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "destination_location_id")
    FileLocationEntity destinationLocation;

    @ManyToOne
    @JoinColumn(name = "dm_config_id", nullable = false)
    DataMovementConfigurationEntity dataMovementConfiguration;

    public int getOutputIndex() {
        return outputIndex;
    }

    public void setOutputIndex(int outputIndex) {
        this.outputIndex = outputIndex;
    }

    public FileLocationEntity getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(FileLocationEntity destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public DataMovementConfigurationEntity getDataMovementConfiguration() {
        return dataMovementConfiguration;
    }

    public void setDataMovementConfiguration(DataMovementConfigurationEntity dataMovementConfiguration) {
        this.dataMovementConfiguration = dataMovementConfiguration;
    }

}
