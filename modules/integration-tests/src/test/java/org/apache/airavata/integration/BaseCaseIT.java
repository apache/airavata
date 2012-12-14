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
import org.apache.airavata.common.utils.Version;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.PasswordCallback;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.DataType;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.ParameterType;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.workflow.model.wf.WorkflowInput;
import org.apache.airavata.ws.monitor.Monitor;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Integration test class.
 */
public class BaseCaseIT {

    private static final Logger log = LoggerFactory.getLogger(BaseCaseIT.class);

    private static int port;
    private static String serverUrl;
    private static String serverContextName;

    private static String registryURL;

    private static String gatewayName = "default";
    private static String userName = "admin";
    private static String password = "admin";

    private static final int TIME_OUT = 20000;

    private static final int TRIES = 3;

    private AiravataAPI airavataAPI;

    protected static void log(String message) {
        log.info(message);
    }

    public static Logger getLog() {
        return log;
    }

    public static int getPort() {
        return port;
    }

    public static String getServerUrl() {
        return serverUrl;
    }

    public static String getServerContextName() {
        return serverContextName;
    }

    public static String getRegistryURL() {
        return registryURL;
    }

    public static String getGatewayName() {
        return gatewayName;
    }

    public static String getUserName() {
        return userName;
    }

    public AiravataAPI getAiravataAPI() {
        return airavataAPI;
    }

    public static String getPassword() {
        return password;
    }

    @BeforeClass
    public static void setUpEnvironment() throws Exception{

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


    protected static void checkServerStartup (AiravataAPI airavataAPI) throws Exception {

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

    @Before
    public void setUp() throws Exception {

        PasswordCallback passwordCallback = new PasswordCallbackImpl();
        this.airavataAPI = AiravataAPIFactory.getAPI(new URI(getRegistryURL()),
                getGatewayName(), getUserName(), passwordCallback);
    }

    @Test
    public void testSetup() {

        Version version = this.airavataAPI.getVersion();

        Assert.assertNotNull(version);

        log("Airavata version - " + version.getFullVersion());

    }

    @Test
    public void testURLs() throws AiravataAPIInvocationException {
        URI eventingServiceURL = this.airavataAPI.getAiravataManager().getEventingServiceURL();
        Assert.assertNotNull(eventingServiceURL);

        URI gFaCURL = this.airavataAPI.getAiravataManager().getGFaCURL();
        Assert.assertNotNull(gFaCURL);

        URI messageBoxServiceURL = this.airavataAPI.getAiravataManager().getMessageBoxServiceURL();
        Assert.assertNotNull(messageBoxServiceURL);
    }

    @Test
    public void testEchoService() throws Exception {
        HostDescription hostDescription = new HostDescription(GlobusHostType.type);
        hostDescription.getType().setHostName("localhost");
        hostDescription.getType().setHostAddress("127.0.0.1");

        log("Saving host description ....");
        airavataAPI.getApplicationManager().saveHostDescription(hostDescription);

        Assert.assertTrue(airavataAPI.getApplicationManager().isHostDescriptorExists(hostDescription.getType()
                .getHostName()));

        ServiceDescription serviceDescription = new ServiceDescription();
        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        serviceDescription.getType().setName("Echo");
        serviceDescription.getType().setDescription("Echo service");
        InputParameterType parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("echo_input");
        parameter.setParameterDescription("echo input");
        ParameterType parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.STRING);
        parameterType.setName("String");
        inputParameters.add(parameter);

        OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("echo_output");
        outputParameter.setParameterDescription("Echo output");
        ParameterType outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.STRING);
        outputParaType.setName("String");
        outputParameters.add(outputParameter);

        serviceDescription.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[]{}));
        serviceDescription.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[]{}));

        log("Saving service description ...");
        airavataAPI.getApplicationManager().saveServiceDescription(serviceDescription);
        Assert.assertTrue(airavataAPI.getApplicationManager().isServiceDescriptorExists(serviceDescription.
                getType().getName()));

        // Deployment descriptor
        ApplicationDeploymentDescription applicationDeploymentDescription = new ApplicationDeploymentDescription();
        ApplicationDeploymentDescriptionType applicationDeploymentDescriptionType
                = applicationDeploymentDescription.getType();
        applicationDeploymentDescriptionType.addNewApplicationName().setStringValue("EchoApplication");
        applicationDeploymentDescriptionType.setExecutableLocation("/bin/echo");
        applicationDeploymentDescriptionType.setScratchWorkingDirectory("/tmp");

        log("Saving deployment description ...");
        airavataAPI.getApplicationManager().saveDeploymentDescription(serviceDescription.getType().getName(),
                hostDescription.getType().getHostName(), applicationDeploymentDescription);
        Assert.assertTrue(airavataAPI.getApplicationManager().isDeploymentDescriptorExists(serviceDescription.getType().
                getName(), hostDescription.getType().getHostName(),
                applicationDeploymentDescription.getType().getApplicationName().getStringValue()));

        log("Saving workflow ...");
        Workflow workflow = new Workflow(getWorkflowComposeContent("src/test/resources/EchoWorkflow.xwf"));
        airavataAPI.getWorkflowManager().
                saveWorkflow(workflow);

        Assert.assertTrue(airavataAPI.getWorkflowManager().isWorkflowExists(workflow.getName()));

        log("Workflow setting up completed ...");

        runWorkFlow(workflow, Arrays.asList("echo_output=Airavata Test"));
    }

    protected void runWorkFlow(Workflow workflow, List<String> inputValues) throws Exception {

        AiravataAPI airavataAPI = AiravataAPIFactory.getAPI(new URI(getRegistryURL()), getGatewayName(),
                getUserName(), new PasswordCallbackImpl());
        String workflowName = workflow.getName();
        List<WorkflowInput> workflowInputs = airavataAPI.getWorkflowManager().getWorkflowInputs(workflowName);

        Assert.assertEquals(workflowInputs.size(), inputValues.size());

        int i = 0;
        for (String valueString : inputValues) {
            workflowInputs.get(i).setValue(valueString);
            ++i;
        }

        String result
                = airavataAPI.getExecutionManager().runExperiment(workflowName, workflowInputs, getUserName(), "",
                workflowName);

        Assert.assertNotNull(result);

        log.info("Run workflow completed ....");
        log.info("Starting monitoring ....");

        monitor(result);

    }

    protected String getWorkflowComposeContent(String fileName) throws IOException {

        File f = new File(".");
        log.info(f.getAbsolutePath());

        File echoWorkflow = new File(fileName);
        if (!echoWorkflow.exists()) {
            fileName = "modules/tomcat-distribution/src/test/resources/EchoWorkflow.xwf";
        }

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        StringBuilder buffer = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }

        log.debug("Workflow compose - " + buffer.toString());
        return buffer.toString();
    }

    public void monitor(String experimentId) throws Exception {
        AiravataAPI airavataAPI = AiravataAPIFactory.getAPI(new URI(getRegistryURL()), getGatewayName(),
                getUserName(), new PasswordCallbackImpl() );
        Monitor experimentMonitor = airavataAPI.getExecutionManager().getExperimentMonitor(experimentId,     // TODO what is experiment name ?
                new TestMonitorListener(this.airavataAPI, experimentId));
        experimentMonitor.startMonitoring();
    }



}
