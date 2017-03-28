package org.apache.airavata.common.utils;

/**
 * Created by Ajinkya on 3/28/17.
 */
public enum DBEventService {

    USER_PROFILE("user.profile"),
    SHARING("sharing"),
    REGISTRY("registry");

    private final String name;
    DBEventService(String name) {
        this.name = name;
    }
    public String toString() {
        return this.name;
    }

}
