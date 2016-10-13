package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the host_ipaddress database table.
 */
@Entity
@Table(name = "host_ipaddress")
public class HostIpaddress implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private HostIpaddressPK id;

    @Column(name = "RESOURCE_ID")
    private String resourceId;

    public HostIpaddress() {
    }


    public HostIpaddressPK getId() {
        return id;
    }

    public void setId(HostIpaddressPK id) {
        this.id = id;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
}