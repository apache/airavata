package org.apache.airavata.research.service.enums;

public enum ResourceTypeEnum {
    NOTEBOOK("NOTEBOOK"),
    DATASET("DATASET"),
    REPOSITORY("REPOSITORY");

    private String str;
    ResourceTypeEnum(String str) {
        this.str = str;
    }
    public String toString() {
        return str;
    }
}
