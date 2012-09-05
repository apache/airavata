package org.apache.airavata.persistance.registry.jpa;

import org.apache.airavata.persistance.registry.jpa.resources.GatewayResource;
import org.apache.airavata.persistance.registry.jpa.resources.ProjectResource;

public class Test {
    public void test() {
        GatewayResource gatewayResource = new GatewayResource();

        ProjectResource p = (ProjectResource) gatewayResource.create(ResourceType.PROJECT);
        p.setName("abc");

        gatewayResource.save();
    }
}
