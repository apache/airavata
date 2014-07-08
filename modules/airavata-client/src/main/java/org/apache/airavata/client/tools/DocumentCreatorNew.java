/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.client.tools;

import java.util.Arrays;
import java.util.HashSet;

import org.airavata.appcatalog.cpi.AppCatalog;
import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationParallelismType;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.LOCALDataMovement;
import org.apache.airavata.model.appcatalog.computeresource.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;
import org.apache.airavata.model.appcatalog.computeresource.SCPDataMovement;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.SecurityProtocol;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.model.error.InvalidRequestException;
import org.apache.thrift.TException;

public class DocumentCreatorNew {

    private AppCatalog appcatalog = null;
    private String trestleshpcHostAddress = "trestles.sdsc.edu";
    private String lonestarHostAddress = "lonestar.tacc.utexas.edu";
    private String stampedeHostAddress  = "stampede.tacc.xsede.org";
    private String gridftpAddress = "gsiftp://trestles-dm1.sdsc.edu:2811";
    private String gramAddress = "trestles-login1.sdsc.edu:2119/jobmanager-pbstest2";
    private Airavata.Client client;
	private GatewayResourceProfile gatewayResourceProfile;
	
    public DocumentCreatorNew(Airavata.Client client) throws AppCatalogException {
    	this.client = client;
	}

    public void createLocalHostDocs() throws AppCatalogException, InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	//Define compute resource host
    	ComputeResourceDescription host = DocumentCreatorUtils.createComputeResourceDescription(
    			"localhost", new HashSet<String>(Arrays.asList(new String[]{"127.0.0.1"})), new HashSet<String>(Arrays.asList(new String[]{"127.0.0.1"})));
//    	host.setIsEmpty(true);
    	host.setComputeResourceId(client.registerComputeResource(host));
    	
    	LOCALSubmission localSubmission = new LOCALSubmission();
    	ResourceJobManager resourceJobManager = DocumentCreatorUtils.createResourceJobManager(ResourceJobManagerType.FORK, null, null,null);
		localSubmission.setResourceJobManager(resourceJobManager);
		client.addLocalSubmissionDetails(host.getComputeResourceId(), 1, localSubmission);
		
		LOCALDataMovement localDataMovement = new LOCALDataMovement();
		client.addLocalDataMovementDetails(host.getComputeResourceId(), 1,localDataMovement);

		//Define application module
		ApplicationModule module = DocumentCreatorUtils.createApplicationModule("echo","1.0.0", "Local host echo applications");
		module.setAppModuleId(client.registerApplicationModule(module));
		
		//Define application interfaces
    	ApplicationInterfaceDescription application = new ApplicationInterfaceDescription();
//    	application.setIsEmpty(false);
    	application.setApplicationName("SimpleEcho0");
		application.addToApplicationModules(module.getAppModuleId());
    	application.addToApplicationInputs(DocumentCreatorUtils.createAppInput("echo_input","echo_input","Echo Input Data",null,DataType.STRING));
		application.addToApplicationOutputs(DocumentCreatorUtils.createAppOutput("echo_output", null, DataType.STRING));
		application.setApplicationInterfaceId(client.registerApplicationInterface(application));
		
		//Define application deployment
		ApplicationDeploymentDescription deployment = DocumentCreatorUtils.createApplicationDeployment(host.getComputeResourceId(), module.getAppModuleId(), "/bin/echo",ApplicationParallelismType.SERIAL, "Local echo app depoyment");
		deployment.setAppDeploymentId(client.registerApplicationDeployment(deployment));

