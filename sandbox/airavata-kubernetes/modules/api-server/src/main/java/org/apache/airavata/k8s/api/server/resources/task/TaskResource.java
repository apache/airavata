package org.apache.airavata.k8s.api.server.resources.task;

import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class TaskResource {

    private long id;
    private int taskType;
    private long parentProcessId;
    private long creationTime;
    private long lastUpdateTime;
    private List<Long> taskStatusIds;
    private String taskDetail;
    private List<Long> taskErrorIds;
    private List<Long> jobIds;

    public long getId() {
        return id;
    }

    public TaskResource setId(long id) {
        this.id = id;
        return this;
    }

    public int getTaskType() {
        return taskType;
    }

    public TaskResource setTaskType(int taskType) {
        this.taskType = taskType;
        return this;
    }

    public long getParentProcessId() {
        return parentProcessId;
    }

    public TaskResource setParentProcessId(long parentProcessId) {
        this.parentProcessId = parentProcessId;
        return this;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public TaskResource setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public TaskResource setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return this;
    }

    public List<Long> getTaskStatusIds() {
        return taskStatusIds;
    }

    public TaskResource setTaskStatusIds(List<Long> taskStatusIds) {
        this.taskStatusIds = taskStatusIds;
        return this;
    }

    public String getTaskDetail() {
        return taskDetail;
    }

    public TaskResource setTaskDetail(String taskDetail) {
        this.taskDetail = taskDetail;
        return this;
    }

    public List<Long> getTaskErrorIds() {
        return taskErrorIds;
    }

    public TaskResource setTaskErrorIds(List<Long> taskErrorIds) {
        this.taskErrorIds = taskErrorIds;
        return this;
    }

    public List<Long> getJobIds() {
        return jobIds;
    }

    public TaskResource setJobIds(List<Long> jobIds) {
        this.jobIds = jobIds;
        return this;
    }
}
