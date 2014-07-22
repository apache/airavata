/**
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
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.DataType;
import org.apache.airavata.schemas.gfac.GsisshHostType;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.JobTypeType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.ParameterType;
import org.apache.airavata.schemas.gfac.ProjectAccountType;
import org.apache.airavata.schemas.gfac.QueueType;

public class UltrascanDocumentCreator {

	private AiravataAPI airavataAPI = null;
    private String hpcHostAddress = "trestles.sdsc.edu";
    private String gsiSshHostNameTrestles = "gsissh-trestles";
    private String gsiSshHostNameStampede = "gsissh-stampede";
  

    public UltrascanDocumentCreator(AiravataAPI airavataAPI) {
        this.airavataAPI = airavataAPI;
    }
    
    public void createEchoPBSDocsforTestles() {
        HostDescription host = new HostDescription(GsisshHostType.type);
        host.getType().setHostAddress(hpcHostAddress);
        host.getType().setHostName(gsiSshHostNameTrestles);
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
        String serviceName = "US3EchoTrestles";
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
        name.setStringValue(serviceName);
        app.setApplicationName(name);
        ProjectAccountType projectAccountType = app.addNewProjectAccount();
        projectAccountType.setProjectAccountNumber("uot111");

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
        String tempDir = "/oasis/projects/nsf/uot111/us3/airavata-workdirs/";

        app.setScratchWorkingDirectory(tempDir);
        app.setInstalledParentPath("/opt/torque/bin/");

        try {
            airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, gsiSshHostNameTrestles, appDesc);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void createMPIPBSDocsTrestles() {
        HostDescription host = new HostDescription(GsisshHostType.type);
        host.getType().setHostAddress(hpcHostAddress);
        host.getType().setHostName(gsiSshHostNameTrestles);
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
        String serviceName = "US3AppTrestles";
        ServiceDescription serv = new ServiceDescription();
        serv.getType().setName(serviceName);

        List<InputParameterType> inputList = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();


        InputParameterType input = InputParameterType.Factory.newInstance();
        input.setParameterName("input");
        ParameterType parameterType = input.addNewParameterType();
        parameterType.setType(DataType.URI);
        parameterType.setName("URI");

        OutputParameterType output = OutputParameterType.Factory.newInstance();
        output.setParameterName("output");
        ParameterType parameterType1 = output.addNewParameterType();
        parameterType1.setType(DataType.URI);
        parameterType1.setName("URI");
        
        OutputParameterType output1 = OutputParameterType.Factory.newInstance();
        output1.setParameterName("stdout");
        ParameterType parameterType2 = output1.addNewParameterType();
        parameterType2.setType(DataType.STD_OUT);
        parameterType2.setName("StdOut");
        
        OutputParameterType output2 = OutputParameterType.Factory.newInstance();
        output2.setParameterName("stderr");
        ParameterType parameterType3 = output2.addNewParameterType();
        parameterType3.setType(DataType.STD_ERR);
        parameterType3.setName("StdErr");

        inputList.add(input);
        outputList.add(output);
        outputList.add(output1);
        outputList.add(output2);
        
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
        name.setStringValue(serviceName);
        app.setApplicationName(name);
        ProjectAccountType projectAccountType = app.addNewProjectAccount();
        projectAccountType.setProjectAccountNumber("uot111");

        QueueType queueType = app.addNewQueue();
        queueType.setQueueName("normal");

        app.setCpuCount(1);
        app.setJobType(JobTypeType.MPI);
        app.setNodeCount(32);
        app.setProcessorsPerNode(2);
        app.setMaxWallTime(10);
        /*
           * Use bat file if it is compiled on Windows
           */
        app.setExecutableLocation("/home/us3/trestles/bin/us_mpi_analysis");

        /*
           * Default tmp location
           */
        String tempDir = "/oasis/projects/nsf/uot111/us3/airavata-workdirs/";
        app.setScratchWorkingDirectory(tempDir);
        app.setInstalledParentPath("/opt/torque/bin/");
        app.setJobSubmitterCommand("/opt/mvapich2/pgi/ib/bin/mpiexec");
        try {
            airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, gsiSshHostNameTrestles, appDesc);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    public void createMPISLURMDocsStampede() {
        HostDescription host = new HostDescription(GsisshHostType.type);
        host.getType().setHostAddress("stampede.tacc.xsede.org");
        host.getType().setHostName("gsissh-stampede");
        ((GsisshHostType) host.getType()).setJobManager("slurm");
        ((GsisshHostType) host.getType()).setInstalledPath("/usr/bin/");
        ((GsisshHostType) host.getType()).setPort(2222);
      
        try {
            airavataAPI.getApplicationManager().saveHostDescription(host);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        /*
        * Service Description creation and saving
        */
        String serviceName = "US3AppStampede";
        ServiceDescription serv = new ServiceDescription();
        serv.getType().setName(serviceName);

        List<InputParameterType> inputList = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();


        InputParameterType input = InputParameterType.Factory.newInstance();
        input.setParameterName("input");
        ParameterType parameterType = input.addNewParameterType();
        parameterType.setType(DataType.URI);
        parameterType.setName("URI");

        OutputParameterType output = OutputParameterType.Factory.newInstance();
        output.setParameterName("output");
        ParameterType parameterType1 = output.addNewParameterType();
        parameterType1.setType(DataType.URI);
        parameterType1.setName("URI");
        
        OutputParameterType output1 = OutputParameterType.Factory.newInstance();
        output1.setParameterName("stdout");
        ParameterType parameterType2 = output1.addNewParameterType();
        parameterType2.setType(DataType.STD_OUT);
        parameterType2.setName("StdOut");
        
        OutputParameterType output2 = OutputParameterType.Factory.newInstance();
        output2.setParameterName("stderr");
        ParameterType parameterType3 = output2.addNewParameterType();
        parameterType3.setType(DataType.STD_ERR);
        parameterType3.setName("StdErr");

        inputList.add(input);
        outputList.add(output);
        outputList.add(output1);
        outputList.add(output2);
        
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
        name.setStringValue(serviceName);
        app.setApplicationName(name);
        ProjectAccountType projectAccountType = app.addNewProjectAccount();
        projectAccountType.setProjectAccountNumber("TG-MCB070039N");

        QueueType queueType = app.addNewQueue();
        queueType.setQueueName("normal");

        app.setCpuCount(1);
        app.setJobType(JobTypeType.MPI);
        app.setNodeCount(32);
        app.setProcessorsPerNode(2);
        app.setMaxWallTime(10);
        /*
           * Use bat file if it is compiled on Windows
           */
        app.setExecutableLocation("/home1/01623/us3/bin/us_mpi_analysis");

        /*
           * Default tmp location
           */
        String tempDir = "/home1/01623/us3";
        app.setScratchWorkingDirectory(tempDir);
        app.setInstalledParentPath("/usr/bin/");
        app.setJobSubmitterCommand("/usr/local/bin/ibrun");
        try {
            airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, gsiSshHostNameStampede, appDesc);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    public void createEchoSlurmDocsofStampede() {
        HostDescription host = new HostDescription(GsisshHostType.type);
        host.getType().setHostAddress("stampede.tacc.xsede.org");
        host.getType().setHostName("stampede-host");
        ((GsisshHostType) host.getType()).setJobManager("slurm");
        ((GsisshHostType) host.getType()).setInstalledPath("/usr/bin/");
        ((GsisshHostType) host.getType()).setPort(2222);
        ((GsisshHostType) host.getType()).setMonitorMode("push");
//        ((GsisshHostType) host.getType()).setMo(2222);


        try {
            airavataAPI.getApplicationManager().saveHostDescription(host);
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        /*
        * Service Description creation and saving
        */
        String serviceName = "US3EchoStampede";
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
        name.setStringValue(serviceName);
        app.setApplicationName(name);
        ProjectAccountType projectAccountType = app.addNewProjectAccount();
        projectAccountType.setProjectAccountNumber("TG-MCB070039N");

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
        String tempDir = "/home1/01623/us3";
       
        app.setScratchWorkingDirectory(tempDir);
        app.setInstalledParentPath("/usr/bin/");

        try {
            airavataAPI.getApplicationManager().saveApplicationDescription(serviceName, gsiSshHostNameStampede, appDesc);
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