		//Define gateway profile
		ComputeResourcePreference computeResourcePreference = DocumentCreatorUtils.createComputeResourcePreference(
				host.getComputeResourceId(), "/tmp", null,
				false, null,
				null, null);
		gatewayResourceProfile = new GatewayResourceProfile();
//		gatewayResourceProfile.setGatewayID("default");
		gatewayResourceProfile.setGatewayName("default");
		gatewayResourceProfile.addToComputeResourcePreferences(computeResourcePreference);
        String gatewayId = client.registerGatewayResourceProfile(gatewayResourceProfile);
        gatewayResourceProfile.setGatewayID(gatewayId);
        client.addGatewayComputeResourcePreference(gatewayResourceProfile.getGatewayID(), host.getComputeResourceId(), computeResourcePreference);
    }

    private GatewayResourceProfile getGatewayResourceProfile() throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
//    	if (gatewayResourceProfile==null){
//    		try {
//				gatewayResourceProfile = client.getGatewayResourceProfile(ga);
//			} catch (Exception e) {
//
//			}
        if (gatewayResourceProfile == null) {
            gatewayResourceProfile = new GatewayResourceProfile();
//				gatewayResourceProfile.setGatewayID("default");
            gatewayResourceProfile.setGatewayName("default");
            gatewayResourceProfile.setGatewayID(client.registerGatewayResourceProfile(gatewayResourceProfile));
        }
//    	}
        return gatewayResourceProfile;

    }
    public void createSSHHostDocs() throws AppCatalogException, InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        ComputeResourceDescription host = DocumentCreatorUtils.createComputeResourceDescription("gw111.iu.xsede.org", null, null);
        host.addToIpAddresses("gw111.iu.xsede.org");
        host.addToHostAliases("gw111.iu.xsede.org");
        host.setResourceDescription("gw111 ssh access");
        host.setComputeResourceId(client.registerComputeResource(host));
        
        SSHJobSubmission jobSubmission = new SSHJobSubmission();
        jobSubmission.setSshPort(22);
        jobSubmission.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
        ResourceJobManager resourceJobManager = DocumentCreatorUtils.createResourceJobManager(ResourceJobManagerType.FORK, null, null, null);
        jobSubmission.setResourceJobManager(resourceJobManager);
        client.addSSHJobSubmissionDetails(host.getComputeResourceId(), 1, jobSubmission);
    	
        SCPDataMovement scpDataMovement = new SCPDataMovement();
        scpDataMovement.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
        scpDataMovement.setSshPort(22);
        client.addSCPDataMovementDetails(host.getComputeResourceId(), 1, scpDataMovement);
        
        ApplicationModule module = DocumentCreatorUtils.createApplicationModule("echo", "1.1", null);
        module.setAppModuleId(client.registerApplicationModule(module));;
        
        ApplicationDeploymentDescription deployment = DocumentCreatorUtils.createApplicationDeployment(host.getComputeResourceId(), module.getAppModuleId(), "/bin/echo", ApplicationParallelismType.SERIAL, "SSHEchoApplication");
        client.registerApplicationDeployment(deployment);
        
        ApplicationInterfaceDescription application = new ApplicationInterfaceDescription();
//    	application.setIsEmpty(false);
        application.setApplicationName("SSHEcho1");
        application.addToApplicationModules(module.getAppModuleId());
        application.addToApplicationInputs(DocumentCreatorUtils.createAppInput("echo_input","echo_input", null, null, DataType.STRING));
        application.addToApplicationOutputs(DocumentCreatorUtils.createAppOutput("echo_output", null, DataType.STRING));
        client.registerApplicationInterface(application);
        client.addGatewayComputeResourcePreference(getGatewayResourceProfile().getGatewayID(), host.getComputeResourceId(), DocumentCreatorUtils.createComputeResourcePreference(host.getComputeResourceId(), "/tmp", null, false, null, null, null));
    }
    
