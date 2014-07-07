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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.schemas.gfac.*;

public class DocumentCreator {

    private AiravataAPI airavataAPI = null;
    private String trestleshpcHostAddress = "trestles.sdsc.edu";
    private String lonestarHostAddress = "lonestar.tacc.utexas.edu";
    private String stampedeHostAddress = "stampede.tacc.xsede.org";
    private String gridftpAddress = "gsiftp://trestles-dm1.sdsc.edu:2811";
    private String gramAddress = "trestles-login1.sdsc.edu:2119/jobmanager-pbstest2";
    private String bigRed2HostAddress = "bigred2.uits.iu.edu";


    public DocumentCreator(AiravataAPI airavataAPI) {
        this.airavataAPI = airavataAPI;
    }

    public void createLocalHostDocs() {
        HostDescription descriptor = new HostDescription();
        descriptor.getType().setHostName("localhost");
        descriptor.getType().setHostAddress("127.0.0.1");
        try {
            airavataAPI.getApplicationManager().saveHostDescription(descriptor);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        String serviceName = "SimpleEcho0";
        ServiceDescription serviceDescription = new ServiceDescription();
        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        serviceDescription.getType().setName(serviceName);
        serviceDescription.getType().setDescription("Echo service");
        // Creating input parameters
        InputParameterType parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("echo_input");
        parameter.setParameterDescription("echo input");
        ParameterType parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.STRING);
        parameterType.setName("String");
        inputParameters.add(parameter);

        // Creating output parameters
        OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("echo_output");
        outputParameter.setParameterDescription("Echo output");
        ParameterType outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.STRING);
        outputParaType.setName("String");
        outputParameters.add(outputParameter);

