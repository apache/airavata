package org.apache.airavata.apis.db.entity.data;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class InDataMovementEntity {

    @Id
    @Column(name = "IN_DM_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String inDataMovementId;
    int inputIndex;
    FileLocationEntity sourceLocation;
}