//
//    public void createGramDocs() {
////        /*
////           creating host descriptor for gram
////        */
////        HostDescription host = new HostDescription(GlobusHostType.type);
////        host.getType().setHostAddress(trestleshpcHostAddress);
////        host.getType().setHostName(trestleshpcHostAddress);
////        ((GlobusHostType) host.getType()).setGlobusGateKeeperEndPointArray(new String[]{gramAddress});
////        ((GlobusHostType) host.getType()).setGridFTPEndPointArray(new String[]{gridftpAddress});
////        try {
////            airavataAPI.getApplicationManager().saveHostDescription(host);
////        } catch (AiravataAPIInvocationException e) {
////            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
////        }
////
////
////        /*
////        * Service Description creation and saving
////        */
////        String serviceName = "SimpleEcho1";
////        ServiceDescription serv = new ServiceDescription();
////        serv.getType().setName(serviceName);
////
////        List<InputParameterType> inputList = new ArrayList<InputParameterType>();
////        List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();
////
////        InputParameterType input = InputParameterType.Factory.newInstance();
////        input.setParameterName("echo_input");
////        ParameterType parameterType = input.addNewParameterType();
////        parameterType.setType(DataType.STRING);
////        parameterType.setName("String");
////
////        OutputParameterType output = OutputParameterType.Factory.newInstance();
////        output.setParameterName("echo_output");
////        ParameterType parameterType1 = output.addNewParameterType();
////        parameterType1.setType(DataType.STRING);
////        parameterType1.setName("String");
////
////        inputList.add(input);
////        outputList.add(output);
////
////        InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList.size()]);
////        OutputParameterType[] outputParamList = outputList.toArray(new OutputParameterType[outputList.size()]);
////
////        serv.getType().setInputParametersArray(inputParamList);
////        serv.getType().setOutputParametersArray(outputParamList);
////        try {
////            airavataAPI.getApplicationManager().saveServiceDescription(serv);
////        } catch (AiravataAPIInvocationException e) {
////            e.printStackTrace();
////        }
////
////        /*
////            Application descriptor creation and saving
////         */
////        ApplicationDescription appDesc = new ApplicationDescription(HpcApplicationDeploymentType.type);
////        HpcApplicationDeploymentType app = (HpcApplicationDeploymentType) appDesc.getType();
////        ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
////        name.setStringValue("EchoLocal");
////        app.setApplicationName(name);
////        ProjectAccountType projectAccountType = app.addNewProjectAccount();
////        projectAccountType.setProjectAccountNumber("sds128");
////
////        QueueType queueType = app.addNewQueue();
////        queueType.setQueueName("normal");
////
////        app.setCpuCount(1);
////        app.setJobType(JobTypeType.SERIAL);
////        app.setNodeCount(1);
////        app.setProcessorsPerNode(1);
////
////        /*
////           * Use bat file if it is compiled on Windows
////           */
////        app.setExecutableLocation("/bin/echo");
////
////        /*
////           * Default tmp location
////           */
////        String tempDir = "/home/ogce/scratch";
////        app.setScratchWorkingDirectory(tempDir);
////        app.setMaxMemory(10);
////
////
////        try {
////            airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, trestleshpcHostAddress, appDesc);
////        } catch (AiravataAPIInvocationException e) {
////            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
////        }
//    }
//
    public void createPBSDocsForOGCE() throws AppCatalogException, InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	
    	ComputeResourceDescription host = DocumentCreatorUtils.createComputeResourceDescription(trestleshpcHostAddress, null, null);
    	host.addToIpAddresses(trestleshpcHostAddress);
    	host.addToHostAliases(trestleshpcHostAddress);
    	host.setComputeResourceId(client.registerComputeResource(host));
    	
    	SSHJobSubmission sshJobSubmission = new SSHJobSubmission();
    	ResourceJobManager resourceJobManager = DocumentCreatorUtils.createResourceJobManager(ResourceJobManagerType.PBS, "/opt/torque/bin/", null, null);
    	sshJobSubmission.setResourceJobManager(resourceJobManager);
    	sshJobSubmission.setSecurityProtocol(SecurityProtocol.GSI);
    	sshJobSubmission.setSshPort(22);
    	client.addSSHJobSubmissionDetails(host.getComputeResourceId(), 1, sshJobSubmission);
    	
    	SCPDataMovement scpDataMovement = new SCPDataMovement();
    	scpDataMovement.setSecurityProtocol(SecurityProtocol.GSI);
    	scpDataMovement.setSshPort(22);
    	
    	client.addSCPDataMovementDetails(host.getComputeResourceId(), 1, scpDataMovement);
    	
        ApplicationModule module1 = DocumentCreatorUtils.createApplicationModule("echo", "1.2", null);
        module1.setAppModuleId(client.registerApplicationModule(module1));
        
    	ApplicationInterfaceDescription application = new ApplicationInterfaceDescription();
