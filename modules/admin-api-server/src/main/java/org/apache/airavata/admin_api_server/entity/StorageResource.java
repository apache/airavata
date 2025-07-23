package org.apache.airavata.admin_api_server.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "storage_resources")
public class StorageResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String storage;
    
    @Column(name = "storage_type", nullable = false)
    private String storageType;
    
    @Column(nullable = false)
    private String status;
    
    private String description;
    
    // Constructors
    public StorageResource() {}
    
    public StorageResource(String name, String storage, String storageType, String status, String description) {
        this.name = name;
        this.storage = storage;
        this.storageType = storageType;
        this.status = status;
        this.description = description;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getStorage() { return storage; }
    public void setStorage(String storage) { this.storage = storage; }
    
    public String getStorageType() { return storageType; }
    public void setStorageType(String storageType) { this.storageType = storageType; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}