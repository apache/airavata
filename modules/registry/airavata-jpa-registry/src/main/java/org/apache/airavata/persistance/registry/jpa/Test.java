package org.apache.airavata.persistance.registry.jpa;

import org.apache.airavata.persistance.registry.jpa.model.Project;
import org.apache.airavata.persistance.registry.jpa.model.Users;
import org.apache.airavata.persistance.registry.jpa.resources.ApplicationDescriptorResource;
import org.apache.airavata.persistance.registry.jpa.resources.GatewayResource;
import org.apache.airavata.persistance.registry.jpa.resources.HostDescriptorResource;
import org.apache.airavata.persistance.registry.jpa.resources.ProjectResource;
import org.apache.airavata.persistance.registry.jpa.resources.PublishWorkflowResource;
import org.apache.airavata.persistance.registry.jpa.resources.ServiceDescriptorResource;
import org.apache.airavata.persistance.registry.jpa.resources.UserResource;
import org.apache.airavata.registry.api.PublishedWorkflowRegistry;

import java.sql.Date;


public class Test {

    public static void main(String[] args) {
        GatewayResource gatewayResource = new GatewayResource();
        gatewayResource.setName("abc");
//        gatewayResource.setGatewayID(1);

        // ProjectResource projectResource = (ProjectResource)gatewayResource.create(ResourceType.PROJECT);
        UserResource userResource = (UserResource)gatewayResource.create(ResourceType.USER);
        //PublishWorkflowResource publishWorkflowResource = (PublishWorkflowResource)gatewayResource.create(ResourceType.PUBLISHED_WORKFLOW);
        //ServiceDescriptorResource serviceDescriptorResource = (ServiceDescriptorResource) gatewayResource.create(ResourceType.SERVICE_DESCRIPTOR);
        //HostDescriptorResource hostDescriptorResource = (HostDescriptorResource) gatewayResource.create(ResourceType.HOST_DESCRIPTOR);
        //ApplicationDescriptorResource applicationDescriptorResource = (ApplicationDescriptorResource) gatewayResource.create(ResourceType.APPLICATION_DESCRIPTOR);


        userResource.setUserName("chathuri");
        userResource.setPassword("11111111");

       // projectResource.setName("myproject");

        //userResource.setProjectResource(projectResource);

        //Date date = new Date(2012, 9, 5);

        //publishWorkflowResource.setPublishedDate(date);
        //publishWorkflowResource.setContent("mycontent");
        //publishWorkflowResource.setVersion("1");

        gatewayResource.save();
        userResource.save();
        //projectResource.save();
        //publishWorkflowResource.save();

    }

    public void test() {

        GatewayResource gatewayResource = new GatewayResource();

        ProjectResource p = (ProjectResource) gatewayResource.create(ResourceType.PROJECT);
        p.setName("abc");

        gatewayResource.save();
    }
}
