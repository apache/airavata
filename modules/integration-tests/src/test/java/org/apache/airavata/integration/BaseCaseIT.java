package org.apache.airavata.integration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.client.api.DescriptorRecordAlreadyExistsException;
import org.apache.airavata.client.api.ExperimentAdvanceOptions;
import org.apache.airavata.client.api.builder.DescriptorBuilder;
import org.apache.airavata.common.utils.Version;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.PasswordCallback;
import org.apache.airavata.registry.api.impl.WorkflowExecutionDataImpl;
import org.apache.airavata.registry.api.workflow.ExperimentData;
import org.apache.airavata.registry.api.workflow.NodeExecutionData;
import org.apache.airavata.schemas.gfac.DataType;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowInput;
import org.apache.airavata.ws.monitor.EventData;
import org.apache.airavata.ws.monitor.EventDataListenerAdapter;
import org.apache.airavata.ws.monitor.EventDataRepository;
import org.apache.airavata.ws.monitor.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


/**
 * Integration test class.
 */
public class BaseCaseIT {

    private final Logger log = LoggerFactory.getLogger(BaseCaseIT.class);

    private int port;
    private String serverUrl;
    private String serverContextName;

    private String registryURL;

    private String gatewayName = "default";
    private String userName = "admin";
    private String password = "admin";

    private static final int TIME_OUT = 20000;

    private static final int TRIES = 3;

    private AiravataAPI airavataAPI;

    protected void log(String message) {
        log.info(message);
    }

    public Logger getLog() {
        return log;
    }

