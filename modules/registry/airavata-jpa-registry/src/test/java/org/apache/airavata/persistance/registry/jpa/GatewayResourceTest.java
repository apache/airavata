package org.apache.airavata.persistance.registry.jpa;

import junit.framework.TestCase;
import org.apache.airavata.persistance.registry.jpa.model.Project;
import org.apache.airavata.persistance.registry.jpa.resources.*;

import java.util.Calendar;
import java.util.List;

public class GatewayResourceTest extends TestCase {
    private GatewayResource gatewayResource;

    @Override
    public void setUp() throws Exception {
        super.setUp();




//        gatewayResource = new GatewayResource();
//        gatewayResource.setGatewayName("default");
//        gatewayResource.setOwner("default");
//        gatewayResource.save();
//        UserResource userResource = (UserResource)gatewayResource.create(ResourceType.USER);
//        userResource.setUserName("admin");
//        userResource.setPassword("admin");
//        userResource.save();
//        WorkerResource workerResource = (WorkerResource)gatewayResource.create(ResourceType.GATEWAY_WORKER);
//        workerResource.setUser(userResource.getUserName());
//        workerResource.save();

    }

    public void testSave() throws Exception {
//        Calendar cal = Calendar.getInstance();
//        cal.set( cal.YEAR, 1970 );
//        cal.set( cal.MONTH, cal.JANUARY );
//        cal.set( cal.DATE, 1 );
//
//        cal.set( cal.HOUR_OF_DAY, 0 );
//        cal.set( cal.MINUTE, 0 );
//        cal.set( cal.SECOND, 0 );
//        cal.set( cal.MILLISECOND, 0 );
//
//        java.sql.Date jsqlD =
//                new java.sql.Date( cal.getTime().getTime() );
//
//        ConfigurationResource config = ResourceUtils.createConfiguration("aa");
//        config.setConfigVal("http://129.79.49.142:8080/axis2/services/WorkflowInterpretor/aaaa");
//        config.setExpireDate(jsqlD);
//        config.save();

//        ConfigurationResource configurationResource = new ConfigurationResource();
//        configurationResource.setConfigKey("cc");
//        configurationResource.setConfigVal("http://129.79.49.142:8080/axis2/services/WorkflowInterpretor/aaaa");
//        configurationResource.setExpireDate(jsqlD);
//        configurationResource.save();
//        List<HostDescriptorResource>  list = gatewayResource.getHostDescriptorResources();
//        for(HostDescriptorResource resource : list) {
//            System.out.println("Host Descriptor name :" +  resource.getHostDescName());
//        }
//        Thread t = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                ResourceUtils.removeConfiguration("aa", "http://129.79.49.142:8080/axis2/services/WorkflowInterpretor");
//                }
//            }
//        });
//        Thread t2 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    ConfigurationResource configurationResource = new ConfigurationResource();
//                    configurationResource.setConfigKey("aa");
//                    configurationResource.setConfigVal("http://129.79.49.142:8080/axis2/services/WorkflowInterpretor");
//                    Calendar calendar = Calendar.getInstance();
//                    //configurationResource.setExpireDate(calendar.);
//                    configurationResource.save();
//                }
//            }
//        });
//        t.start();
//        t2.start();
//        while(true) {
//            Thread.sleep(10000);
//        }

    }

    public void testCreate() throws Exception {
//        boolean result;
//        HostDescriptorResource hostDescriptorResource = gatewayResource.createHostDescriptorResource("Localhost");
////        hostDescriptorResource.setHostDescName("Localhost");
//        hostDescriptorResource.setUserName("admin");
//        hostDescriptorResource.setContent("<hostDescription xmlns=\"http://schemas.airavata.apache.org/gfac/type\">\n" +
//                " <hostName>LocalHost</hostName>\n" +
//                " <hostAddress>127.0.0.1</hostAddress>\n" +
//                "</hostDescription>");
//        hostDescriptorResource.save();
//        result = gatewayResource.isExists(ResourceType.HOST_DESCRIPTOR, "Localhost");
//        assertTrue("The result doesn't exists", result == true);


//        ProjectResource projectResource = (ProjectResource)gatewayResource.create(ResourceType.PROJECT);
//        projectResource.setName("project1");
//        WorkerResource workerResource = new WorkerResource();
//        workerResource.setGateway(gatewayResource);
//        workerResource.setUser("admin");
//        projectResource.setWorker(workerResource);
//        projectResource.save();
//        result = workerResource.isProjectExists("project1");
//        assertTrue("The result doesn't exists", result == true);

    }

    public void testIsExists() throws Exception {
//        boolean result = gatewayResource.isExists(ResourceType.HOST_DESCRIPTOR, "Localhost");
//
//        assertTrue("The result doesn't exists", result == true);

//        result = gatewayResource.isExists(ResourceType.USER, "admin");
//
//        assertTrue("The result exisits", result);
    }
}
