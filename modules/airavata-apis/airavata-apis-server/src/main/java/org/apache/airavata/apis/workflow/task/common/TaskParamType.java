package org.apache.airavata.apis.workflow.task.common;

public interface TaskParamType {
    public String serialize();
    public void deserialize(String content);
}