package org.apache.airavata.apis.db.entity.backend.iface;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class SSHInterfaceEntity {

    @Id
    @Column(name = "SSH_IFACE_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String sshInterfaceId;



}
