package org.apache.airavata.apis.db.entity.data;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class OutDataMovementEntity {

    @Id
    @Column(name = "OUT_DM_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String outDataMovementId;
    int outputIndex = 1;
    FileLocationEntity destinationLocation;
}
