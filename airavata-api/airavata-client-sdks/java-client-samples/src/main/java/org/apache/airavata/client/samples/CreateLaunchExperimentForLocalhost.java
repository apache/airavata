package org.apache.airavata.client.samples;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.client.tools.RegisterSampleApplicationsUtils;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationParallelismType;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.error.AiravataClientConnectException;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shameera on 9/30/14.
 */
public class CreateLaunchExperimentForLocalhost {

    private static final String THRIFT_SERVER_HOST = "127.0.0.1";
    private static final int THRIFT_SERVER_PORT = 8930;
    private static final String DEFAULT_GATEWAY = "Sample";

    private Airavata.Client airavataClient;
    private String localhostId;
    private String echoModuleId;
    private String addModuleId;
    private String multiplyModuleId;
    private String subtractModuleId;

    public static void main(String[] args) throws AiravataClientConnectException, TException {
        CreateLaunchExperimentForLocalhost worker = new CreateLaunchExperimentForLocalhost();
        worker.register();
    }


    public void register() throws AiravataClientConnectException, TException {
        airavataClient = AiravataClientFactory.createAiravataClient(THRIFT_SERVER_HOST, THRIFT_SERVER_PORT);

        registerLocalhost();
//        registerGatewayProfile();
        registerApplicationModules();
        registerApplicationDeployments();
        registerApplicationInterfaces();
    }

    private void registerGatewayProfile() throws TException {
        ComputeResourcePreference localhostResourcePreference = RegisterSampleApplicationsUtils.
             createComputeResourcePreference("localhost", "test", false, null, null, null,
                "/Users/shameera/work/source/git_airavata/modules/distribution/server/target/apache-airavata-server-0.14-SNAPSHOT/tmp");
        GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
        gatewayResourceProfile.setGatewayID(DEFAULT_GATEWAY);
        gatewayResourceProfile.setGatewayName(DEFAULT_GATEWAY);
        gatewayResourceProfile.addToComputeResourcePreferences(localhostResourcePreference);
        airavataClient.registerGatewayResourceProfile(gatewayResourceProfile);
    }

    private void registerLocalhost() {
//        try {
//            System.out.println("\n #### Registering Localhost Computational Resource #### \n");
//
//            ComputeResourceDescription computeResourceDescription = RegisterSampleApplicationsUtils.
//                    createComputeResourceDescription("localhost", "LocalHost", null, null);
//            localhostId = airavataClient.registerComputeResource(computeResourceDescription);
//            ResourceJobManager resourceJobManager = RegisterSampleApplicationsUtils.
//                    createResourceJobManager(ResourceJobManagerType.FORK, null, null, null);
//            LOCALSubmission submission = new LOCALSubmission();
//            submission.setResourceJobManager(resourceJobManager);
//            boolean localSubmission = airavataClient.addLocalSubmissionDetails(localhostId, 1, submission);
//            if (!localSubmission) throw new AiravataClientException();
//            System.out.println("LocalHost Resource Id is " + localhostId);
//
//        } catch (TException e) {
//            e.printStackTrace();
//        }
    }

    private void registerApplicationInterfaces() {
         registerAddApplicationInterface();
        registerSubtractApplicationInterface();
        registerMultiplyApplicationInterface();
        registerEchoInterface();
    }

    private void registerApplicationDeployments() throws TException {
        System.out.println("#### Registering Application Deployments on Localhost #### \n");
        //Register Echo
        String echoAppDeployId = airavataClient.registerApplicationDeployment(
                RegisterSampleApplicationsUtils.createApplicationDeployment(echoModuleId, localhostId,
                        "/Users/shameera/work/tryout/scripts/echo.sh", ApplicationParallelismType.SERIAL, "Echo application description"));
        System.out.println("Echo on localhost Id " + echoAppDeployId);

        //Register Add application
        String addAppDeployId = airavataClient.registerApplicationDeployment(
                RegisterSampleApplicationsUtils.createApplicationDeployment(addModuleId, localhostId,
                        "/Users/shameera/work/tryout/scripts/add.sh", ApplicationParallelismType.SERIAL, "Add application description"));
        System.out.println("Add on localhost Id " + addAppDeployId);

        //Register Multiply application
        String multiplyAppDeployId = airavataClient.registerApplicationDeployment(
                RegisterSampleApplicationsUtils.createApplicationDeployment(multiplyModuleId, localhostId,
                        "/Users/shameera/work/tryout/scripts/multiply.sh", ApplicationParallelismType.SERIAL, "Multiply application description"));
        System.out.println("Echo on localhost Id " + multiplyAppDeployId);

        //Register Subtract application
        String subtractAppDeployId = airavataClient.registerApplicationDeployment(
                RegisterSampleApplicationsUtils.createApplicationDeployment(subtractModuleId, localhostId,
                        "/Users/shameera/work/tryout/scripts/subtract.sh", ApplicationParallelismType.SERIAL, "Subtract application description "));
        System.out.println("Echo on localhost Id " + subtractAppDeployId);
    }

