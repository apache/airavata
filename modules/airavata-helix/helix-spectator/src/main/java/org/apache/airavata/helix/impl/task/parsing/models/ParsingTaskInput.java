package org.apache.airavata.helix.impl.task.parsing.models;

public class ParsingTaskInput {
    private String id;
    private String contextVariableName;
    private String value;

    public String getContextVariableName() {
        return contextVariableName;
    }

    public void setContextVariableName(String contextVariableName) {
        this.contextVariableName = contextVariableName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
