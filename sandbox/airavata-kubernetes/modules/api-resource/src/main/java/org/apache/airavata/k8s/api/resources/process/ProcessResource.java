package org.apache.airavata.k8s.api.resources.process;

import org.apache.airavata.k8s.api.resources.task.TaskResource;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class ProcessResource {

    private long id;
    private long experimentId;
    private long creationTime;
    private long lastUpdateTime;
    private List<ProcessStatusResource> processStatuses = new ArrayList<>();
    private List<TaskResource> tasks = new ArrayList<>();
    private List<Long> processErrorIds = new ArrayList<>();
    private String taskDag;
    private String experimentDataDir;

    public long getId() {
        return id;
    }

    public ProcessResource setId(long id) {
        this.id = id;
        return this;
    }

    public long getExperimentId() {
        return experimentId;
    }

    public ProcessResource setExperimentId(long experimentId) {
        this.experimentId = experimentId;
        return this;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public ProcessResource setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public ProcessResource setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return this;
    }

    public List<ProcessStatusResource> getProcessStatuses() {
        return processStatuses;
    }

    public ProcessResource setProcessStatuses(List<ProcessStatusResource> processStatuses) {
        this.processStatuses = processStatuses;
        return this;
    }

    public List<TaskResource> getTasks() {
        return tasks;
    }

    public ProcessResource setTasks(List<TaskResource> tasks) {
        this.tasks = tasks;
        return this;
    }

    public String getTaskDag() {
        return taskDag;
    }

    public ProcessResource setTaskDag(String taskDag) {
        this.taskDag = taskDag;
        return this;
    }

    public List<Long> getProcessErrorIds() {
        return processErrorIds;
    }

    public ProcessResource setProcessErrorIds(List<Long> processErrorIds) {
        this.processErrorIds = processErrorIds;
        return this;
    }

    public String getExperimentDataDir() {
        return experimentDataDir;
    }

    public ProcessResource setExperimentDataDir(String experimentDataDir) {
        this.experimentDataDir = experimentDataDir;
        return this;
    }
}
