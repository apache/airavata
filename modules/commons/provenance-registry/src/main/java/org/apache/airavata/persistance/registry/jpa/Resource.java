package org.apache.airavata.persistance.registry.jpa;

import java.util.List;

public interface Resource {
    Resource create(ResourceType type);

    void remove(ResourceType type, Object name);

    Resource get(ResourceType type, Object name);

    List<Resource> get(ResourceType type);

    void save();

    boolean isExists(ResourceType type, Object name);

}
