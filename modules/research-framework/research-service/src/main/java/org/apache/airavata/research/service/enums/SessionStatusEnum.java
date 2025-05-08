package org.apache.airavata.research.service.enums;

public enum SessionStatusEnum {
    CREATED("CREATED"),
    RUNNING("RUNNING"),
    FINISHED("FINISHED"),
    TERMINATED("TERMINATED"),
    ERROR("ERROR");

    private String str;
    SessionStatusEnum(String str) {
        this.str = str;
    }
    public String toString() {
        return str;
    }
}
