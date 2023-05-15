package org.apache.airavata.apis.db.entity.backend;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class EC2BackendEntity {

    @Id
    @Column(name = "BACKEND_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String backendId;
    @Column
    String flavor;
    @Column
    String region;

    @Column
    String awsCredentialId;

}
