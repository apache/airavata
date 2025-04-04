package org.apache.airavata.agent.connection.service.models;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class AgentLaunchRequest {

    private String experimentName;
    private String projectName;
    private String remoteCluster;
    private String group;
    private List<String> libraries = new ArrayList<>();
    private List<String> pip = new ArrayList<>();
    private List<String> mounts = new ArrayList<>();

    private String queue = "shared";
    private int wallTime = 30;
    private int cpuCount = 2;
    private int nodeCount = 1;
    private int memory = 2048;

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public String getRemoteCluster() {
        return remoteCluster;
    }

    public void setRemoteCluster(String remoteCluster) {
        this.remoteCluster = remoteCluster;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public int getWallTime() {
        return wallTime;
    }

    public void setWallTime(int wallTime) {
        this.wallTime = wallTime;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getApplicationInterfaceName() {
        return remoteCluster + (StringUtils.isNotBlank(group) ? ("_" + group) : "");
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<String> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<String> libraries) {
        this.libraries = libraries;
    }

    public List<String> getPip() {
        return pip;
    }

    public void setPip(List<String> pip) {
        this.pip = pip;
    }

    public List<String> getMounts() {
        return mounts;
    }

    public void setMounts(List<String> mounts) {
        this.mounts = mounts;
    }
}