//    	application.setIsEmpty(false);
        application.setApplicationName("SimpleEcho2");
        application.addToApplicationModules(module1.getAppModuleId());
        application.addToApplicationInputs(DocumentCreatorUtils.createAppInput("echo_input", "echo_input", "echo_input", null, DataType.STRING));
        application.addToApplicationOutputs(DocumentCreatorUtils.createAppOutput("echo_output", null, DataType.STRING));

        application.setApplicationInterfaceId(client.registerApplicationInterface(application));

        ApplicationDeploymentDescription deployment = DocumentCreatorUtils.createApplicationDeployment(host.getComputeResourceId(), module1.getAppModuleId(), "/bin/echo", ApplicationParallelismType.SERIAL, "Echo application");
        deployment.setAppDeploymentId(client.registerApplicationDeployment(deployment));
        
        client.addGatewayComputeResourcePreference(getGatewayResourceProfile().getGatewayID(), host.getComputeResourceId(), DocumentCreatorUtils.createComputeResourcePreference(host.getComputeResourceId(), "/oasis/scratch/trestles/ogce/temp_project/", "sds128", false, null, null, null));


        ApplicationModule module2 = DocumentCreatorUtils.createApplicationModule("wrf", "1.0.0", null);
        module2.setAppModuleId(client.registerApplicationModule(module2));
        ApplicationInterfaceDescription application2 = new ApplicationInterfaceDescription();
//    	application2.setIsEmpty(false);
        application2.setApplicationName("WRF");
        application2.addToApplicationModules(module2.getAppModuleId());
        application2.addToApplicationInputs(DocumentCreatorUtils.createAppInput("WRF_Namelist", "WRF_Namelist", null, null, DataType.URI));
        application2.addToApplicationInputs(DocumentCreatorUtils.createAppInput("WRF_Boundary_File", "WRF_Boundary_File", null, null, DataType.URI));
        application2.addToApplicationInputs(DocumentCreatorUtils.createAppInput("WRF_Input_File", "WRF_Input_File", null, null, DataType.URI));

        application2.addToApplicationOutputs(DocumentCreatorUtils.createAppOutput("WRF_Output", null, DataType.URI));
        application2.addToApplicationOutputs(DocumentCreatorUtils.createAppOutput("WRF_Execution_Log", null, DataType.URI));
        application2.setApplicationInterfaceId(client.registerApplicationInterface(application2));

        ApplicationDeploymentDescription deployment2 = DocumentCreatorUtils.createApplicationDeployment(host.getComputeResourceId(), module2.getAppModuleId(), "/home/ogce/apps/wrf_wrapper.sh", ApplicationParallelismType.MPI,"WRF");
        deployment2.setAppDeploymentId(client.registerApplicationDeployment(deployment2));
    }
    
    public void createSlurmDocs() throws AppCatalogException, InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	ComputeResourceDescription host = DocumentCreatorUtils.createComputeResourceDescription(stampedeHostAddress, null, null);
    	host.addToHostAliases(stampedeHostAddress);
    	host.addToIpAddresses(stampedeHostAddress);
    	host.setComputeResourceId(client.registerComputeResource(host));

    	ResourceJobManager resourceJobManager = DocumentCreatorUtils.createResourceJobManager(ResourceJobManagerType.SLURM, "/usr/bin/", null, "push");
    	SSHJobSubmission sshJobSubmission = new SSHJobSubmission();
    	sshJobSubmission.setResourceJobManager(resourceJobManager);
    	sshJobSubmission.setSecurityProtocol(SecurityProtocol.GSI);
    	sshJobSubmission.setSshPort(2222);
    	client.addSSHJobSubmissionDetails(host.getComputeResourceId(), 1, sshJobSubmission);
    	
       	SCPDataMovement scpDataMovement = new SCPDataMovement();
    	scpDataMovement.setSecurityProtocol(SecurityProtocol.GSI);
    	scpDataMovement.setSshPort(22);
    	client.addSCPDataMovementDetails(host.getComputeResourceId(), 1, scpDataMovement);
    	
    	ApplicationModule module = DocumentCreatorUtils.createApplicationModule("echo", "1.3", null);
    	module.setAppModuleId(client.registerApplicationModule(module));
    	
    	ApplicationInterfaceDescription application = new ApplicationInterfaceDescription();
