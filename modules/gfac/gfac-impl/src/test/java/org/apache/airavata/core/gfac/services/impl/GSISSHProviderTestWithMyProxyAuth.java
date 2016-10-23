///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
//*/
//package org.apache.airavata.core.gfac.services.impl;
//
//import java.io.File;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.UUID;
//
//import org.apache.aiaravata.application.catalog.data.model.ApplicationInterface;
//import org.apache.airavata.commons.gfac.type.ActualParameter;
//import org.apache.airavata.commons.gfac.type.ApplicationDescription;
//import org.apache.airavata.commons.gfac.type.HostDescription;
//import org.apache.airavata.commons.gfac.type.ServiceDescription;
//import org.apache.airavata.gfac.core.GFacConfiguration;
//import org.apache.airavata.gfac.core.GFacException;
//import org.apache.airavata.gfac.core.SecurityContext;
//import org.apache.airavata.gfac.core.context.ApplicationContext;
//import org.apache.airavata.gfac.core.context.JobExecutionContext;
//import org.apache.airavata.gfac.core.context.MessageContext;
//import org.apache.airavata.gfac.impl.BetterGfacImpl;
//import org.apache.airavata.gfac.gsissh.security.GSISecurityContext;
//import org.apache.airavata.gfac.ssh.api.Cluster;
//import org.apache.airavata.gfac.ssh.api.SSHApiException;
//import org.apache.airavata.gfac.ssh.api.ServerInfo;
//import GSIAuthenticationInfo;
//import org.apache.airavata.gfac.ssh.impl.HPCRemoteCluster;
//import org.apache.airavata.gfac.ssh.impl.authentication.MyProxyAuthenticationInfo;
//import org.apache.airavata.gfac.ssh.util.CommonUtils;
//import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
//import org.apache.airavata.model.experiment.TaskDetails;
//import org.apache.airavata.registry.core.experiment.registry.jpa.impl.RegistryFactory;
//import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
//import org.apache.airavata.schemas.gfac.GsisshHostType;
//import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
//import org.apache.airavata.schemas.gfac.InputParameterType;
//import org.apache.airavata.schemas.gfac.JobTypeType;
//import org.apache.airavata.schemas.gfac.OutputParameterType;
//import org.apache.airavata.schemas.gfac.ProjectAccountType;
//import org.apache.airavata.schemas.gfac.QueueType;
//import org.apache.airavata.schemas.gfac.StringParameterType;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//
//public class GSISSHProviderTestWithMyProxyAuth {
//    private JobExecutionContext jobExecutionContext;
//
//    //FIXME: move job properties to configuration file
//    private static final String hostAddress = "trestles.sdsc.edu";
//    private static final String hostName = "trestles";
//    private String myProxyUserName;
//    private String myProxyPassword;
//    private String workingDirectory;
//    private String certificateLocation = "/Users/lahirugunathilake/Downloads/certificates";
//
//    @BeforeClass
//    public void setUp() throws Exception {
////        System.setProperty("myproxy.user", "ogce");
////        System.setProperty("myproxy.password", "");
////        System.setProperty("basedir", "/Users/lahirugunathilake/Downloads");
////        System.setProperty("gsi.working.directory", "/home/ogce");
////        System.setProperty("gsi.certificate.path", "/Users/lahirugunathilake/Downloads/certificates");
//        certificateLocation = System.getProperty("trusted.cert.location");
//        myProxyUserName = System.getProperty("myproxy.username");
//        myProxyPassword = System.getProperty("myproxy.password");
//        workingDirectory = System.getProperty("gsi.working.directory");
//
//        if (myProxyUserName == null || myProxyPassword == null || certificateLocation == null) {
//            System.out.println(">>>>>> Please run tests with my proxy user name and password. " +
//                    "E.g :- mvn clean install -Dmyproxy.username=xxx -Dmyproxy.password=xxx -Dgsi.working.directory=/path<<<<<<<");
//            throw new Exception("Need my proxy user name password to run tests.");
//        }
//        URL resource = ApplicationSettings.loadFile(org.apache.airavata.common.utils.Constants.GFAC_CONFIG_XML);
//        assert resource != null;
//        System.out.println(resource.getFile());
//        GFacConfiguration gFacConfiguration = GFacConfiguration.create(new File(resource.getPath()), null);
//
//        /*
//        * Host
//        */
//        HostDescription host = new HostDescription(GsisshHostType.type);
//        host.getType().setHostAddress(hostAddress);
//        host.getType().setHostName(hostName);
//
//        /*
//        * App
//        */
//        ApplicationDescription appDesc = new ApplicationDescription(HpcApplicationDeploymentType.type);
//        HpcApplicationDeploymentType app = (HpcApplicationDeploymentType) appDesc.getType();
//        ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
//        name.setStringValue("EchoLocal");
//        app.setApplicationName(name);
//        ProjectAccountType projectAccountType = app.addNewProjectAccount();
//        projectAccountType.setProjectAccountNumber("sds128");
//
//        QueueType queueType = app.addNewQueue();
//        queueType.setQueueName("normal");
//
//        app.setCpuCount(1);
//        app.setJobType(JobTypeType.SERIAL);
//        app.setNodeCount(1);
//        app.setProcessorsPerNode(1);
//
//        /*
//        * Use bat file if it is compiled on Windows
//        */
//        app.setExecutableLocation("/bin/echo");
//
//        /*
//        * Default tmp location
//        */
//        String tempDir = "/home/ogce/scratch/";
//        String date = (new Date()).toString();
//        date = date.replaceAll(" ", "_");
//        date = date.replaceAll(":", "_");
//
//        tempDir = workingDirectory + File.separator
//                + "SimpleEcho" + "_" + date + "_" + UUID.randomUUID();
//
//        System.out.println(tempDir);
//        app.setScratchWorkingDirectory(tempDir);
//        app.setStaticWorkingDirectory(tempDir);
//        app.setInputDataDirectory(tempDir + File.separator + "inputData");
//        app.setOutputDataDirectory(tempDir + File.separator + "outputData");
//        app.setStandardOutput(tempDir + File.separator + app.getApplicationName().getStringValue() + ".stdout");
//        app.setStandardError(tempDir + File.separator + app.getApplicationName().getStringValue() + ".stderr");
//        app.setMaxWallTime(5);
//        app.setInstalledParentPath("/opt/torque/bin/");
//
//        /*
//        * Service
//        */
//        ServiceDescription serv = new ServiceDescription();
//        serv.getType().setName("SimpleEcho");
//
//        List<InputParameterType> inputList = new ArrayList<InputParameterType>();
//
//        InputParameterType input = InputParameterType.Factory.newInstance();
//        input.setParameterName("echo_input");
//        input.setParameterType(StringParameterType.Factory.newInstance());
//        inputList.add(input);
//
//        InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList
//
//                .size()]);
//        List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();
//        OutputParameterType output = OutputParameterType.Factory.newInstance();
//        output.setParameterName("echo_output");
//        output.setParameterType(StringParameterType.Factory.newInstance());
//        outputList.add(output);
//
//        OutputParameterType[] outputParamList = outputList
//                .toArray(new OutputParameterType[outputList.size()]);
//
//        serv.getType().setInputParametersArray(inputParamList);
//        serv.getType().setOutputParametersArray(outputParamList);
//
//        jobExecutionContext = new JobExecutionContext(gFacConfiguration, serv.getType().getName());
//        // Adding security context
//        jobExecutionContext.addSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT, getSecurityContext(app));
//        ApplicationContext applicationContext = new ApplicationContext();
//        jobExecutionContext.setApplicationContext(applicationContext);
//        applicationContext.setServiceDescription(serv);
//        applicationContext.setApplicationDeploymentDescription(appDesc);
//        applicationContext.setHostDescription(host);
//
//        MessageContext inMessage = new MessageContext();
//        ActualParameter echo_input = new ActualParameter();
//        ((StringParameterType) echo_input.getType()).setValue("echo_output=hello");
//        inMessage.addParameter("echo_input", echo_input);
//
//
//        jobExecutionContext.setInMessageContext(inMessage);
//
//        MessageContext outMessage = new MessageContext();
//        ActualParameter echo_out = new ActualParameter();
////		((StringParameterType)echo_input.getType()).setValue("echo_output=hello");
//        outMessage.addParameter("echo_output", echo_out);
//        jobExecutionContext.setRegistry(RegistryFactory.getLoggingRegistry());
//        jobExecutionContext.setTaskData(new TaskDetails("11323"));
//        jobExecutionContext.setOutMessageContext(outMessage);
//
//    }
//
//    private SecurityContext getSecurityContext(HpcApplicationDeploymentType app) {
//        GSIAuthenticationInfo authenticationInfo
//                = new MyProxyAuthenticationInfo(myProxyUserName, myProxyPassword, "myproxy.teragrid.org",
//                7512, 17280000, certificateLocation);
//
//        // Server info
//        ServerInfo serverInfo = new ServerInfo("ogce", "trestles.sdsc.edu");
//        Cluster pbsCluster = null;
//        try {
//            pbsCluster = new HPCRemoteCluster(serverInfo, authenticationInfo, CommonUtils.getPBSJobManager(app.getInstalledParentPath()));
//        } catch (SSHApiException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//        GSISecurityContext sshSecurityContext = new GSISecurityContext(pbsCluster);
//        return sshSecurityContext;
//    }
//    @Test
//    public void testGSISSHProvider() throws GFacException {
//        BetterGfacImpl gFacAPI = new BetterGfacImpl();
//        gFacAPI.submitJob(jobExecutionContext.getExperimentID(), jobExecutionContext.getTaskData().getTaskID(), jobExecutionContext.getGatewayID());
//        System.out.println(jobExecutionContext.getJobDetails().getJobDescription());
//        System.out.println(jobExecutionContext.getJobDetails().getJobID());
//    }
//
//}
