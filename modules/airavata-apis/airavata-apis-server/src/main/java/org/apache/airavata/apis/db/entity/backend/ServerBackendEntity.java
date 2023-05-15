package org.apache.airavata.apis.db.entity.backend;

import org.apache.airavata.apis.db.entity.backend.iface.SCPInterfaceEntity;
import org.apache.airavata.apis.db.entity.backend.iface.SSHInterfaceEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class ServerBackendEntity {

    @Id
    @Column(name = "BACKEND_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String backendId;

    @Column
    String hostName;
    @Column
    int port;

    SSHInterfaceEntity commandInterface;
    SCPInterfaceEntity dataInterface;
    @Column
    String workingDirectory;

}