//    	application.setIsEmpty(false);
    	application.setApplicationName("SimpleEcho3");
    	application.addToApplicationModules(module.getAppModuleId());
    	application.addToApplicationInputs(DocumentCreatorUtils.createAppInput("echo_input", "echo_input", null, null, DataType.STRING));
    	application.addToApplicationOutputs(DocumentCreatorUtils.createAppOutput("echo_output", null, DataType.STRING));
        application.setApplicationInterfaceId(client.registerApplicationInterface(application));

        ApplicationDeploymentDescription deployment = DocumentCreatorUtils.createApplicationDeployment(host.getComputeResourceId(), module.getAppModuleId(), "/bin/echo", ApplicationParallelismType.SERIAL, "EchoLocal");
        deployment.setAppDeploymentId(client.registerApplicationDeployment(deployment));
        
        client.addGatewayComputeResourcePreference(getGatewayResourceProfile().getGatewayID(), host.getComputeResourceId(), DocumentCreatorUtils.createComputeResourcePreference(host.getComputeResourceId(), "/home1/01437/ogce", "TG-STA110014S", false, null, null, null));
    }

    public void createSGEDocs() throws AppCatalogException, InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	ComputeResourceDescription host = DocumentCreatorUtils.createComputeResourceDescription(lonestarHostAddress, null, null);
    	host.addToHostAliases(lonestarHostAddress);
    	host.addToIpAddresses(lonestarHostAddress);
    	host.setComputeResourceId(client.registerComputeResource(host));
    	
    	ResourceJobManager resourceJobManager = DocumentCreatorUtils.createResourceJobManager(ResourceJobManagerType.UGE, "/opt/sge6.2/bin/lx24-amd64/", null, null);
    	SSHJobSubmission sshJobSubmission = new SSHJobSubmission();
    	sshJobSubmission.setResourceJobManager(resourceJobManager);
    	sshJobSubmission.setSecurityProtocol(SecurityProtocol.GSI);
    	sshJobSubmission.setSshPort(22);

    	client.addSSHJobSubmissionDetails(host.getComputeResourceId(), 1, sshJobSubmission);
    	
    	SCPDataMovement scpDataMovement = new SCPDataMovement();
    	scpDataMovement.setSecurityProtocol(SecurityProtocol.GSI);
    	scpDataMovement.setSshPort(22);
    	client.addSCPDataMovementDetails(host.getComputeResourceId(), 1, scpDataMovement);
    	
        ApplicationModule module = DocumentCreatorUtils.createApplicationModule("echo", "1.4", null);
        module.setAppModuleId(client.registerApplicationModule(module));
        
        ApplicationInterfaceDescription application = new ApplicationInterfaceDescription();
