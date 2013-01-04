package org.apache.airavata.api.samples;

import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.client.api.DescriptorRecordAlreadyExistsException;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.PasswordCallback;
import org.apache.airavata.rest.client.PasswordCallbackImpl;
import org.apache.airavata.schemas.gfac.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class CreateApplication {
    private static final Logger log = LoggerFactory.getLogger(CreateApplication.class);

    private static int port;
    private static String serverUrl;
    private static String serverContextName;

    private static String registryURL;

    private static String gatewayName = "default";
    private static String userName = "admin";
    private static String password = "admin";

    private static AiravataAPI airavataAPI;

    public static void main(String[] args) throws AiravataAPIInvocationException, IOException, URISyntaxException, DescriptorRecordAlreadyExistsException {

        //creating airavata client object //
        port = Integer.parseInt("8080");
        serverUrl = "localhost";
        serverContextName = "airavata-registry";

        log.info("Configurations - port : " + port);
        log.info("Configurations - serverUrl : " + serverUrl);
        log.info("Configurations - serverContext : " + serverContextName);

        registryURL = "http://" + serverUrl + ":" + port + "/" + serverContextName + "/api";

        log.info("Configurations - Registry URL : " + registryURL);

        PasswordCallback passwordCallback = new PasswordCallbackImpl(getUserName(), getPassword());
        airavataAPI = AiravataAPIFactory.getAPI(new URI(getRegistryURL()),
                getGatewayName(), getUserName(), passwordCallback);

        //Now creating documents to be saved in to registry using above created airavata client object//

        HostDescription descriptor = new HostDescription(GlobusHostType.type);
        descriptor.getType().setHostName("localhost");
        descriptor.getType().setHostAddress("127.0.0.1");

        log.info("Saving host description ....");
        airavataAPI.getApplicationManager().saveHostDescription(descriptor);

        ServiceDescription serviceDescription = new ServiceDescription();
        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        serviceDescription.getType().setName("Echo");
        serviceDescription.getType().setDescription("Echo service");
        //Creating input parameters
        InputParameterType parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("echo_input");
        parameter.setParameterDescription("echo input");
        ParameterType parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.STRING);
        parameterType.setName("String");
        inputParameters.add(parameter);

        //Creating output parameters
        OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("echo_output");
        outputParameter.setParameterDescription("Echo output");
        ParameterType outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.STRING);
        outputParaType.setName("String");
        outputParameters.add(outputParameter);

        //Setting input and output parameters to serviceDescriptor
        serviceDescription.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[]{}));
        serviceDescription.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[]{}));

        log.info("Saving service description ...");
        //Saving service descriptor
        airavataAPI.getApplicationManager().saveServiceDescription(serviceDescription);

        // Deployment descriptor creation
        ApplicationDescription applicationDeploymentDescription = new ApplicationDescription();
        ApplicationDeploymentDescriptionType applicationDeploymentDescriptionType
                = applicationDeploymentDescription.getType();
        applicationDeploymentDescriptionType.addNewApplicationName().setStringValue("EchoApplication");
        applicationDeploymentDescriptionType.setExecutableLocation("/bin/echo");
        applicationDeploymentDescriptionType.setScratchWorkingDirectory("/tmp");

        log.info("Saving deployment description ...");

        //Saving deployment Descriptor with an association with given serviceName, Host name
        airavataAPI.getApplicationManager().addApplicationDescription(serviceDescription,
                descriptor, applicationDeploymentDescription);
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

    public static String getPassword() {
        return password;
    }
}
