package org.apache.airavata.apis.db.entity.data;

import org.apache.airavata.apis.db.entity.DataMovementConfigurationEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class OutDataMovementEntity {

    @Id
    @Column(name = "OUT_DM_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String outDataMovementId;

    @Column
    int outputIndex = 1;

    @OneToOne
    @JoinColumn(name = "destination_location_id", referencedColumnName = "file_location_id")
    FileLocationEntity destinationLocation;

    @ManyToOne
    @JoinColumn(name = "dm_config_id", referencedColumnName = "dm_config_id")
    DataMovementConfigurationEntity dataMovementConfiguration;

    public String getOutDataMovementId() {
        return outDataMovementId;
    }

    public void setOutDataMovementId(String outDataMovementId) {
        this.outDataMovementId = outDataMovementId;
    }

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((outDataMovementId == null) ? 0 : outDataMovementId.hashCode());
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
        OutDataMovementEntity other = (OutDataMovementEntity) obj;
        if (outDataMovementId == null) {
            if (other.outDataMovementId != null)
                return false;
        } else if (!outDataMovementId.equals(other.outDataMovementId))
            return false;
        return true;
    }

}
