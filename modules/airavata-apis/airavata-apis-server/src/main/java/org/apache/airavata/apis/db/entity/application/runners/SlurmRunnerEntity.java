package org.apache.airavata.apis.db.entity.application.runners;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;

import java.util.List;

@Entity
public class SlurmRunnerEntity extends RunnerEntity {

    @Column
    int nodes;

    @Column
    int cpus;

    @Column
    int memory;

    @Column
    int wallTime;

    @ElementCollection
    List<String> preJobCommands;

    @ElementCollection
    List<String> moduleLoadCommands;

    @Column
    String executable;

    @ElementCollection
    List<String> postJobCommands;

    @Column
    String queue;

    @ElementCollection
    List<String> notificationEmails;

    public int getNodes() {
        return nodes;
    }

    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    public int getCpus() {
        return cpus;
    }

    public void setCpus(int cpus) {
        this.cpus = cpus;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public int getWallTime() {
        return wallTime;
    }

    public void setWallTime(int wallTime) {
        this.wallTime = wallTime;
    }

    public List<String> getPreJobCommands() {
        return preJobCommands;
    }

    public void setPreJobCommands(List<String> preJobCommands) {
        this.preJobCommands = preJobCommands;
    }

    public List<String> getModuleLoadCommands() {
        return moduleLoadCommands;
    }

    public void setModuleLoadCommands(List<String> moduleLoadCommands) {
        this.moduleLoadCommands = moduleLoadCommands;
    }

    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public List<String> getPostJobCommands() {
        return postJobCommands;
    }

    public void setPostJobCommands(List<String> postJobCommands) {
        this.postJobCommands = postJobCommands;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public List<String> getNotificationEmails() {
        return notificationEmails;
    }

    public void setNotificationEmails(List<String> notificationEmails) {
        this.notificationEmails = notificationEmails;
    }

}