//    	application.setIsEmpty(false);
        application.setApplicationName("SimpleEcho4");
        application.addToApplicationModules(module.getAppModuleId());
    	application.addToApplicationInputs(DocumentCreatorUtils.createAppInput("echo_input", "echo_input", null, null, DataType.STRING));
    	application.addToApplicationOutputs(DocumentCreatorUtils.createAppOutput("echo_output", null, DataType.STRING));
        application.setApplicationInterfaceId(client.registerApplicationInterface(application));

        ApplicationDeploymentDescription deployment = DocumentCreatorUtils.createApplicationDeployment(host.getComputeResourceId(), module.getAppModuleId(), "/bin/echo", ApplicationParallelismType.SERIAL, "EchoLocal");
        deployment.setAppDeploymentId(client.registerApplicationDeployment(deployment));
        
        client.addGatewayComputeResourcePreference(getGatewayResourceProfile().getGatewayID(), host.getComputeResourceId(), DocumentCreatorUtils.createComputeResourcePreference(host.getComputeResourceId(), "/home1/01437/ogce", "TG-STA110014S", false, null, null, null));
    }

//	public void createEchoHostDocs() {
//		String serviceName = "Echo";
//		ServiceDescription serviceDescription = new ServiceDescription();
//		List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
//		List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
//		serviceDescription.getType().setName(serviceName);
//		serviceDescription.getType().setDescription("Echo service");
//		// Creating input parameters
//		InputParameterType parameter = InputParameterType.Factory.newInstance();
//		parameter.setParameterName("echo_input");
//		parameter.setParameterDescription("echo input");
//		ParameterType parameterType = parameter.addNewParameterType();
//		parameterType.setType(DataType.STRING);
//		parameterType.setName("String");
//		inputParameters.add(parameter);
//
//		// Creating output parameters
//		OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
//		outputParameter.setParameterName("echo_output");
//		outputParameter.setParameterDescription("Echo output");
//		ParameterType outputParaType = outputParameter.addNewParameterType();
//		outputParaType.setType(DataType.STRING);
//		outputParaType.setName("String");
//		outputParameters.add(outputParameter);
//
//		// Setting input and output parameters to serviceDescriptor
//		serviceDescription.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[] {}));
//		serviceDescription.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[] {}));
//
//		try {
//			airavataAPI.getApplicationManager().saveServiceDescription(serviceDescription);
//		} catch (AiravataAPIInvocationException e) {
//			e.printStackTrace(); // To change body of catch statement use File |
//									// Settings | File Templates.
//		}
//		// Localhost
//		ApplicationDescription applicationDeploymentDescription = new ApplicationDescription();
//		ApplicationDeploymentDescriptionType applicationDeploymentDescriptionType = applicationDeploymentDescription.getType();
//		applicationDeploymentDescriptionType.addNewApplicationName().setStringValue(serviceName);
//		applicationDeploymentDescriptionType.setExecutableLocation("/bin/echo");
//		applicationDeploymentDescriptionType.setScratchWorkingDirectory("/tmp");
//
//		try {
//			airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, "localhost", applicationDeploymentDescription);
//		} catch (AiravataAPIInvocationException e) {
//			e.printStackTrace(); // To change body of catch statement use File |
//									// Settings | File Templates.
//		}
//		// Stampede
//		/*
//		 * Application descriptor creation and saving
//		 */
//		ApplicationDescription appDesc1 = new ApplicationDescription(HpcApplicationDeploymentType.type);
//		HpcApplicationDeploymentType app1 = (HpcApplicationDeploymentType) appDesc1.getType();
//		ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
//		name.setStringValue(serviceName);
//		app1.setApplicationName(name);
//		ProjectAccountType projectAccountType = app1.addNewProjectAccount();
//		projectAccountType.setProjectAccountNumber("TG-STA110014S");
//
//		QueueType queueType = app1.addNewQueue();
//		queueType.setQueueName("normal");
//
//		app1.setCpuCount(1);
//		app1.setJobType(JobTypeType.SERIAL);
//		app1.setNodeCount(1);
//		app1.setProcessorsPerNode(1);
//		app1.setMaxWallTime(10);
//		/*
//		 * Use bat file if it is compiled on Windows
//		 */
//		app1.setExecutableLocation("/bin/echo");
//
//		/*
//		 * Default tmp location
//		 */
//		String tempDir = "/home1/01437/ogce";
//
//		app1.setScratchWorkingDirectory(tempDir);
//		app1.setInstalledParentPath("/usr/bin/");
//
//		try {
//			airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, stampedeHostAddress, appDesc1);
//		} catch (AiravataAPIInvocationException e) {
//			e.printStackTrace(); // To change body of catch statement use File |
//									// Settings | File Templates.
//		}
//		// Trestles
//		/*
//		 * Application descriptor creation and saving
//		 */
//		ApplicationDescription appDesc2 = new ApplicationDescription(HpcApplicationDeploymentType.type);
//		HpcApplicationDeploymentType app2 = (HpcApplicationDeploymentType) appDesc2.getType();
//		ApplicationDeploymentDescriptionType.ApplicationName name2 = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
//		name2.setStringValue(serviceName);
//		app2.setApplicationName(name);
//		ProjectAccountType projectAccountType2 = app2.addNewProjectAccount();
//		projectAccountType2.setProjectAccountNumber("sds128");
//
//		QueueType queueType2 = app2.addNewQueue();
//		queueType2.setQueueName("normal");
//
//		app2.setCpuCount(1);
//		app2.setJobType(JobTypeType.SERIAL);
//		app2.setNodeCount(1);
//		app2.setProcessorsPerNode(1);
//		app2.setMaxWallTime(10);
//		/*
//		 * Use bat file if it is compiled on Windows
//		 */
//		app2.setExecutableLocation("/bin/echo");
//
//		/*
//		 * Default tmp location
//		 */
//		String tempDir2 = "/home/ogce/scratch";
//
//		app2.setScratchWorkingDirectory(tempDir2);
//		app2.setInstalledParentPath("/opt/torque/bin/");
//
//		try {
//			airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, trestleshpcHostAddress, appDesc2);
//		} catch (AiravataAPIInvocationException e) {
//			e.printStackTrace(); // To change body of catch statement use File |
//									// Settings | File Templates.
//		}
//		// Lonestar
//		/*
//		 * Application descriptor creation and saving
//		 */
//		ApplicationDescription appDesc3 = new ApplicationDescription(HpcApplicationDeploymentType.type);
//		HpcApplicationDeploymentType app3 = (HpcApplicationDeploymentType) appDesc3.getType();
//		ApplicationDeploymentDescriptionType.ApplicationName name3 = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
//		name3.setStringValue(serviceName);
//		app3.setApplicationName(name);
//		ProjectAccountType projectAccountType3 = app3.addNewProjectAccount();
//		projectAccountType3.setProjectAccountNumber("TG-STA110014S");
//
//		QueueType queueType3 = app3.addNewQueue();
//		queueType3.setQueueName("normal");
//
//		app3.setCpuCount(1);
//		app3.setJobType(JobTypeType.SERIAL);
//		app3.setNodeCount(1);
//		app3.setProcessorsPerNode(1);
//		app3.setMaxWallTime(10);
//		/*
//		 * Use bat file if it is compiled on Windows
//		 */
//		app3.setExecutableLocation("/bin/echo");
//
//		/*
//		 * Default tmp location
//		 */
//		String tempDir3 = "/home1/01437/ogce";
//
//		app3.setScratchWorkingDirectory(tempDir3);
//		app3.setInstalledParentPath("/opt/sge6.2/bin/lx24-amd64/");
//
//		try {
//			airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, lonestarHostAddress, appDesc3);
//		} catch (AiravataAPIInvocationException e) {
//			e.printStackTrace(); // To change body of catch statement use File |
//									// Settings | File Templates.
//		}
//
//	}
    
    
    
}

