package org.apache.airavata.apis.db.entity;

import org.apache.airavata.apis.db.entity.data.InDataMovementEntity;
import org.apache.airavata.apis.db.entity.data.OutDataMovementEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Set;

@Entity
public class DataMovementConfigurationEntity {

    @Id
    @Column(name = "DM_CONFIG_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String dmConfig;
    Set<InDataMovementEntity> inMovements;
    Set<OutDataMovementEntity> ourMovements;
}
