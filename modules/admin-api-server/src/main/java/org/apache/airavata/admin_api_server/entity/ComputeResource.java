package org.apache.airavata.admin_api_server.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "compute_resources")
public class ComputeResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String compute;
    
    @Column(name = "compute_type", nullable = false)
    private String computeType;
    
    @Column(nullable = false)
    private String status;
    
    private String description;
    
    // Step 2 fields - Queue Configuration
    @Column(name = "scheduler_type")
    private String schedulerType;
    
    @Column(name = "data_movement_protocol")
    private String dataMovementProtocol;
    
    @ElementCollection
    @CollectionTable(name = "compute_resource_queues", joinColumns = @JoinColumn(name = "compute_resource_id"))
    @Column(name = "queue_name")
    private List<String> queues;
    
    // Constructors
    public ComputeResource() {}
    
    public ComputeResource(String name, String compute, String computeType, String status, String description) {
        this.name = name;
        this.compute = compute;
        this.computeType = computeType;
        this.status = status;
        this.description = description;
    }
    
    public ComputeResource(String name, String compute, String computeType, String status, String description, 
                          String schedulerType, String dataMovementProtocol, List<String> queues) {
        this.name = name;
        this.compute = compute;
        this.computeType = computeType;
        this.status = status;
        this.description = description;
        this.schedulerType = schedulerType;
        this.dataMovementProtocol = dataMovementProtocol;
        this.queues = queues;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCompute() { return compute; }
    public void setCompute(String compute) { this.compute = compute; }
    
    public String getComputeType() { return computeType; }
    public void setComputeType(String computeType) { this.computeType = computeType; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getSchedulerType() { return schedulerType; }
    public void setSchedulerType(String schedulerType) { this.schedulerType = schedulerType; }
    
    public String getDataMovementProtocol() { return dataMovementProtocol; }
    public void setDataMovementProtocol(String dataMovementProtocol) { this.dataMovementProtocol = dataMovementProtocol; }
    
    public List<String> getQueues() { return queues; }
    public void setQueues(List<String> queues) { this.queues = queues; }
}