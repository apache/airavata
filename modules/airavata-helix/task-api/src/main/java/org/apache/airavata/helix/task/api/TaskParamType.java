package org.apache.airavata.helix.task.api;

public interface TaskParamType {

    public String serialize();
    public void deserialize(String content);
}
