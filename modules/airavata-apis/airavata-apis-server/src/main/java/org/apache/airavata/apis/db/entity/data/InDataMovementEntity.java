package org.apache.airavata.apis.db.entity.data;


import org.apache.airavata.apis.db.entity.DataMovementConfigurationEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class InDataMovementEntity {

    @Id
    @Column(name = "IN_DM_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String inDataMovementId;

    @Column
    int inputIndex;

    @OneToOne
    @JoinColumn(name = "source_location_id", referencedColumnName = "file_location_id")
    FileLocationEntity sourceLocation;

    @ManyToOne
    @JoinColumn(name = "dm_config_id", referencedColumnName = "dm_config_id")
    DataMovementConfigurationEntity dataMovementConfiguration;

    public String getInDataMovementId() {
        return inDataMovementId;
    }

    public void setInDataMovementId(String inDataMovementId) {
        this.inDataMovementId = inDataMovementId;
    }

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((inDataMovementId == null) ? 0 : inDataMovementId.hashCode());
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
        InDataMovementEntity other = (InDataMovementEntity) obj;
        if (inDataMovementId == null) {
            if (other.inDataMovementId != null)
                return false;
        } else if (!inDataMovementId.equals(other.inDataMovementId))
            return false;
        return true;
    }

}
