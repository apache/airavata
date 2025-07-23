package org.apache.airavata.admin_api_server.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_starred_resources")
public class UserStarredResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "resource_id", nullable = false)
    private Long resourceId;
    
    @Column(name = "resource_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;
    
    public enum ResourceType {
        MODEL, DATASET, NOTEBOOK, REPOSITORY, STORAGE_RESOURCE, COMPUTE_RESOURCE
    }
    
    public UserStarredResource() {}
    
    public UserStarredResource(User user, Long resourceId, ResourceType resourceType) {
        this.user = user;
        this.resourceId = resourceId;
        this.resourceType = resourceType;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Long getResourceId() {
        return resourceId;
    }
    
    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
    
    public ResourceType getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }
}