    private void registerApplicationModules() throws TException {
        //Register Echo
        echoModuleId = airavataClient.registerApplicationModule(
                RegisterSampleApplicationsUtils.createApplicationModule(
                        "Echo", "1.0", "Echo application description"));
        System.out.println("Echo Module Id " + echoModuleId);
        //Register Echo
        addModuleId = airavataClient.registerApplicationModule(
                RegisterSampleApplicationsUtils.createApplicationModule(
                        "Add", "1.0", "Add application description"));
        System.out.println("Add Module Id " + addModuleId);
        //Register Echo
        multiplyModuleId = airavataClient.registerApplicationModule(
                RegisterSampleApplicationsUtils.createApplicationModule(
                        "Multiply", "1.0", "Multiply application description"));
        System.out.println("Multiply Module Id " + multiplyModuleId);
        //Register Echo
        subtractModuleId = airavataClient.registerApplicationModule(
                RegisterSampleApplicationsUtils.createApplicationModule(
                        "Subtract", "1.0", "Subtract application description"));
        System.out.println("Subtract Module Id " + subtractModuleId);

    }


    public void registerEchoInterface() {
        try {
            System.out.println("#### Registering Echo Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(echoModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("Input_to_Echo", "Hello World",
                    DataType.STRING, null, false, "A test string to Echo", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Echoed_Output",
                    "", DataType.STRING);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            String echoInterfaceId = airavataClient.registerApplicationInterface(
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription("Echo", "Echo application description",
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Echo Application Interface Id " + echoInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public void registerAddApplicationInterface() {
        try {
            System.out.println("#### Registering Add Application Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(addModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("x", "2",
                    DataType.STRING, null, false, "Add operation input_1", null);
            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("y", "3",
                    DataType.STRING, null, false, "Add operation input_2", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Result",
                    "0", DataType.STRING);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            String addApplicationInterfaceId = airavataClient.registerApplicationInterface(
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription("Add", "Add two numbers",
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Add Application Interface Id " + addApplicationInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public void registerMultiplyApplicationInterface() {
        try {
            System.out.println("#### Registering Multiply Application Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(multiplyModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("x", "4",
                    DataType.STRING, null, false, "Multiply operation input_1", null);
            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("y", "5",
                    DataType.STRING, null, false, "Multiply operation input_2", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Result",
                    "0", DataType.STRING);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            String multiplyApplicationInterfaceId = airavataClient.registerApplicationInterface(
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription("Multiply", "Multiply two numbers",
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Multiply Application Interface Id " + multiplyApplicationInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public void registerSubtractApplicationInterface() {
        try {
            System.out.println("#### Registering Subtract Application Interface #### \n");

            List<String> appModules = new ArrayList<String>();
            appModules.add(subtractModuleId);

            InputDataObjectType input1 = RegisterSampleApplicationsUtils.createAppInput("x", "6",
                    DataType.STRING, null, false, "Subtract operation input_1", null);
            InputDataObjectType input2 = RegisterSampleApplicationsUtils.createAppInput("y", "7",
                    DataType.STRING, null, false, "Subtract operation input_2", null);

            List<InputDataObjectType> applicationInputs = new ArrayList<InputDataObjectType>();
            applicationInputs.add(input1);
            applicationInputs.add(input2);

            OutputDataObjectType output1 = RegisterSampleApplicationsUtils.createAppOutput("Result",
                    "0", DataType.STRING);

            List<OutputDataObjectType> applicationOutputs = new ArrayList<OutputDataObjectType>();
            applicationOutputs.add(output1);

            String subtractApplicationInterfaceId = airavataClient.registerApplicationInterface(
                    RegisterSampleApplicationsUtils.createApplicationInterfaceDescription("Subtract", "Subtract two numbers",
                            appModules, applicationInputs, applicationOutputs));
            System.out.println("Subtract Application Interface Id " + subtractApplicationInterfaceId);

        } catch (TException e) {
            e.printStackTrace();
        }
    }
}
