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

package org.apache.airavata.rest.mappings.utils;

import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.rest.mappings.resourcemappings.ApplicationDescriptor;
import org.apache.airavata.rest.mappings.resourcemappings.HostDescriptor;
import org.apache.airavata.rest.mappings.resourcemappings.ServiceDescriptor;
import org.apache.airavata.rest.mappings.resourcemappings.ServiceParameters;
import org.apache.airavata.schemas.gfac.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DescriptorUtil {

    public static HostDescription createHostDescription(String hostName, String hostAddress,
                                                        String hostEndpoint, String gatekeeperEndpoint, String providerType) {
        HostDescription host = new HostDescription();
        host.getType().setHostName(hostName);
        host.getType().setHostAddress(hostAddress);
        if(providerType.equalsIgnoreCase(HostTypes.GLOBUS_HOST_TYPE)){
        	 host.getType().changeType(GlobusHostType.type);
             ((GlobusHostType) host.getType()).
                     setGridFTPEndPointArray(new String[]{hostEndpoint});
             ((GlobusHostType) host.getType()).
                     setGlobusGateKeeperEndPointArray(new String[]{gatekeeperEndpoint});
        }else if (providerType.equalsIgnoreCase(HostTypes.SSH_HOST_TYPE)){
          	 host.getType().changeType(SSHHostType.type);
        }else if (providerType.equalsIgnoreCase(HostTypes.UNICORE_HOST_TYPE)){
        	 host.getType().changeType(GlobusHostType.type);
             ((UnicoreHostType) host.getType()).
                     setGridFTPEndPointArray(new String[]{hostEndpoint});
             ((UnicoreHostType) host.getType()).
                     setUnicoreBESEndPointArray(new String[]{gatekeeperEndpoint});
        }
        return host;
    }

    public static ApplicationDescription registerApplication(String appName, String exeuctableLocation, String scratchWorkingDirectory, String hostName,
                                                                       String projAccNumber, String queueName, String cpuCount, String nodeCount, String maxMemory) throws Exception {
        // Create Application Description
        ApplicationDescription appDesc = new ApplicationDescription(HpcApplicationDeploymentType.type);
        HpcApplicationDeploymentType app = (HpcApplicationDeploymentType) appDesc.getType();
        app.setCpuCount(Integer.parseInt(cpuCount));
        app.setNodeCount(Integer.parseInt(nodeCount));
        ApplicationDeploymentDescriptionType.ApplicationName name = appDesc.getType().addNewApplicationName();
        name.setStringValue(appName);
        app.setExecutableLocation(exeuctableLocation);
        app.setScratchWorkingDirectory(scratchWorkingDirectory);
        ProjectAccountType projectAccountType = ((HpcApplicationDeploymentType) appDesc.getType()).addNewProjectAccount();
        projectAccountType.setProjectAccountNumber(projAccNumber);
        QueueType queueType = app.addNewQueue();
        queueType.setQueueName(queueName);
        app.setMaxMemory(Integer.parseInt(maxMemory));
        return appDesc;
    }

    public static ServiceDescription getServiceDescription(String serviceName, String inputName, String inputType,
                                                           String outputName, String outputType) {
        // Create Service Description
        ServiceDescription serv = new ServiceDescription();
        serv.getType().setName(serviceName);

        InputParameterType input = InputParameterType.Factory.newInstance();
        input.setParameterName(inputName);
        ParameterType parameterType = input.addNewParameterType();
        parameterType.setType(DataType.Enum.forString(inputType));
        parameterType.setName(inputName);
        List<InputParameterType> inputList = new ArrayList<InputParameterType>();
        inputList.add(input);
        InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList
                .size()]);

        OutputParameterType output = OutputParameterType.Factory.newInstance();
        output.setParameterName(outputName);
        ParameterType parameterType1 = output.addNewParameterType();
        parameterType1.setType(DataType.Enum.forString(outputType));
        parameterType1.setName(outputName);
        List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();
        outputList.add(output);
        OutputParameterType[] outputParamList = outputList
                .toArray(new OutputParameterType[outputList.size()]);
        serv.getType().setInputParametersArray(inputParamList);
        serv.getType().setOutputParametersArray(outputParamList);
        return serv;
    }


    public static HostDescriptor createHostDescriptor (HostDescription hostDescription){
        List<String> hostType = new ArrayList<String>();
        List<String> gridFTPEndPoint = new ArrayList<String>();
        List<String> gateKeeperEndPoint  = new ArrayList<String>();
        List<String> imageID  = new ArrayList<String>();
        List<String> instanceID  = new ArrayList<String>();

        HostDescriptor hostDescriptor = new HostDescriptor();
        hostDescriptor.setHostname(hostDescription.getType().getHostName());
        hostDescriptor.setHostAddress(hostDescription.getType().getHostAddress());

        HostDescriptionType hostDescriptionType = hostDescription.getType();
        if (hostDescriptionType instanceof GlobusHostType){
            GlobusHostType globusHostType = (GlobusHostType) hostDescriptionType;
            hostType.add(HostTypes.GLOBUS_HOST_TYPE);
            String[] globusGateKeeperEndPointArray = globusHostType.getGlobusGateKeeperEndPointArray();
            for (int i = 0; i < globusGateKeeperEndPointArray.length ; i++){
                gateKeeperEndPoint.add(globusGateKeeperEndPointArray[i]);
            }

            String[] gridFTPEndPointArray = globusHostType.getGridFTPEndPointArray();
            for (int i = 0; i < gridFTPEndPointArray.length ; i++){
                gridFTPEndPoint.add(gridFTPEndPointArray[i]);
            }
        }else if (hostDescriptionType instanceof GsisshHostType){
            GsisshHostType gsisshHostType = (GsisshHostType) hostDescriptionType;
            hostType.add(HostTypes.GSISSH_HOST_TYPE);

            String[] gridFTPEndPointArray = gsisshHostType.getGridFTPEndPointArray();
            for (int i = 0; i < gridFTPEndPointArray.length ; i++){
                gridFTPEndPoint.add(gridFTPEndPointArray[i]);
            }
        }  else if (hostDescriptionType instanceof  SSHHostType) {
            hostType.add(HostTypes.SSH_HOST_TYPE);
        } else if (hostDescriptionType instanceof  UnicoreHostType) {
        	UnicoreHostType unicoreHostType = (UnicoreHostType) hostDescriptionType;
             hostType.add(HostTypes.UNICORE_HOST_TYPE);
             String[] unicoreGateKeeperEndPointArray = unicoreHostType.getUnicoreBESEndPointArray();
             for (int i = 0; i < unicoreGateKeeperEndPointArray.length ; i++){
                 gateKeeperEndPoint.add(unicoreGateKeeperEndPointArray[i]);
             }

             String[] gridFTPEndPointArray = unicoreHostType.getGridFTPEndPointArray();
             for (int i = 0; i < gridFTPEndPointArray.length ; i++){
                 gridFTPEndPoint.add(gridFTPEndPointArray[i]);
             }
        }  else if (hostDescriptionType instanceof  Ec2HostType) {
            hostType.add(HostTypes.EC2_HOST_TYPE);
        } else {
            hostType.add(HostTypes.HOST_DESCRIPTION_TYPE);
        }
        hostDescriptor.setGateKeeperEndPoint(gateKeeperEndPoint);
        hostDescriptor.setGridFTPEndPoint(gridFTPEndPoint);
        hostDescriptor.setImageID(imageID);
        hostDescriptor.setInstanceID(instanceID);
        hostDescriptor.setHostType(hostType);
        return hostDescriptor;
    }

    public static HostDescription createHostDescription (HostDescriptor hostDescriptor){
        HostDescription hostDescription = new HostDescription(HostDescriptionType.type);
        hostDescription.getType().setHostAddress(hostDescriptor.getHostAddress());
        hostDescription.getType().setHostName(hostDescriptor.getHostname());

        if (hostDescriptor.getHostType() != null && !hostDescriptor.getHostType().isEmpty()) {
            if (hostDescriptor.getHostType().get(0).equals(HostTypes.GLOBUS_HOST_TYPE)) {
                hostDescription.getType().changeType(GlobusHostType.type);
                if (!hostDescriptor.getGateKeeperEndPoint().isEmpty() && hostDescriptor.getGateKeeperEndPoint() != null){
                    ((GlobusHostType) hostDescription.getType()).addGlobusGateKeeperEndPoint(hostDescriptor.getGateKeeperEndPoint().get(0));
                }
                if (!hostDescriptor.getGridFTPEndPoint().isEmpty() && hostDescriptor.getGridFTPEndPoint() != null){
                    ((GlobusHostType) hostDescription.getType()).addGridFTPEndPoint(hostDescriptor.getGridFTPEndPoint().get(0));
                }

            } else if (hostDescriptor.getHostType().get(0).equals(HostTypes.GSISSH_HOST_TYPE)) {
                hostDescription.getType().changeType(GsisshHostType.type);
                if (!hostDescriptor.getGridFTPEndPoint().isEmpty() && hostDescriptor.getGridFTPEndPoint() != null){
                    ((GsisshHostType) hostDescription).addGridFTPEndPoint(hostDescriptor.getGridFTPEndPoint().get(0));
                }

            } else if (hostDescriptor.getHostType().get(0).equals(HostTypes.EC2_HOST_TYPE)) {
                hostDescription.getType().changeType(Ec2HostType.type);
                if (!hostDescriptor.getImageID().isEmpty() && hostDescriptor.getImageID() != null ){
                    ((Ec2HostType) hostDescription).addImageID(hostDescriptor.getImageID().get(0));
                }
                if (!hostDescriptor.getInstanceID().isEmpty() && hostDescriptor.getInstanceID() != null){
                    ((Ec2HostType) hostDescription).addInstanceID(hostDescriptor.getInstanceID().get(0));
                }

            }else if (hostDescriptor.getHostType().get(0).equals(HostTypes.SSH_HOST_TYPE)) {
            	hostDescription.getType().changeType(SSHHostType.type);
            } else if (hostDescriptor.getHostType().get(0).equals(HostTypes.UNICORE_HOST_TYPE)) {
                 hostDescription.getType().changeType(UnicoreHostType.type);
                 if (!hostDescriptor.getGateKeeperEndPoint().isEmpty() && hostDescriptor.getGateKeeperEndPoint() != null){
                     ((UnicoreHostType) hostDescription.getType()).addUnicoreBESEndPoint(hostDescriptor.getGateKeeperEndPoint().get(0));
                 }
                 if (!hostDescriptor.getGridFTPEndPoint().isEmpty() && hostDescriptor.getGridFTPEndPoint() != null){
                     ((UnicoreHostType) hostDescription.getType()).addGridFTPEndPoint(hostDescriptor.getGridFTPEndPoint().get(0));
                 }
            }
        }

        return hostDescription;
    }

    public static ServiceDescription createServiceDescription (ServiceDescriptor serviceDescriptor){
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.getType().setName(serviceDescriptor.getServiceName());
        serviceDescription.getType().setDescription(serviceDescriptor.getDescription());
        List<ServiceParameters> inputParams = serviceDescriptor.getInputParams();
        InputParameterType[] inputParameterTypeArray = new InputParameterType[inputParams.size()];
        for (int i = 0; i < inputParams.size(); i++){
            InputParameterType parameter = InputParameterType.Factory.newInstance();
            parameter.setParameterName(inputParams.get(i).getName());
            parameter.setParameterValueArray(new String[]{inputParams.get(i).getName()});
            ParameterType parameterType = parameter.addNewParameterType();
            parameterType.setType(DataType.Enum.forString(inputParams.get(i).getType()));
            parameterType.setName(inputParams.get(i).getType());
            parameter.setParameterType(parameterType);
            inputParameterTypeArray[i] = parameter;
        }
        serviceDescription.getType().setInputParametersArray(inputParameterTypeArray);

        List<ServiceParameters> outputParams = serviceDescriptor.getOutputParams();
        OutputParameterType[] outputParameterTypeArray = new OutputParameterType[outputParams.size()];
        for (int i = 0; i < outputParams.size(); i++){
            OutputParameterType parameter = OutputParameterType.Factory.newInstance();
            parameter.setParameterName(outputParams.get(i).getName());
            ParameterType parameterType = parameter.addNewParameterType();
            parameterType.setType(DataType.Enum.forString(outputParams.get(i).getType()));
            parameterType.setName(outputParams.get(i).getType());
            parameter.setParameterType(parameterType);
            outputParameterTypeArray[i] = parameter;
        }
        serviceDescription.getType().setOutputParametersArray(outputParameterTypeArray);
        return serviceDescription;
    }

    public static ServiceDescriptor createServiceDescriptor(ServiceDescription serviceDescription){
        ServiceDescriptor serviceDescriptor = new ServiceDescriptor();
        serviceDescriptor.setServiceName(serviceDescription.getType().getName());
        serviceDescriptor.setDescription(serviceDescription.getType().getDescription());
        InputParameterType[] inputParametersArray = serviceDescription.getType().getInputParametersArray();
        OutputParameterType[] outputParametersArray = serviceDescription.getType().getOutputParametersArray();
        List<ServiceParameters> inputParams = new ArrayList<ServiceParameters>();
        List<ServiceParameters> outputParams = new ArrayList<ServiceParameters>();

        for (int i = 0; i < inputParametersArray.length; i++){
            ServiceParameters serviceParameters = new ServiceParameters();
            serviceParameters.setType(inputParametersArray[i].getParameterType().getType().toString());
//            String[] parameterValueArray = inputParametersArray[i].getParameterValueArray();
//            if (parameterValueArray.length != 0){
//                serviceParameters.setName(parameterValueArray[0]);
//            }
            serviceParameters.setName(inputParametersArray[i].getParameterName());
            serviceParameters.setDescription(inputParametersArray[i].getParameterDescription());
//            serviceParameters.set(inputParametersArray[i].getParameterType().getType().toString());
            inputParams.add(serviceParameters);
        }
        serviceDescriptor.setInputParams(inputParams);

        for (int i = 0; i < outputParametersArray.length; i++){
            ServiceParameters serviceParameters = new ServiceParameters();
            serviceParameters.setType(outputParametersArray[i].getParameterType().getType().toString());
            serviceParameters.setName(outputParametersArray[i].getParameterName());
            serviceParameters.setDescription(outputParametersArray[i].getParameterDescription());
//            serviceParameters.setDataType(outputParametersArray[i].getParameterType().getType().toString());
            outputParams.add(serviceParameters);
        }
        serviceDescriptor.setOutputParams(outputParams);
        return serviceDescriptor;
    }

    public static ApplicationDescription createApplicationDescription(ApplicationDescriptor applicationDescriptor){
        ApplicationDescription applicationDescription = new ApplicationDescription();
        ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
        name.setStringValue(applicationDescriptor.getName());
        applicationDescription.getType().setApplicationName(name);
        applicationDescription.getType().setExecutableLocation(applicationDescriptor.getExecutablePath());
        applicationDescription.getType().setScratchWorkingDirectory(applicationDescriptor.getWorkingDir());

        if (applicationDescriptor.getInputDir() != null){
            applicationDescription.getType().setInputDataDirectory(applicationDescriptor.getInputDir());
        }
        if (applicationDescriptor.getOutputDir() != null){
            applicationDescription.getType().setOutputDataDirectory(applicationDescriptor.getOutputDir());
        }
        if (applicationDescriptor.getStdIn() != null){
            applicationDescription.getType().setStandardInput(applicationDescriptor.getStdIn());
        }
        if (applicationDescriptor.getStdOut() != null){
            applicationDescription.getType().setStandardOutput(applicationDescriptor.getStdOut());
        }
        if (applicationDescriptor.getStdError() != null){
            applicationDescription.getType().setStandardError(applicationDescriptor.getStdError());
        }
        if (applicationDescriptor.getStaticWorkigDir() != null){
            applicationDescription.getType().setStaticWorkingDirectory(applicationDescriptor.getStaticWorkigDir());
        }
        HashMap<String,String> environmentVariables = applicationDescriptor.getEnvironmentVariables();
        if (environmentVariables != null && !environmentVariables.isEmpty()){
            NameValuePairType[] appEnviVariablesArray = new NameValuePairType[environmentVariables.size()];
            for(String key : environmentVariables.keySet()) {
                int i = 0;
                NameValuePairType nameValuePairType = applicationDescription.getType().addNewApplicationEnvironment();
                nameValuePairType.setName(key);
                nameValuePairType.setValue(environmentVariables.get(key));
                appEnviVariablesArray[i] = nameValuePairType;
                i++;
            }
            applicationDescription.getType().setApplicationEnvironmentArray(appEnviVariablesArray);
        }

        //set advanced options according app desc type
        if(applicationDescriptor.getApplicationDescType() != null && !applicationDescriptor.getApplicationDescType().isEmpty()){
            if (applicationDescriptor.getApplicationDescType().equals(ApplicationDescriptorTypes.HPC_APP_DEP_DESC_TYPE)){
                ApplicationDescription appDesc = new ApplicationDescription(HpcApplicationDeploymentType.type);
                appDesc.getType().setApplicationName(name);
                appDesc.getType().setExecutableLocation(applicationDescriptor.getExecutablePath());
                appDesc.getType().setScratchWorkingDirectory(applicationDescriptor.getWorkingDir());

                if (applicationDescriptor.getInputDir() != null){
                    appDesc.getType().setInputDataDirectory(applicationDescriptor.getInputDir());
                }
                if (applicationDescriptor.getOutputDir() != null){
                    appDesc.getType().setOutputDataDirectory(applicationDescriptor.getOutputDir());
                }
                if (applicationDescriptor.getStdIn() != null){
                    appDesc.getType().setStandardInput(applicationDescriptor.getStdIn());
                }
                if (applicationDescriptor.getStdOut() != null){
                    appDesc.getType().setStandardOutput(applicationDescriptor.getStdOut());
                }
                if (applicationDescriptor.getStdError() != null){
                    appDesc.getType().setStandardError(applicationDescriptor.getStdError());
                }
                if (applicationDescriptor.getStaticWorkigDir() != null){
                    appDesc.getType().setStaticWorkingDirectory(applicationDescriptor.getStaticWorkigDir());
                }
                HashMap<String,String> envVariables = applicationDescriptor.getEnvironmentVariables();
                if (envVariables != null && !envVariables.isEmpty()){
                    NameValuePairType[] appEnviVariablesArray = new NameValuePairType[envVariables.size()];
                    for(String key : envVariables.keySet()) {
                        int i = 0;
                        NameValuePairType nameValuePairType = applicationDescription.getType().addNewApplicationEnvironment();
                        nameValuePairType.setName(key);
                        nameValuePairType.setValue(envVariables.get(key));
                        appEnviVariablesArray[i] = nameValuePairType;
                        i++;
                    }
                    appDesc.getType().setApplicationEnvironmentArray(appEnviVariablesArray);
                }

                HpcApplicationDeploymentType app = (HpcApplicationDeploymentType) appDesc.getType();

                ProjectAccountType projectAccountType = app.addNewProjectAccount();
                if (applicationDescriptor.getProjectNumber() != null){
                    projectAccountType.setProjectAccountNumber(applicationDescriptor.getProjectNumber());
                }
                if (applicationDescriptor.getProjectDescription() != null){
                    projectAccountType.setProjectAccountDescription(applicationDescriptor.getProjectDescription());
                }
                app.setProjectAccount(projectAccountType);

                app.setCpuCount(applicationDescriptor.getCpuCount());
                if (applicationDescriptor.getJobType() != null){
                    app.setJobType(JobTypeType.Enum.forString(applicationDescriptor.getJobType()));
                }

                app.setMaxMemory(applicationDescriptor.getMaxMemory());
                app.setMinMemory(applicationDescriptor.getMinMemory());
                app.setMaxWallTime(applicationDescriptor.getMaxWallTime());
                app.setNodeCount(applicationDescriptor.getNodeCount());
                app.setProcessorsPerNode(applicationDescriptor.getProcessorsPerNode());

                QueueType queueType = app.addNewQueue();
                if (applicationDescriptor.getQueueName() != null){
                    queueType.setQueueName(applicationDescriptor.getQueueName());
                }
                app.setQueue(queueType);

                return appDesc;
            }
        }
        return applicationDescription;
    }

    public static ApplicationDescriptor createApplicationDescriptor (ApplicationDescription applicationDescription){
        ApplicationDescriptor applicationDescriptor = new ApplicationDescriptor();
        applicationDescriptor.setName(applicationDescription.getType().getApplicationName().getStringValue());
        applicationDescriptor.setExecutablePath(applicationDescription.getType().getExecutableLocation());
        applicationDescriptor.setWorkingDir(applicationDescription.getType().getScratchWorkingDirectory());

        if (applicationDescription.getType().getInputDataDirectory() != null && !applicationDescription.getType().getInputDataDirectory().equals("") ){
            applicationDescriptor.setInputDir(applicationDescription.getType().getInputDataDirectory());
        }
        if (applicationDescription.getType().getOutputDataDirectory() != null && !applicationDescription.getType().getOutputDataDirectory().equals("")){
            applicationDescriptor.setOutputDir(applicationDescription.getType().getOutputDataDirectory());
        }
        if (applicationDescription.getType().getStaticWorkingDirectory() != null && !applicationDescription.getType().getStaticWorkingDirectory().equals("")){
            applicationDescriptor.setStaticWorkigDir(applicationDescription.getType().getStaticWorkingDirectory());
        }
        if (applicationDescription.getType().getStandardInput() != null && !applicationDescription.getType().getStandardInput().equals("")){
            applicationDescriptor.setStdIn(applicationDescription.getType().getStandardInput());
        }
        if (applicationDescription.getType().getStandardOutput() != null && !applicationDescription.getType().getStandardOutput().equals("")){
            applicationDescriptor.setStdOut(applicationDescription.getType().getStandardOutput());
        }
        if (applicationDescription.getType().getStandardError() != null && !applicationDescription.getType().getStandardError().equals("")){
            applicationDescriptor.setStdError(applicationDescription.getType().getStandardError());
        }
        NameValuePairType[] environmentArray = applicationDescription.getType().getApplicationEnvironmentArray();
        HashMap<String, String> environmentVariableMap = new HashMap<String, String>();
        if (environmentArray != null && environmentArray.length != 0){
            for (NameValuePairType nameValuePairType : environmentArray){
                environmentVariableMap.put(nameValuePairType.getName(), nameValuePairType.getValue());
            }
            applicationDescriptor.setEnvironmentVariables(environmentVariableMap);
        }

        if(applicationDescription.getType() != null){
            if(applicationDescription.getType() instanceof HpcApplicationDeploymentType){
                applicationDescriptor.setApplicationDescType(ApplicationDescriptorTypes.HPC_APP_DEP_DESC_TYPE);
                HpcApplicationDeploymentType gramApplicationDeploymentType = (HpcApplicationDeploymentType) applicationDescription.getType();
                if(gramApplicationDeploymentType != null){
                    applicationDescriptor.setCpuCount(gramApplicationDeploymentType.getCpuCount());
                    applicationDescriptor.setNodeCount(gramApplicationDeploymentType.getNodeCount());
                    applicationDescriptor.setMaxMemory(gramApplicationDeploymentType.getMaxMemory());
                    applicationDescriptor.setMinMemory(gramApplicationDeploymentType.getMinMemory());
                    applicationDescriptor.setMaxWallTime(gramApplicationDeploymentType.getMaxWallTime());
                    if (gramApplicationDeploymentType.getJobType() != null)  {
                        applicationDescriptor.setJobType(gramApplicationDeploymentType.getJobType().toString());
                    }
                    if (gramApplicationDeploymentType.getProjectAccount() != null){
                        if (gramApplicationDeploymentType.getProjectAccount().getProjectAccountNumber() != null){
                            applicationDescriptor.setProjectNumber(gramApplicationDeploymentType.getProjectAccount().getProjectAccountNumber());
                        }
                    }
                    if (gramApplicationDeploymentType.getProjectAccount() != null){
                        if (gramApplicationDeploymentType.getProjectAccount().getProjectAccountDescription() != null){
                            applicationDescriptor.setProjectDescription(gramApplicationDeploymentType.getProjectAccount().getProjectAccountDescription());
                        }
                    }
                    if(gramApplicationDeploymentType.getQueue() != null){
                        applicationDescriptor.setQueueName(gramApplicationDeploymentType.getQueue().getQueueName());
                    }
                    applicationDescriptor.setProcessorsPerNode(gramApplicationDeploymentType.getProcessorsPerNode());
                }
            } else if (applicationDescription.getType() instanceof Ec2ApplicationDeploymentType) {
                applicationDescriptor.setApplicationDescType(ApplicationDescriptorTypes.EC2_APP_DEP_DESC_TYPE);
            }
        }

        return applicationDescriptor;
    }

}
