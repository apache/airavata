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

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.schemas.gfac.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DocumentCreator {

    private AiravataAPI airavataAPI = null;
    private String hpcHostAddress = "trestles.sdsc.edu";
    private String gramHostName = "gram-trestles";
    private String gsiSshHostName = "gsissh-trestles";
    private String gridftpAddress = "gsiftp://trestles-dm1.sdsc.edu:2811";
    private String gramAddress = "trestles-login1.sdsc.edu:2119/jobmanager-pbstest2";


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

        ServiceDescription serviceDescription = new ServiceDescription();
        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        serviceDescription.getType().setName("Echo");
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
            airavataAPI.getApplicationManager().saveApplicationDescription("Echo", "localhost", applicationDeploymentDescription);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void createGramDocs() {
        /*
           creating host descriptor for gram
        */
        HostDescription host = new HostDescription(GlobusHostType.type);
        host.getType().setHostAddress(hpcHostAddress);
        host.getType().setHostName(gramHostName);
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
        String date = (new Date()).toString();
        date = date.replaceAll(" ", "_");
        date = date.replaceAll(":", "_");

        tempDir = tempDir + File.separator
                + "SimpleEcho" + "_" + date + "_" + UUID.randomUUID();

        app.setScratchWorkingDirectory(tempDir);
        app.setStaticWorkingDirectory(tempDir);
        app.setInputDataDirectory(tempDir + File.separator + "inputData");
        app.setOutputDataDirectory(tempDir + File.separator + "outputData");
        app.setStandardOutput(tempDir + File.separator + app.getApplicationName().getStringValue() + ".stdout");
        app.setStandardError(tempDir + File.separator + app.getApplicationName().getStringValue() + ".stderr");
        app.setMaxMemory(10);


        try {
            airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, gramHostName, appDesc);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void createPBSDocs() {
        HostDescription host = new HostDescription(GsisshHostType.type);
        host.getType().setHostAddress(hpcHostAddress);
        host.getType().setHostName(gsiSshHostName);
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
        String tempDir = "/home/ogce/scratch";
        String date = (new Date()).toString();
        date = date.replaceAll(" ", "_");
        date = date.replaceAll(":", "_");

        tempDir = tempDir + File.separator
                + "SimpleEcho" + "_" + date + "_" + UUID.randomUUID();

        app.setScratchWorkingDirectory(tempDir);
        app.setStaticWorkingDirectory(tempDir);
        app.setInputDataDirectory(tempDir + File.separator + "inputData");
        app.setOutputDataDirectory(tempDir + File.separator + "outputData");
        app.setStandardOutput(tempDir + File.separator + app.getApplicationName().getStringValue() + ".stdout");
        app.setStandardError(tempDir + File.separator + app.getApplicationName().getStringValue() + ".stderr");
        app.setInstalledParentPath("/opt/torque/bin/");

        try {
            airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, gsiSshHostName, appDesc);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void createSlurmDocs() {
        HostDescription host = new HostDescription(GsisshHostType.type);
        host.getType().setHostAddress("stampede.tacc.xsede.org");
        host.getType().setHostName("stampede-host");
        ((GsisshHostType) host.getType()).setJobManager("slurm");
        ((GsisshHostType) host.getType()).setPort(2222);


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
        String date = (new Date()).toString();
        date = date.replaceAll(" ", "_");
        date = date.replaceAll(":", "_");

        tempDir = tempDir + File.separator
                + "SimpleEcho" + "_" + date + "_" + UUID.randomUUID();

        app.setScratchWorkingDirectory(tempDir);
        app.setStaticWorkingDirectory(tempDir);
        app.setInputDataDirectory(tempDir + File.separator + "inputData");
        app.setOutputDataDirectory(tempDir + File.separator + "outputData");
        app.setStandardOutput(tempDir + File.separator + app.getApplicationName().getStringValue() + ".stdout");
        app.setStandardError(tempDir + File.separator + app.getApplicationName().getStringValue() + ".stderr");
        app.setInstalledParentPath("/usr/bin/");

        try {
            airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, "stampede-host", appDesc);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public AiravataAPI getAiravataAPI() {
        return airavataAPI;
    }

    public void setAiravataAPI(AiravataAPI airavataAPI) {
        this.airavataAPI = airavataAPI;
    }
}

