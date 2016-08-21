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
//import org.apache.airavata.gfac.ssh.security.SSHSecurityContext;
//import org.apache.airavata.gfac.ssh.api.RemoteCluster;
//import org.apache.airavata.gfac.ssh.api.SSHApiException;
//import org.apache.airavata.gfac.ssh.api.ServerInfo;
//import AuthenticationInfo;
//import org.apache.airavata.gfac.ssh.api.job.JobManagerConfiguration;
//import org.apache.airavata.gfac.ssh.impl.HPCRemoteCluster;
//import org.apache.airavata.gfac.ssh.impl.authentication.DefaultPasswordAuthenticationInfo;
//import org.apache.airavata.gfac.ssh.impl.authentication.DefaultPublicKeyFileAuthentication;
//import org.apache.airavata.gfac.ssh.util.CommonUtils;
//import org.apache.airavata.model.experiment.TaskDetails;
//import org.apache.airavata.registry.core.experiment.registry.jpa.impl.RegistryFactory;
//import org.apache.airavata.schemas.gfac.*;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//
//import java.io.File;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.UUID;
//
//public class BigRed2TestWithSSHAuth {
//    private JobExecutionContext jobExecutionContext;
//
//    private String userName;
//    private String password;
//    private String passPhrase;
//    private String hostName;
//    private String workingDirectory;
//    private String privateKeyPath;
//    private String publicKeyPath;
//
//    @BeforeClass
//    public void setUp() throws Exception {
//
//        System.out.println("Test case name " + this.getClass().getName());
////        System.setProperty("ssh.host","bigred2.uits.iu.edu");        //default ssh host
////        System.setProperty("ssh.user", "lginnali");
////        System.setProperty("ssh.private.key.path", "/Users/lahirugunathilake/.ssh/id_dsa");
////        System.setProperty("ssh.public.key.path", "/Users/lahirugunathilake/.ssh/id_dsa.pub");
////        System.setProperty("ssh.working.directory", "/tmp");
//
//        this.hostName = "bigred2.uits.iu.edu";
//        this.hostName = System.getProperty("ssh.host");
//        this.userName = System.getProperty("ssh.username");
//        this.password = System.getProperty("ssh.password");
//        this.privateKeyPath = System.getProperty("private.ssh.key");
//        this.publicKeyPath = System.getProperty("public.ssh.key");
//        this.passPhrase = System.getProperty("ssh.keypass");
//        this.workingDirectory = System.getProperty("ssh.working.directory");
//
//
//         if (this.userName == null
//                || (this.password==null && (this.publicKeyPath == null || this.privateKeyPath == null)) || this.workingDirectory == null) {
//            System.out.println("########### In order to test you have to either username password or private,public keys");
//            System.out.println("Use -Dssh.username=xxx -Dssh.password=yyy -Dssh.keypass=zzz " +
//                    "-Dprivate.ssh.key -Dpublic.ssh.key -Dssh.working.directory ");
//        }
//        URL resource = ApplicationSettings.loadFile(org.apache.airavata.common.utils.Constants.GFAC_CONFIG_XML);
//        assert resource != null;
//        System.out.println(resource.getFile());
//        GFacConfiguration gFacConfiguration = GFacConfiguration.create(new File(resource.getPath()), null);
//
////        gFacConfiguration.setMyProxyLifeCycle(3600);
////        gFacConfiguration.setMyProxyServer("myproxy.teragrid.org");
////        gFacConfiguration.setMyProxyUser("*****");
////        gFacConfiguration.setMyProxyPassphrase("*****");
////        gFacConfiguration.setTrustedCertLocation("./certificates");
////        //have to set InFlwo Handlers and outFlowHandlers
////        gFacConfiguration.setInHandlers(Arrays.asList(new String[] {"org.apache.airavata.gfac.handler.GramDirectorySetupHandler","org.apache.airavata.gfac.handler.GridFTPInputHandler"}));
////        gFacConfiguration.setOutHandlers(Arrays.asList(new String[] {"org.apache.airavata.gfac.handler.GridFTPOutputHandler"}));
//
//        /*
//        * Host
//        */
//        HostDescription host = new HostDescription(SSHHostType.type);
//        host.getType().setHostAddress(hostName);
//        host.getType().setHostName(hostName);
//        ((SSHHostType)host.getType()).setHpcResource(true);
//        /*
//        * App
//        */
//        ApplicationDescription appDesc = new ApplicationDescription(HpcApplicationDeploymentType.type);
//        HpcApplicationDeploymentType app = (HpcApplicationDeploymentType) appDesc.getType();
//        ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
//        name.setStringValue("EchoLocal");
//        app.setApplicationName(name);
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
//        String tempDir = "/tmp";
//        String date = (new Date()).toString();
//        date = date.replaceAll(" ", "_");
//        date = date.replaceAll(":", "_");
//
//        tempDir = tempDir + File.separator
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
//        app.setJobSubmitterCommand("aprun -n 1");
//        app.setInstalledParentPath("/opt/torque/torque-4.2.3.1/bin/");
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
//        jobExecutionContext.addSecurityContext(SSHSecurityContext.SSH_SECURITY_CONTEXT, getSecurityContext(app));
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
//
//    private SecurityContext getSecurityContext(HpcApplicationDeploymentType app) {
//         try {
//
//        AuthenticationInfo authenticationInfo = null;
//        if (password != null) {
//            authenticationInfo = new DefaultPasswordAuthenticationInfo(this.password);
//        } else {
//            authenticationInfo = new DefaultPublicKeyFileAuthentication(this.publicKeyPath, this.privateKeyPath,
//                    this.passPhrase);
//        }
//        // Server info
//        ServerInfo serverInfo = new ServerInfo(this.userName, this.hostName);
//
//        RemoteCluster pbsCluster = null;
//        SSHSecurityContext sshSecurityContext = null;
//
//            JobManagerConfiguration pbsJobManager = CommonUtils.getPBSJobManager(app.getInstalledParentPath());
//            pbsCluster = new HPCRemoteCluster(serverInfo, authenticationInfo, pbsJobManager);
//
//
//            sshSecurityContext = new SSHSecurityContext();
//            sshSecurityContext.setRemoteCluster(pbsCluster);
//            sshSecurityContext.setUsername(userName);
//            sshSecurityContext.setKeyPass(passPhrase);
//            sshSecurityContext.setPrivateKeyLoc(privateKeyPath);
//             return sshSecurityContext;
//        } catch (SSHApiException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//        return null;
//    }
//
//    @Test
//    public void testSSHProvider() throws GFacException {
//        BetterGfacImpl gFacAPI = new BetterGfacImpl();
//        gFacAPI.submitJob(jobExecutionContext.getExperimentID(), jobExecutionContext.getTaskData().getTaskID(), jobExecutionContext.getGatewayID());
//        org.junit.Assert.assertNotNull(jobExecutionContext.getJobDetails().getJobDescription());
//        org.junit.Assert.assertNotNull(jobExecutionContext.getJobDetails().getJobID());
//    }
//
//}
