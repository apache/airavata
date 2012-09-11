package org.apache.airavata.persistance.registry.jpa;

import junit.framework.TestCase;
import org.apache.airavata.persistance.registry.jpa.model.Project;
import org.apache.airavata.persistance.registry.jpa.resources.GatewayResource;
import org.apache.airavata.persistance.registry.jpa.resources.HostDescriptorResource;
import org.apache.airavata.persistance.registry.jpa.resources.ProjectResource;
import org.apache.airavata.persistance.registry.jpa.resources.WorkerResource;

public class GatewayResourceTest extends TestCase {
    private GatewayResource gatewayResource;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        gatewayResource = new GatewayResource();
        gatewayResource.setGatewayName("default");
        gatewayResource.setOwner("default");
    }

    public void testSave() throws Exception {

    }

    public void testCreate() throws Exception {
        boolean result;
        HostDescriptorResource hostDescriptorResource = (HostDescriptorResource)gatewayResource.create(ResourceType.HOST_DESCRIPTOR);
        hostDescriptorResource.setHostDescName("Localhost");
        hostDescriptorResource.setUserName("admin");
        hostDescriptorResource.save();
        result = gatewayResource.isExists(ResourceType.HOST_DESCRIPTOR, "Localhost");
        assertTrue("The result doesn't exists", result == true);

        ProjectResource projectResource = (ProjectResource)gatewayResource.create(ResourceType.PROJECT);
        projectResource.setName("project1");
        WorkerResource workerResource = new WorkerResource();
        workerResource.setGateway(gatewayResource);
        workerResource.setUser("admin");
        projectResource.setWorker(workerResource);
        projectResource.save();
//        result = workerResource.isProjectExists("project1");
//        assertTrue("The result doesn't exists", result == true);

    }

    public void testIsExists() throws Exception {
        boolean result = gatewayResource.isExists(ResourceType.HOST_DESCRIPTOR, "Localhost");

        assertTrue("The result doesn't exists", result == true);

//        result = gatewayResource.isExists(ResourceType.USER, "admin");
//
//        assertTrue("The result exisits", result);
    }
}
