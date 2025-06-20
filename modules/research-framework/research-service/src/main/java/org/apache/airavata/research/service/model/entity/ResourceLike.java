package org.apache.airavata.research.service.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.UuidGenerator;

@Entity(name = "RESOURCE_LIKE")
public class ResourceLike {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false, length = 48)
    private String id;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;


    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