        // Setting input and output parameters to serviceDescriptor
        serviceDescription.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[]{}));
        serviceDescription.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[]{}));

        try {
            airavataAPI.getApplicationManager().saveServiceDescription(serviceDescription);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        ApplicationDescription applicationDeploymentDescription = new ApplicationDescription();
        ApplicationDeploymentDescriptionType applicationDeploymentDescriptionType = applicationDeploymentDescription
                .getType();
        applicationDeploymentDescriptionType.addNewApplicationName().setStringValue("EchoApplication");
        applicationDeploymentDescriptionType.setExecutableLocation("/bin/echo");
        applicationDeploymentDescriptionType.setScratchWorkingDirectory("/tmp");

        try {
            airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, "localhost", applicationDeploymentDescription);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    public void createSSHHostDocs() {
        HostDescription descriptor = new HostDescription(SSHHostType.type);
        descriptor.getType().setHostName("gw111.iu.xsede.org");
        descriptor.getType().setHostAddress("gw111.iu.xsede.org");
        try {
            airavataAPI.getApplicationManager().saveHostDescription(descriptor);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        String serviceName = "SSHEcho1";
        ServiceDescription serviceDescription = new ServiceDescription();
        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        serviceDescription.getType().setName(serviceName);
        serviceDescription.getType().setDescription("Echo service");
        // Creating input parameters
        InputParameterType parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("echo_input");
        parameter.setParameterDescription("echo input");
        ParameterType parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.STRING);
        parameterType.setName("String");
        inputParameters.add(parameter);

        // Creating output parameters
        OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("echo_output");
        outputParameter.setParameterDescription("Echo output");
        ParameterType outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.STRING);
        outputParaType.setName("String");
        outputParameters.add(outputParameter);

        // Setting input and output parameters to serviceDescriptor
        serviceDescription.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[]{}));
        serviceDescription.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[]{}));

        try {
            airavataAPI.getApplicationManager().saveServiceDescription(serviceDescription);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        ApplicationDescription applicationDeploymentDescription = new ApplicationDescription();
        ApplicationDeploymentDescriptionType applicationDeploymentDescriptionType = applicationDeploymentDescription
                .getType();
        applicationDeploymentDescriptionType.addNewApplicationName().setStringValue("SSHEchoApplication");
        applicationDeploymentDescriptionType.setExecutableLocation("/bin/echo");
        applicationDeploymentDescriptionType.setScratchWorkingDirectory("/tmp");

        try {
            airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, "gw111.iu.xsede.org", applicationDeploymentDescription);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void createGramDocs() {
        /*
           creating host descriptor for gram
        */
        HostDescription host = new HostDescription(GlobusHostType.type);
        host.getType().setHostAddress(trestleshpcHostAddress);
        host.getType().setHostName(trestleshpcHostAddress);
        ((GlobusHostType) host.getType()).setGlobusGateKeeperEndPointArray(new String[]{gramAddress});
        ((GlobusHostType) host.getType()).setGridFTPEndPointArray(new String[]{gridftpAddress});
        try {
            airavataAPI.getApplicationManager().saveHostDescription(host);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        /*
        * Service Description creation and saving
        */
        String serviceName = "SimpleEcho1";
        ServiceDescription serv = new ServiceDescription();
        serv.getType().setName(serviceName);

        List<InputParameterType> inputList = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();

        InputParameterType input = InputParameterType.Factory.newInstance();
        input.setParameterName("echo_input");
        ParameterType parameterType = input.addNewParameterType();
        parameterType.setType(DataType.STRING);
        parameterType.setName("String");

        OutputParameterType output = OutputParameterType.Factory.newInstance();
        output.setParameterName("echo_output");
        ParameterType parameterType1 = output.addNewParameterType();
        parameterType1.setType(DataType.STRING);
        parameterType1.setName("String");

        inputList.add(input);
        outputList.add(output);

        InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList.size()]);
        OutputParameterType[] outputParamList = outputList.toArray(new OutputParameterType[outputList.size()]);

        serv.getType().setInputParametersArray(inputParamList);
        serv.getType().setOutputParametersArray(outputParamList);
        try {
            airavataAPI.getApplicationManager().saveServiceDescription(serv);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();
        }

        /*
            Application descriptor creation and saving
         */
        ApplicationDescription appDesc = new ApplicationDescription(HpcApplicationDeploymentType.type);
        HpcApplicationDeploymentType app = (HpcApplicationDeploymentType) appDesc.getType();
        ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
        name.setStringValue("EchoLocal");
        app.setApplicationName(name);
        ProjectAccountType projectAccountType = app.addNewProjectAccount();
        projectAccountType.setProjectAccountNumber("sds128");

        QueueType queueType = app.addNewQueue();
        queueType.setQueueName("normal");

        app.setCpuCount(1);
        app.setJobType(JobTypeType.SERIAL);
        app.setNodeCount(1);
        app.setProcessorsPerNode(1);

        /*
           * Use bat file if it is compiled on Windows
           */
        app.setExecutableLocation("/bin/echo");

        /*
           * Default tmp location
           */
        String tempDir = "/home/ogce/scratch";
        app.setScratchWorkingDirectory(tempDir);
        app.setMaxMemory(10);


        try {
            airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, trestleshpcHostAddress, appDesc);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void createPBSDocsForOGCE() {
        HostDescription host = new HostDescription(GsisshHostType.type);
        host.getType().setHostAddress(trestleshpcHostAddress);
        host.getType().setHostName(trestleshpcHostAddress);
        ((GsisshHostType) host.getType()).setPort(22);
        ((GsisshHostType) host.getType()).setInstalledPath("/opt/torque/bin/");

        try {
            airavataAPI.getApplicationManager().saveHostDescription(host);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        /*
        * Service Description creation and saving
        */
        String serviceName = "SimpleEcho2";
        ServiceDescription serv = new ServiceDescription();
        serv.getType().setName(serviceName);

        List<InputParameterType> inputList = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();


        InputParameterType input = InputParameterType.Factory.newInstance();
        input.setParameterName("echo_input");
        ParameterType parameterType = input.addNewParameterType();
        parameterType.setType(DataType.STRING);
        parameterType.setName("String");

        OutputParameterType output = OutputParameterType.Factory.newInstance();
        output.setParameterName("echo_output");
        ParameterType parameterType1 = output.addNewParameterType();
        parameterType1.setType(DataType.STRING);
        parameterType1.setName("String");

        inputList.add(input);
        outputList.add(output);

        InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList.size()]);
        OutputParameterType[] outputParamList = outputList.toArray(new OutputParameterType[outputList.size()]);

        serv.getType().setInputParametersArray(inputParamList);
        serv.getType().setOutputParametersArray(outputParamList);
        try {
            airavataAPI.getApplicationManager().saveServiceDescription(serv);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        /*
            Application descriptor creation and saving
         */
        ApplicationDescription appDesc = new ApplicationDescription(HpcApplicationDeploymentType.type);
        HpcApplicationDeploymentType app = (HpcApplicationDeploymentType) appDesc.getType();
        ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
        name.setStringValue("EchoLocal");
        app.setApplicationName(name);
        ProjectAccountType projectAccountType = app.addNewProjectAccount();
        projectAccountType.setProjectAccountNumber("sds128");

        QueueType queueType = app.addNewQueue();
        queueType.setQueueName("normal");

        app.setCpuCount(1);
        app.setJobType(JobTypeType.SERIAL);
        app.setNodeCount(1);
        app.setProcessorsPerNode(1);
        app.setMaxWallTime(10);
        /*
           * Use bat file if it is compiled on Windows
           */
        app.setExecutableLocation("/bin/echo");

        /*
           * Default tmp location
           */
        String tempDir = "/oasis/scratch/trestles/ogce/temp_project/";


        app.setScratchWorkingDirectory(tempDir);
        app.setInstalledParentPath("/opt/torque/bin/");

        try {
            airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, trestleshpcHostAddress, appDesc);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

         /*
        * Service Description creation and saving
        */
        String wrfserviceName = "WRF";
        ServiceDescription wrfServ = new ServiceDescription();
        wrfServ.getType().setName(wrfserviceName);

        List<InputParameterType> wrfinputList = new ArrayList<InputParameterType>();
        List<OutputParameterType> wrfoutputList = new ArrayList<OutputParameterType>();


        InputParameterType wrfinput1 = InputParameterType.Factory.newInstance();
        wrfinput1.setParameterName("WRF_Namelist");
        ParameterType wrfparameterType1 = wrfinput1.addNewParameterType();
        wrfparameterType1.setType(DataType.URI);
        wrfparameterType1.setName("URI");

        InputParameterType wrfinput2 = InputParameterType.Factory.newInstance();
        wrfinput2.setParameterName("WRF_Input_File");
        ParameterType wrfparameterType2 = wrfinput2.addNewParameterType();
        wrfparameterType2.setType(DataType.URI);
        wrfparameterType2.setName("URI");

        InputParameterType wrfinput3 = InputParameterType.Factory.newInstance();
        wrfinput3.setParameterName("WRF_Boundary_File");
        ParameterType wrfparameterType3 = wrfinput3.addNewParameterType();
        wrfparameterType3.setType(DataType.URI);
        wrfparameterType3.setName("URI");

        OutputParameterType wrfOutput1 = OutputParameterType.Factory.newInstance();
        wrfOutput1.setParameterName("WRF_Output");
        ParameterType wrfoutparameterType1 = wrfOutput1.addNewParameterType();
        wrfoutparameterType1.setType(DataType.URI);
        wrfoutparameterType1.setName("URI");

        OutputParameterType wrfOutput2 = OutputParameterType.Factory.newInstance();
        wrfOutput2.setParameterName("WRF_Execution_Log");
        ParameterType wrfoutparameterType2 = wrfOutput2.addNewParameterType();
        wrfoutparameterType2.setType(DataType.URI);
        wrfoutparameterType2.setName("URI");

        wrfinputList.add(wrfinput1);
        wrfinputList.add(wrfinput2);
        wrfinputList.add(wrfinput3);
        wrfoutputList.add(wrfOutput1);
        wrfoutputList.add(wrfOutput2);

        InputParameterType[] wrfinputParamList = wrfinputList.toArray(new InputParameterType[wrfinputList.size()]);
        OutputParameterType[] wrfoutputParamList = wrfoutputList.toArray(new OutputParameterType[wrfoutputList.size()]);

        wrfServ.getType().setInputParametersArray(wrfinputParamList);
        wrfServ.getType().setOutputParametersArray(wrfoutputParamList);
        try {
            airavataAPI.getApplicationManager().saveServiceDescription(wrfServ);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        /*
            Application descriptor creation and saving
         */
        ApplicationDescription wrfAppDesc = new ApplicationDescription(HpcApplicationDeploymentType.type);
        HpcApplicationDeploymentType wrfApp = (HpcApplicationDeploymentType) wrfAppDesc.getType();
        ApplicationDeploymentDescriptionType.ApplicationName wrfName = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
        wrfName.setStringValue("WRF");
        wrfApp.setApplicationName(wrfName);
        ProjectAccountType wrfprojectAccountType = wrfApp.addNewProjectAccount();
        wrfprojectAccountType.setProjectAccountNumber("sds128");

        QueueType wrfQueueType = wrfApp.addNewQueue();
        wrfQueueType.setQueueName("normal");

        wrfApp.setCpuCount(32);
        wrfApp.setJobType(JobTypeType.MPI);
        wrfApp.setNodeCount(2);
        wrfApp.setProcessorsPerNode(1);
        wrfApp.setMaxWallTime(30);
        /*
           * Use bat file if it is compiled on Windows
           */
        wrfApp.setExecutableLocation("/home/ogce/apps/wrf_wrapper.sh");

        /*
           * Default tmp location
           */
        String wrfTempDir = "/oasis/scratch/trestles/ogce/temp_project/";

        wrfApp.setScratchWorkingDirectory(wrfTempDir);
        wrfApp.setInstalledParentPath("/opt/torque/bin/");

        try {
            airavataAPI.getApplicationManager().saveApplicationDescription(wrfserviceName, trestleshpcHostAddress, wrfAppDesc);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    public void createSlurmDocs() {
        HostDescription host = new HostDescription(GsisshHostType.type);
        host.getType().setHostAddress(stampedeHostAddress);
        host.getType().setHostName(stampedeHostAddress);
        ((GsisshHostType) host.getType()).setJobManager("slurm");
        ((GsisshHostType) host.getType()).setInstalledPath("/usr/bin/");
        ((GsisshHostType) host.getType()).setPort(2222);
        ((GsisshHostType) host.getType()).setMonitorMode("push");


        try {
            airavataAPI.getApplicationManager().saveHostDescription(host);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        /*
        * Service Description creation and saving
        */
        String serviceName = "SimpleEcho3";
        ServiceDescription serv = new ServiceDescription();
        serv.getType().setName(serviceName);

        List<InputParameterType> inputList = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();


        InputParameterType input = InputParameterType.Factory.newInstance();
        input.setParameterName("echo_input");
        ParameterType parameterType = input.addNewParameterType();
        parameterType.setType(DataType.STRING);
        parameterType.setName("String");

        OutputParameterType output = OutputParameterType.Factory.newInstance();
        output.setParameterName("echo_output");
        ParameterType parameterType1 = output.addNewParameterType();
        parameterType1.setType(DataType.STRING);
        parameterType1.setName("String");

        inputList.add(input);
        outputList.add(output);

        InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList.size()]);
        OutputParameterType[] outputParamList = outputList.toArray(new OutputParameterType[outputList.size()]);

        serv.getType().setInputParametersArray(inputParamList);
        serv.getType().setOutputParametersArray(outputParamList);
        try {
            airavataAPI.getApplicationManager().saveServiceDescription(serv);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        /*
           Application descriptor creation and saving
        */
        ApplicationDescription appDesc = new ApplicationDescription(HpcApplicationDeploymentType.type);
        HpcApplicationDeploymentType app = (HpcApplicationDeploymentType) appDesc.getType();
        ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
        name.setStringValue("EchoLocal");
        app.setApplicationName(name);
        ProjectAccountType projectAccountType = app.addNewProjectAccount();
        projectAccountType.setProjectAccountNumber("TG-STA110014S");

        QueueType queueType = app.addNewQueue();
        queueType.setQueueName("normal");

        app.setCpuCount(1);
        app.setJobType(JobTypeType.SERIAL);
        app.setNodeCount(1);
        app.setProcessorsPerNode(1);
        app.setMaxWallTime(10);
        /*
        * Use bat file if it is compiled on Windows
        */
        app.setExecutableLocation("/bin/echo");

        /*
        * Default tmp location
        */
        String tempDir = "/home1/01437/ogce";


        app.setScratchWorkingDirectory(tempDir);
        app.setInstalledParentPath("/usr/bin/");

        try {
            airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, stampedeHostAddress, appDesc);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void createSGEDocs() {
        HostDescription host = new HostDescription(GsisshHostType.type);
        host.getType().setHostAddress(lonestarHostAddress);
        host.getType().setHostName(lonestarHostAddress);
        ((GsisshHostType) host.getType()).setJobManager("sge");
        ((GsisshHostType) host.getType()).setInstalledPath("/opt/sge6.2/bin/lx24-amd64/");
        ((GsisshHostType) host.getType()).setPort(22);
        try {
            airavataAPI.getApplicationManager().saveHostDescription(host);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        /*
        * Service Description creation and saving
        */
        String serviceName = "SimpleEcho4";
        ServiceDescription serv = new ServiceDescription();
        serv.getType().setName(serviceName);

        List<InputParameterType> inputList = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();


        InputParameterType input = InputParameterType.Factory.newInstance();
        input.setParameterName("echo_input");
        ParameterType parameterType = input.addNewParameterType();
        parameterType.setType(DataType.STRING);
        parameterType.setName("String");

        OutputParameterType output = OutputParameterType.Factory.newInstance();
        output.setParameterName("echo_output");
        ParameterType parameterType1 = output.addNewParameterType();
        parameterType1.setType(DataType.STRING);
        parameterType1.setName("String");

        inputList.add(input);
        outputList.add(output);

        InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList.size()]);
        OutputParameterType[] outputParamList = outputList.toArray(new OutputParameterType[outputList.size()]);

        serv.getType().setInputParametersArray(inputParamList);
        serv.getType().setOutputParametersArray(outputParamList);
        try {
            airavataAPI.getApplicationManager().saveServiceDescription(serv);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        /*
           Application descriptor creation and saving
        */
        ApplicationDescription appDesc = new ApplicationDescription(HpcApplicationDeploymentType.type);
        HpcApplicationDeploymentType app = (HpcApplicationDeploymentType) appDesc.getType();
        ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
        name.setStringValue("EchoLocal");
        app.setApplicationName(name);
        ProjectAccountType projectAccountType = app.addNewProjectAccount();
        projectAccountType.setProjectAccountNumber("TG-STA110014S");

        QueueType queueType = app.addNewQueue();
        queueType.setQueueName("normal");

        app.setCpuCount(1);
        app.setJobType(JobTypeType.SERIAL);
        app.setNodeCount(1);
        app.setProcessorsPerNode(1);
        app.setMaxWallTime(10);
        /*
        * Use bat file if it is compiled on Windows
        */
        app.setExecutableLocation("/bin/echo");

        /*
        * Default tmp location
        */
        String tempDir = "/home1/01437/ogce";


        app.setScratchWorkingDirectory(tempDir);
        app.setInstalledParentPath("/opt/sge6.2/bin/lx24-amd64/");

        try {
            airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, lonestarHostAddress, appDesc);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void createEchoHostDocs() {
        String serviceName = "Echo";
        ServiceDescription serviceDescription = new ServiceDescription();
        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        serviceDescription.getType().setName(serviceName);
        serviceDescription.getType().setDescription("Echo service");
        // Creating input parameters
        InputParameterType parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("echo_input");
        parameter.setParameterDescription("echo input");
        ParameterType parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.STRING);
        parameterType.setName("String");
        inputParameters.add(parameter);

        // Creating output parameters
        OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("echo_output");
        outputParameter.setParameterDescription("Echo output");
        ParameterType outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.STRING);
        outputParaType.setName("String");
        outputParameters.add(outputParameter);

        // Setting input and output parameters to serviceDescriptor
        serviceDescription.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[]{}));
        serviceDescription.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[]{}));

        try {
            airavataAPI.getApplicationManager().saveServiceDescription(serviceDescription);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace(); // To change body of catch statement use File |
            // Settings | File Templates.
        }
        // Localhost
        ApplicationDescription applicationDeploymentDescription = new ApplicationDescription();
        ApplicationDeploymentDescriptionType applicationDeploymentDescriptionType = applicationDeploymentDescription.getType();
        applicationDeploymentDescriptionType.addNewApplicationName().setStringValue(serviceName);
        applicationDeploymentDescriptionType.setExecutableLocation("/bin/echo");
        applicationDeploymentDescriptionType.setScratchWorkingDirectory("/tmp");

        try {
            airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, "localhost", applicationDeploymentDescription);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace(); // To change body of catch statement use File |
            // Settings | File Templates.
        }
        // Stampede
        /*
         * Application descriptor creation and saving
		 */
        ApplicationDescription appDesc1 = new ApplicationDescription(HpcApplicationDeploymentType.type);
        HpcApplicationDeploymentType app1 = (HpcApplicationDeploymentType) appDesc1.getType();
        ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
        name.setStringValue(serviceName);
        app1.setApplicationName(name);
        ProjectAccountType projectAccountType = app1.addNewProjectAccount();
        projectAccountType.setProjectAccountNumber("TG-STA110014S");

        QueueType queueType = app1.addNewQueue();
        queueType.setQueueName("normal");

        app1.setCpuCount(1);
        app1.setJobType(JobTypeType.SERIAL);
        app1.setNodeCount(1);
        app1.setProcessorsPerNode(1);
        app1.setMaxWallTime(10);
		/*
		 * Use bat file if it is compiled on Windows
		 */
        app1.setExecutableLocation("/bin/echo");

		/*
		 * Default tmp location
		 */
        String tempDir = "/home1/01437/ogce";

        app1.setScratchWorkingDirectory(tempDir);
        app1.setInstalledParentPath("/usr/bin/");

        try {
            airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, stampedeHostAddress, appDesc1);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace(); // To change body of catch statement use File |
            // Settings | File Templates.
        }
        // Trestles
		/*
		 * Application descriptor creation and saving
		 */
        ApplicationDescription appDesc2 = new ApplicationDescription(HpcApplicationDeploymentType.type);
        HpcApplicationDeploymentType app2 = (HpcApplicationDeploymentType) appDesc2.getType();
        ApplicationDeploymentDescriptionType.ApplicationName name2 = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
        name2.setStringValue(serviceName);
        app2.setApplicationName(name);
        ProjectAccountType projectAccountType2 = app2.addNewProjectAccount();
        projectAccountType2.setProjectAccountNumber("sds128");

        QueueType queueType2 = app2.addNewQueue();
        queueType2.setQueueName("normal");

        app2.setCpuCount(1);
        app2.setJobType(JobTypeType.SERIAL);
        app2.setNodeCount(1);
        app2.setProcessorsPerNode(1);
        app2.setMaxWallTime(10);
		/*
		 * Use bat file if it is compiled on Windows
		 */
        app2.setExecutableLocation("/bin/echo");

		/*
		 * Default tmp location
		 */
        String tempDir2 = "/home/ogce/scratch";

        app2.setScratchWorkingDirectory(tempDir2);
        app2.setInstalledParentPath("/opt/torque/bin/");

        try {
            airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, trestleshpcHostAddress, appDesc2);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace(); // To change body of catch statement use File |
            // Settings | File Templates.
        }
        // Lonestar
		/*
		 * Application descriptor creation and saving
		 */
        ApplicationDescription appDesc3 = new ApplicationDescription(HpcApplicationDeploymentType.type);
        HpcApplicationDeploymentType app3 = (HpcApplicationDeploymentType) appDesc3.getType();
        ApplicationDeploymentDescriptionType.ApplicationName name3 = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
        name3.setStringValue(serviceName);
        app3.setApplicationName(name);
        ProjectAccountType projectAccountType3 = app3.addNewProjectAccount();
        projectAccountType3.setProjectAccountNumber("TG-STA110014S");

        QueueType queueType3 = app3.addNewQueue();
        queueType3.setQueueName("normal");

        app3.setCpuCount(1);
        app3.setJobType(JobTypeType.SERIAL);
        app3.setNodeCount(1);
        app3.setProcessorsPerNode(1);
        app3.setMaxWallTime(10);
		/*
		 * Use bat file if it is compiled on Windows
		 */
        app3.setExecutableLocation("/bin/echo");

		/*
		 * Default tmp location
		 */
        String tempDir3 = "/home1/01437/ogce";

        app3.setScratchWorkingDirectory(tempDir3);
        app3.setInstalledParentPath("/opt/sge6.2/bin/lx24-amd64/");

        try {
            airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, lonestarHostAddress, appDesc3);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace(); // To change body of catch statement use File |
            // Settings | File Templates.
        }

    }

    public void createBigRedDocs() {
         /*
        * Host
        */
        HostDescription host = new HostDescription(SSHHostType.type);
        host.getType().setHostAddress(bigRed2HostAddress);
        host.getType().setHostName("bigred2");
        ((SSHHostType) host.getType()).setHpcResource(true);
        try {
            airavataAPI.getApplicationManager().saveHostDescription(host);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        /*
        * App
        */
        ApplicationDescription appDesc = new ApplicationDescription(HpcApplicationDeploymentType.type);
        HpcApplicationDeploymentType app = (HpcApplicationDeploymentType) appDesc.getType();
        ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
        name.setStringValue("EchoLocal");
        app.setApplicationName(name);

        app.setCpuCount(1);
        app.setJobType(JobTypeType.SERIAL);
        app.setNodeCount(1);
        app.setProcessorsPerNode(1);

        /*
        * Use bat file if it is compiled on Windows
        */
        app.setExecutableLocation("/bin/echo");

        /*
        * Default tmp location
        */
        String tempDir = "/tmp";
        String date = (new Date()).toString();
        date = date.replaceAll(" ", "_");
        date = date.replaceAll(":", "_");

        tempDir = tempDir + File.separator
                + "SimpleEcho" + "_" + date + "_" + UUID.randomUUID();

        System.out.println(tempDir);
        app.setScratchWorkingDirectory(tempDir);
        app.setStaticWorkingDirectory(tempDir);
        app.setInputDataDirectory(tempDir + File.separator + "inputData");
        app.setOutputDataDirectory(tempDir + File.separator + "outputData");
        app.setStandardOutput(tempDir + File.separator + app.getApplicationName().getStringValue() + ".stdout");
        app.setStandardError(tempDir + File.separator + app.getApplicationName().getStringValue() + ".stderr");
        app.setMaxWallTime(5);
        app.setJobSubmitterCommand("aprun -n 1");
        app.setInstalledParentPath("/opt/torque/torque-4.2.3.1/bin/");

        /*
        * Service
        */
        ServiceDescription serv = new ServiceDescription();
        serv.getType().setName("SimpleEchoBR");

        List<InputParameterType> inputList = new ArrayList<InputParameterType>();

        InputParameterType input = InputParameterType.Factory.newInstance();
        input.setParameterName("echo_input");
        input.setParameterType(StringParameterType.Factory.newInstance());
        inputList.add(input);

        InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList

                .size()]);
        List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();
        OutputParameterType output = OutputParameterType.Factory.newInstance();
        output.setParameterName("echo_output");
        output.setParameterType(StringParameterType.Factory.newInstance());
        outputList.add(output);

        OutputParameterType[] outputParamList = outputList
                .toArray(new OutputParameterType[outputList.size()]);

        serv.getType().setInputParametersArray(inputParamList);
        serv.getType().setOutputParametersArray(outputParamList);
        try {
            airavataAPI.getApplicationManager().saveServiceDescription(serv);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace(); // To change body of catch statement use File |
            // Settings | File Templates.
        }
        try {
            airavataAPI.getApplicationManager().saveApplicationDescription("SimpleEchoBR", trestleshpcHostAddress, appDesc);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace(); // To change body of catch statement use File |
            // Settings | File Templates.
        }
    }

    public AiravataAPI getAiravataAPI() {
        return airavataAPI;
    }

    public void setAiravataAPI(AiravataAPI airavataAPI) {
        this.airavataAPI = airavataAPI;
    }
}

