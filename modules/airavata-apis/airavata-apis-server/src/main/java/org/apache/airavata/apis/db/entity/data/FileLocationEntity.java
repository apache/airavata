package org.apache.airavata.apis.db.entity.data;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class FileLocationEntity {

    @Id
    @Column(name = "FILE_LOCATION_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String fileLocationId;
    @Column
    String storageId;
    @Column
    String path;
    @Column
    String storageCredentialId;
}
