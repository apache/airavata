package org.apache.airavata.persistance.registry.jpa;

import org.apache.airavata.persistance.registry.jpa.resources.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


public class Test {

    public static void main(String[] args) {
//        String PERSISTENCE_UNIT_NAME = "airavata_data";
//        EntityManagerFactory factory;
//        EntityManager em;
//        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
//        em = factory.createEntityManager();

        GatewayResource gatewayResource = new GatewayResource();
        gatewayResource.setGatewayName("default");
        gatewayResource.setOwner("default");

        gatewayResource.isExists(ResourceType.HOST_DESCRIPTOR, "Localhost");
//
//        boolean state = gatewayResource.isExists(ResourceType.HOST_DESCRIPTOR, "Localhost");
//        System.out.println(state);

        ConfigurationResource configurationResource = new ConfigurationResource();
        ResourceUtils.removeConfiguration("messagebox.url","http://140.182.199.161:8080/axis2/services/MsgBoxService");

//        GatewayResource gatewayResource = new GatewayResource();
//        gatewayResource.setGatewayName("abc");
//        gatewayResource.setOwner("lahiru");
//
//        WorkerResource workerResource = (WorkerResource)gatewayResource.create(ResourceType.GATEWAY_WORKER);
//        workerResource.setUser("aaaaa");
//        workerResource.setGateway(gatewayResource);
//
//        UserResource userResource = (UserResource)gatewayResource.create(ResourceType.USER);
//        userResource.setUserName("aaaaa");
//        userResource.setPassword("11111111");
//
//        ConfigurationResource configurationResource = new ConfigurationResource();
////        configurationResource.setConfigKey();
//
//
////        gatewayResource.save();
////        userResource.save();
//        workerResource.save();


    }

    public void test() {

        GatewayResource gatewayResource = new GatewayResource();

        ProjectResource p = (ProjectResource) gatewayResource.create(ResourceType.PROJECT);
        p.setName("abc");

        gatewayResource.save();
    }
}
