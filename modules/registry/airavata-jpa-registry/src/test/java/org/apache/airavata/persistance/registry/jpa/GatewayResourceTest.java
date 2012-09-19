package org.apache.airavata.persistance.registry.jpa;

import junit.framework.TestCase;
import org.apache.airavata.persistance.registry.jpa.model.Project;
import org.apache.airavata.persistance.registry.jpa.resources.*;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

public class GatewayResourceTest extends TestCase {
    private GatewayResource gatewayResource;

    @Override
    public void setUp() throws Exception {
        super.setUp();
//        java.util.Date date= new java.util.Date();
//        Timestamp time = new Timestamp(date.getTime());
//        ExperimentResource experimentResource = new ExperimentResource();
//        experimentResource.setExpID("Experiement_ID1");
//        ExperimentDataResource experimentDataResource = (ExperimentDataResource)experimentResource.create(ResourceType.EXPERIMENT_DATA);
//        experimentDataResource.setExpName("name1");
//        experimentDataResource.setUserName("admin");
//        experimentDataResource.save();

//        ExperimentMetadataResource experimentMetadataResource = (ExperimentMetadataResource)experimentDataResource.create(ResourceType.EXPERIMENT_METADATA);

//        experimentMetadataResource.setMetadata("test metadata");
//        experimentMetadataResource.save();

//        ExperimentDataResource experimentDataResource = (ExperimentDataResource)experimentResource.get(ResourceType.EXPERIMENT_DATA, "Experiement_ID1");
//        WorkflowDataResource workflowDataResource = (WorkflowDataResource)experimentDataResource.get(ResourceType.WORKFLOW_DATA, "workflow1");
//        NodeDataResource nodeDataResource = (NodeDataResource)workflowDataResource.create(ResourceType.NODE_DATA);
//        GramDataResource gramDataResource = (GramDataResource)workflowDataResource.create(ResourceType.GRAM_DATA);

//        NodeDataResource nodeDataResource = new NodeDataResource();

//        nodeDataResource.setWorkflowDataResource(workflowDataResource);
//        nodeDataResource.setLastUpdateTime(time);
//        nodeDataResource.setStartTime(time);
//        nodeDataResource.setInputs("inputs");
//        nodeDataResource.setOutputs("outputs");
//        nodeDataResource.setNodeID("node1");
//        nodeDataResource.setNodeType("nodeType1");
//        nodeDataResource.setStatus("OKState");
//        nodeDataResource.save();

//        GramDataResource gramDataResource = new GramDataResource();
//        gramDataResource.setWorkflowDataResource(workflowDataResource);
//        gramDataResource.setNodeID("node1");
//        gramDataResource.setRsl("rsl");
//        gramDataResource.setInvokedHost("host1");
//        gramDataResource.setLocalJobID("job1");
//        gramDataResource.save();
//
//        nodeDataResource = (NodeDataResource)workflowDataResource.get(ResourceType.NODE_DATA, "node1");
//        System.out.println(nodeDataResource.getInputs());
//        System.out.println(nodeDataResource.getOutputs());
//        System.out.println(nodeDataResource.getLastUpdateTime());
//        System.out.println(nodeDataResource.getNodeID());
//        System.out.println(nodeDataResource.getNodeType());
//        System.out.println(nodeDataResource.getStartTime());
//        System.out.println(nodeDataResource.getStatus());
//        System.out.println(nodeDataResource.getWorkflowDataResource().getWorkflowInstanceID());
//
//        gramDataResource = (GramDataResource)workflowDataResource.get(ResourceType.GRAM_DATA, "node1");
//        System.out.println(gramDataResource.getWorkflowDataResource().getWorkflowInstanceID());
//        System.out.println(gramDataResource.getInvokedHost());
//        System.out.println(gramDataResource.getLocalJobID());
//        System.out.println(gramDataResource.getNodeID());
//        System.out.println(gramDataResource.getRsl());




//        workflowDataResource.setWorkflowInstanceID("workflow1");
//        workflowDataResource.setStatus("OKstatus");
//        workflowDataResource.setTemplateName("workflow1");
//        workflowDataResource.setLastUpdatedTime(time);
//        workflowDataResource.setStartTime(time);
//        workflowDataResource.save();


//        System.out.println("Exp ID : " +workflowDataResource.getExperimentID());
//        System.out.println("WF Name : " + workflowDataResource.getWorkflowInstanceID());
//
//        System.out.println("Status : " +workflowDataResource.getStatus());

//        experimentMetadataResource = (ExperimentMetadataResource)experimentDataResource.get(ResourceType.EXPERIMENT_METADATA, "Experiement_ID1");
//        System.out.println("exp metadata : " + experimentMetadataResource.getMetadata());



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
//        ServiceDescriptorResource serviceDescriptorResource = gatewayResource.createServiceDescriptorResource("bb");
//        serviceDescriptorResource.setUserName("admin");
//        serviceDescriptorResource.setContent("ccccc");
//        serviceDescriptorResource.save();
//
//        ApplicationDescriptorResource applicationDescriptorResource = gatewayResource.createApplicationDescriptorResource("test");
//        applicationDescriptorResource.setUpdatedUser("admin");
//        applicationDescriptorResource.setContent("abc");
//        applicationDescriptorResource.setHostDescName("aa");
//        applicationDescriptorResource.setServiceDescName("bb");
//        applicationDescriptorResource.save();
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
