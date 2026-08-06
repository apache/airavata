package org.apache.airavata.application.model.deployment;

import org.apache.airavata.application.model.template.ApplicationTemplateEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;

@Entity
public class SlurmApplicationDeploymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String deploymentId;

    /*
     * TODO: This should be a foreign key to a SlurmClusterEntity, but we don't have
     * that yet.
     */
    private String slurmClusterId;

    @ManyToOne
    @JoinColumn(name = "template_id", foreignKey = @ForeignKey(name = "fk_dep_app_template"))
    private ApplicationTemplateEntity applicationTemplate;

    /*
     * All execution commands go into this. This includes module loads, clean up
     * commands, actual run command. Script can be parameterized using jinja
     * template
     */
    @Lob
    @Column(nullable = false)
    private String slurmRunSection;

    // Slurm resource allocation parameters
    @Column(nullable = true)
    private Integer cpus;
    @Column(nullable = true)
    private String mem;
    @Column(nullable = true)
    private String memPerCpu;
    @Column(nullable = true)
    private Integer ntasksPerNode;
    @Column(nullable = true)
    private Integer cpusPerTask;
    @Column(nullable = true)
    private Integer nodes;
    @Column(nullable = true)
    private Integer ntasks;

    // GPU related fields
    @Column(nullable = true)
    private String gres;
    @Column(nullable = true)
    private Integer gpus;
    @Column(nullable = true)
    private String memPerGpu;
    @Column(nullable = true)
    private String cpusPerGpu;
    @Column(nullable = true)
    private Integer gpusPerNode;

    @Column(nullable = false)
    private Long wallTimeMinutes;

    @Column(nullable = true)
    private String constraints; // e.g. "gpu", "cpu", "highmem", etc.

    @Column(nullable = false)
    private String allocation;
    @Column(nullable = false)
    private String user; // User who will submit the job
    @Column(nullable = true)
    private String workDir; // Additional subdir for execution will be created inside this
    @Column(nullable = true)
    private String partition;

    // Getters and Setters
    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getSlurmClusterId() {
        return slurmClusterId;
    }

    public void setSlurmClusterId(String slurmClusterId) {
        this.slurmClusterId = slurmClusterId;
    }

    public ApplicationTemplateEntity getApplicationTemplate() {
        return applicationTemplate;
    }

    public void setApplicationTemplate(ApplicationTemplateEntity applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }

    public String getSlurmRunSection() {
        return slurmRunSection;
    }

    public void setSlurmRunSection(String slurmRunSection) {
        this.slurmRunSection = slurmRunSection;
    }

    public Integer getCpus() {
        return cpus;
    }

    public void setCpus(Integer cpus) {
        this.cpus = cpus;
    }

    public String getMem() {
        return mem;
    }

    public void setMem(String mem) {
        this.mem = mem;
    }

    public String getMemPerCpu() {
        return memPerCpu;
    }

    public void setMemPerCpu(String memPerCpu) {
        this.memPerCpu = memPerCpu;
    }

    public Integer getNtasksPerNode() {
        return ntasksPerNode;
    }

    public void setNtasksPerNode(Integer ntasksPerNode) {
        this.ntasksPerNode = ntasksPerNode;
    }

    public Integer getCpusPerTask() {
        return cpusPerTask;
    }

    public void setCpusPerTask(Integer cpusPerTask) {
        this.cpusPerTask = cpusPerTask;
    }

    public Integer getNodes() {
        return nodes;
    }

    public void setNodes(Integer nodes) {
        this.nodes = nodes;
    }

    public Integer getNtasks() {
        return ntasks;
    }

    public void setNtasks(Integer ntasks) {
        this.ntasks = ntasks;
    }

    public String getGres() {
        return gres;
    }

    public void setGres(String gres) {
        this.gres = gres;
    }

    public Integer getGpus() {
        return gpus;
    }

    public void setGpus(Integer gpus) {
        this.gpus = gpus;
    }

    public String getMemPerGpu() {
        return memPerGpu;
    }

    public void setMemPerGpu(String memPerGpu) {
        this.memPerGpu = memPerGpu;
    }

    public String getCpusPerGpu() {
        return cpusPerGpu;
    }

    public void setCpusPerGpu(String cpusPerGpu) {
        this.cpusPerGpu = cpusPerGpu;
    }

    public Integer getGpusPerNode() {
        return gpusPerNode;
    }

    public void setGpusPerNode(Integer gpusPerNode) {
        this.gpusPerNode = gpusPerNode;
    }

    public Long getWallTimeMinutes() {
        return wallTimeMinutes;
    }

    public void setWallTimeMinutes(Long wallTimeMinutes) {
        this.wallTimeMinutes = wallTimeMinutes;
    }

    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    public String getAllocation() {
        return allocation;
    }

    public void setAllocation(String allocation) {
        this.allocation = allocation;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }
}