    public int getPort() {
        return port;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getServerContextName() {
        return serverContextName;
    }

    public String getRegistryURL() {
        return registryURL;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public String getUserName() {
        return userName;
    }

    public AiravataAPI getAiravataAPI() {
        return airavataAPI;
    }

    public String getPassword() {
        return password;
    }

    public BaseCaseIT() throws Exception {
        setUpEnvironment();
    }

    public void setUpEnvironment() throws Exception{

        log("..................Validating server logs .............................");
        //TODO validate logs

        log("Reading test server configurations ...");

        String strPort = System.getProperty("test.server.port");

        if (strPort == null) {
            strPort = "8080";
        }

        String strHost = System.getProperty("test.server.url");

        if (strHost == null) {
            strHost = "localhost";
        }

        String strContext = System.getProperty("test.server.context");

        if (strContext == null) {
            strContext = "airavata-registry";
        }

        port = Integer.parseInt(strPort);
        serverUrl = strHost;
        serverContextName = strContext;

        log("Configurations - port : " + port);
        log("Configurations - serverUrl : " + serverUrl);
        log("Configurations - serverContext : " + serverContextName);

        registryURL = "http://" + serverUrl + ":" + port + "/" + serverContextName + "/api";

        log("Configurations - Registry URL : " + registryURL);


        PasswordCallback passwordCallback = new PasswordCallbackImpl();
        AiravataAPI airavataAPI = AiravataAPIFactory.getAPI(new URI(getRegistryURL()),
                getGatewayName(), getUserName(), passwordCallback);

        checkServerStartup(airavataAPI);


        log("Server successfully started .............................");
        log("Running tests .............................");
    }


    protected void checkServerStartup (AiravataAPI airavataAPI) throws Exception {

        int tries = 0;

        while (true) {

            if (tries == TRIES) {
                log("Server not responding. Cannot continue with integration tests ...");
                throw new Exception("Server not responding !");
            }

            log("Checking server is running, try - " + tries);

            URI eventingServiceURL = airavataAPI.getAiravataManager().getEventingServiceURL();

            URI gFaCURL = airavataAPI.getAiravataManager().getGFaCURL();

            URI messageBoxServiceURL = airavataAPI.getAiravataManager().getMessageBoxServiceURL();

            URI workflowInterpreterServiceURL = airavataAPI.getAiravataManager().getWorkflowInterpreterServiceURL();

            if (eventingServiceURL == null ||
                    gFaCURL == null ||
                    messageBoxServiceURL == null ||
                    workflowInterpreterServiceURL == null) {

                log.info("Waiting till server initializes ........");
                Thread.sleep(TIME_OUT);
            } else {
                break;
            }

            ++tries;
        }

    }

    @BeforeTest
    public void setUp() throws Exception {

        PasswordCallback passwordCallback = new PasswordCallbackImpl();
        this.airavataAPI = AiravataAPIFactory.getAPI(new URI(getRegistryURL()),
                getGatewayName(), getUserName(), passwordCallback);
    }

    @Test(groups = { "setupTests" })
    public void testSetup() {

        Version version = this.airavataAPI.getVersion();

        Assert.assertNotNull(version);

        log("Airavata version - " + version.getFullVersion());

    }

    @Test(groups = { "setupTests" }, dependsOnMethods = { "testSetup" })
    public void testURLs() throws AiravataAPIInvocationException {
        URI eventingServiceURL = this.airavataAPI.getAiravataManager().getEventingServiceURL();
        Assert.assertNotNull(eventingServiceURL);

        URI gFaCURL = this.airavataAPI.getAiravataManager().getGFaCURL();
        Assert.assertNotNull(gFaCURL);

        URI messageBoxServiceURL = this.airavataAPI.getAiravataManager().getMessageBoxServiceURL();
        Assert.assertNotNull(messageBoxServiceURL);
    }

    @Test(groups = { "echoGroup" }, dependsOnGroups = { "setupTests" })
    public void testEchoService() throws Exception {

        DescriptorBuilder descriptorBuilder = airavataAPI.getDescriptorBuilder();

        HostDescription hostDescription
                = descriptorBuilder.buildHostDescription(HostDescriptionType.type, "localhost", "127.0.0.1");

        log("Adding host description ....");
        airavataAPI.getApplicationManager().addHostDescription(hostDescription);

        Assert.assertTrue(airavataAPI.getApplicationManager().isHostDescriptorExists(hostDescription.getType()
                .getHostName()));


        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        inputParameters.add(descriptorBuilder.buildInputParameterType("echo_input", "echo input", DataType.STRING));

        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        outputParameters.add(descriptorBuilder.buildOutputParameterType("echo_output", "Echo output", DataType.STRING));

        ServiceDescription serviceDescription = descriptorBuilder.buildServiceDescription("Echo", "Echo service",
                inputParameters, outputParameters);

        log("Adding service description ...");
        airavataAPI.getApplicationManager().addServiceDescription(serviceDescription);
        Assert.assertTrue(airavataAPI.getApplicationManager().isServiceDescriptorExists(serviceDescription.
                getType().getName()));

        // Deployment descriptor
        ApplicationDescription applicationDeploymentDescription
                = descriptorBuilder.buildApplicationDeploymentDescription("EchoApplication", "/bin/echo", "/tmp");

        log("Adding deployment description ...");
        airavataAPI.getApplicationManager().addApplicationDescription(serviceDescription,
                hostDescription, applicationDeploymentDescription);

        Assert.assertTrue(airavataAPI.getApplicationManager().isApplicationDescriptorExists(serviceDescription.getType().
                getName(), hostDescription.getType().getHostName(),
                applicationDeploymentDescription.getType().getApplicationName().getStringValue()));

        log("Saving workflow ...");
        Workflow workflow = new Workflow(getWorkflowComposeContent("src/test/resources/EchoWorkflow.xwf"));
        airavataAPI.getWorkflowManager().addWorkflow(workflow);

        Assert.assertTrue(airavataAPI.getWorkflowManager().isWorkflowExists(workflow.getName()));

        log("Workflow setting up completed ...");

        runWorkFlow(workflow, Arrays.asList("echo_output=Airavata Test"));
    }

    @Test(groups = { "performanceTesting" } )
    public void testExistsHostDescriptor() throws AiravataAPIInvocationException {

        airavataAPI.getApplicationManager().isHostDescriptorExists("localhost");
    }

    @Test(groups = { "echoGroup" }/*, dependsOnMethods = { "testEchoService" }*/)
    public void testUpdateEchoService() throws AiravataAPIInvocationException, IOException, ComponentException, GraphException {

        DescriptorBuilder descriptorBuilder = airavataAPI.getDescriptorBuilder();

        HostDescription hostDescription
                = descriptorBuilder.buildHostDescription(HostDescriptionType.type, "localhost", "127.0.0.1");

        log("Trying to add host description ....");
        try {
            airavataAPI.getApplicationManager().addHostDescription(hostDescription);
            Assert.fail("Host Descriptor should already exists and should go to update.");
        } catch (DescriptorRecordAlreadyExistsException e) {

            log("Updating host description ....");
            airavataAPI.getApplicationManager().updateHostDescription(hostDescription);
        }

        Assert.assertTrue(airavataAPI.getApplicationManager().isHostDescriptorExists(hostDescription.getType()
                .getHostName()));


        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        inputParameters.add(descriptorBuilder.buildInputParameterType("echo_input", "echo input", DataType.STRING));

        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        outputParameters.add(descriptorBuilder.buildOutputParameterType("echo_output", "Echo output", DataType.STRING));

        ServiceDescription serviceDescription = descriptorBuilder.buildServiceDescription("Echo", "Echo service",
                inputParameters, outputParameters);

        log("Adding service description ...");
        try {
            airavataAPI.getApplicationManager().addServiceDescription(serviceDescription);
            Assert.fail("Service Descriptor should already exists and should go to update.");
        } catch (DescriptorRecordAlreadyExistsException e) {

            log("Updating service description ....");
            airavataAPI.getApplicationManager().updateServiceDescription(serviceDescription);
        }

        Assert.assertTrue(airavataAPI.getApplicationManager().isServiceDescriptorExists(serviceDescription.
                getType().getName()));

        // Deployment descriptor
        ApplicationDescription applicationDeploymentDescription
                = descriptorBuilder.buildApplicationDeploymentDescription("EchoApplication", "/bin/echo", "/tmp");

        log("Adding deployment description ...");
        try {
            airavataAPI.getApplicationManager().addApplicationDescription(serviceDescription,
                    hostDescription, applicationDeploymentDescription);
            Assert.fail("Application Descriptor should already exists and should go to update.");
        } catch (DescriptorRecordAlreadyExistsException e) {

            log("Updating application description ....");
            airavataAPI.getApplicationManager().updateApplicationDescription(serviceDescription,
                    hostDescription, applicationDeploymentDescription);
        }

        Assert.assertTrue(airavataAPI.getApplicationManager().isApplicationDescriptorExists(serviceDescription.getType().
                getName(), hostDescription.getType().getHostName(),
                applicationDeploymentDescription.getType().getApplicationName().getStringValue()));

        log("Saving workflow ...");
        Workflow workflow = new Workflow(getWorkflowComposeContent("src/test/resources/EchoWorkflow.xwf"));

        try {
            airavataAPI.getWorkflowManager().addWorkflow(workflow);
            Assert.fail("Workflow should already exists and should go to update.");
        } catch (DescriptorRecordAlreadyExistsException e) {

            log("Updating workflow...");
            airavataAPI.getWorkflowManager().updateWorkflow(workflow);
        }

        Assert.assertTrue(airavataAPI.getWorkflowManager().isWorkflowExists(workflow.getName()));

        log("Workflow setting up completed ...");

        try {
        	runWorkFlowWithoutMonitor(workflow, Arrays.asList("echo_output=Airavata_Test"));
        } catch (Exception e) {
            log.error("An error occurred while invoking workflow", e);
        }
    }

    protected void runWorkFlow(Workflow workflow, List<String> inputValues) throws Exception {

        AiravataAPI airavataAPI = AiravataAPIFactory.getAPI(new URI(getRegistryURL()), getGatewayName(),
                getUserName(), new PasswordCallbackImpl());
        List<WorkflowInput> workflowInputs = setupInputs(workflow, inputValues);
        String workflowName = workflow.getName();
        ExperimentAdvanceOptions options = airavataAPI.getExecutionManager().createExperimentAdvanceOptions(workflowName,getUserName(),null);
        String experimentId = airavataAPI.getExecutionManager().runExperiment(workflowName, workflowInputs, options);
        
        Assert.assertNotNull(experimentId);

        log.info("Run workflow completed ....");
        log.info("Starting monitoring ....");

        monitor(experimentId);
    }
    
    protected void runWorkFlowWithoutMonitor(Workflow workflow, List<String> inputValues) throws Exception {

        AiravataAPI airavataAPI = AiravataAPIFactory.getAPI(new URI(getRegistryURL()), getGatewayName(),
                getUserName(), new PasswordCallbackImpl());
        List<WorkflowInput> workflowInputs = setupInputs(workflow, inputValues);
        String workflowName = workflow.getName();
        ExperimentAdvanceOptions options = airavataAPI.getExecutionManager().createExperimentAdvanceOptions(workflowName,getUserName(),null);
        String experimentId = airavataAPI.getExecutionManager().runExperiment(workflowName, workflowInputs, options, new EventDataListenerAdapter() {
			@Override
			public void notify(EventDataRepository eventDataRepo, EventData eventData) {
				//do nothing
			}
		});
        
        Assert.assertNotNull(experimentId);
        airavataAPI.getExecutionManager().waitForExperimentTermination(experimentId);
        
        log.info("Run workflow completed ....");
        log.info("Starting monitoring ....");

        verifyOutput(experimentId, "echo_output=Airavata_Test");
    }

    protected void verifyOutput(String experimentId, String outputVerifyingString) throws Exception {
    	AiravataAPI airavataAPI = AiravataAPIFactory.getAPI(new URI(getRegistryURL()), getGatewayName(),
                getUserName(), new PasswordCallbackImpl());
        log.info("Workflow Experiment ID Returned : " + experimentId);

        ExperimentData experimentData = airavataAPI.getProvenanceManager().getExperimentData(experimentId);

        log.info("Verifying output ...");

        List<WorkflowExecutionDataImpl> workflowInstanceData = experimentData.getWorkflowExecutionDataList();

        for(WorkflowExecutionDataImpl data:workflowInstanceData){
            List<NodeExecutionData> nodeDataList = data.getNodeDataList();
            for(NodeExecutionData nodeData:nodeDataList){
            	System.out.print("******************************");
            	System.out.println(nodeData.getOutputData().get(0).getValue());
                Assert.assertEquals("Airavata_Test", nodeData.getOutputData().get(0).getValue());
                Assert.assertEquals(outputVerifyingString, nodeData.getInputData().get(0).getValue());
            }
        }
    }
    
	private List<WorkflowInput> setupInputs(Workflow workflow,List<String> inputValues)	throws Exception {
		AiravataAPI airavataAPI = AiravataAPIFactory.getAPI(new URI(getRegistryURL()), getGatewayName(),
                getUserName(), new PasswordCallbackImpl());
		List<WorkflowInput> workflowInputs = airavataAPI.getWorkflowManager().getWorkflowInputs(workflow.getName());

        Assert.assertEquals(workflowInputs.size(), inputValues.size());

        int i = 0;
        for (String valueString : inputValues) {
            workflowInputs.get(i).setValue(valueString);
            ++i;
        }
		return workflowInputs;
	}

    protected String getWorkflowComposeContent(String fileName) throws IOException {
        File f = new File(".");
        log.info(f.getAbsolutePath());

        File echoWorkflow = new File(fileName);
        if (!echoWorkflow.exists()) {
            fileName = "modules/integration-tests/src/test/resources/EchoWorkflow.xwf";
        }

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        StringBuilder buffer = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
        log.debug("Workflow compose - " + buffer.toString());
        return buffer.toString();
    }

    public void monitor(String experimentId) throws Exception {
        AiravataAPI airavataAPI = AiravataAPIFactory.getAPI(new URI(getRegistryURL()), getGatewayName(),
                getUserName(), new PasswordCallbackImpl() );
        TestMonitorListener monitorListener = new TestMonitorListener(this.airavataAPI, experimentId);
		Monitor experimentMonitor = airavataAPI.getExecutionManager().getExperimentMonitor(experimentId,     // TODO what is experiment name ?
                monitorListener);
        experimentMonitor.startMonitoring();
        experimentMonitor.waitForCompletion();
    }



}
