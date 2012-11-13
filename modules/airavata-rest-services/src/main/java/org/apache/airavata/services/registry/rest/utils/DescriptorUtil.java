package org.apache.airavata.services.registry.rest.utils;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.schemas.gfac.*;
import org.apache.airavata.schemas.gfac.impl.GramApplicationDeploymentTypeImpl;
import org.apache.airavata.services.registry.rest.resourcemappings.ApplicationDescriptor;
import org.apache.airavata.services.registry.rest.resourcemappings.HostDescriptor;
import org.apache.airavata.services.registry.rest.resourcemappings.ServiceDescriptor;
import org.apache.airavata.services.registry.rest.resourcemappings.ServiceParameters;

import java.util.ArrayList;
import java.util.List;

public class DescriptorUtil {

    public static HostDescription createHostDescription(String hostName, String hostAddress,
                                                        String hostEndpoint, String gatekeeperEndpoint) {
        HostDescription host = new HostDescription();
        if("".equalsIgnoreCase(gatekeeperEndpoint) || "".equalsIgnoreCase(hostEndpoint)) {
            host.getType().changeType(GlobusHostType.type);
            host.getType().setHostName(hostName);
            host.getType().setHostAddress(hostAddress);
            ((GlobusHostType) host.getType()).
                    setGridFTPEndPointArray(new String[]{hostEndpoint});
            ((GlobusHostType) host.getType()).
                    setGlobusGateKeeperEndPointArray(new String[]{gatekeeperEndpoint});
        } else {
            host.getType().setHostName(hostName);
            host.getType().setHostAddress(hostAddress);
        }
        return host;
    }

    public static ApplicationDeploymentDescription registerApplication(String appName, String exeuctableLocation, String scratchWorkingDirectory, String hostName,
                                                                       String projAccNumber, String queueName, String cpuCount, String nodeCount, String maxMemory) throws Exception {
        // Create Application Description
        ApplicationDeploymentDescription appDesc = new ApplicationDeploymentDescription(GramApplicationDeploymentType.type);
        GramApplicationDeploymentType app = (GramApplicationDeploymentType) appDesc.getType();
        app.setCpuCount(Integer.parseInt(cpuCount));
        app.setNodeCount(Integer.parseInt(nodeCount));
        ApplicationDeploymentDescriptionType.ApplicationName name = appDesc.getType().addNewApplicationName();
        name.setStringValue(appName);
        app.setExecutableLocation(exeuctableLocation);
        app.setScratchWorkingDirectory(scratchWorkingDirectory);
        ProjectAccountType projectAccountType = ((GramApplicationDeploymentType) appDesc.getType()).addNewProjectAccount();
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
        List<String> globusGateKeeperEndPoint  = new ArrayList<String>();
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
                globusGateKeeperEndPoint.add(globusGateKeeperEndPointArray[i]);
            }

            String[] gridFTPEndPointArray = globusHostType.getGridFTPEndPointArray();
            for (int i = 0; i < gridFTPEndPointArray.length ; i++){
                gridFTPEndPoint.add(globusGateKeeperEndPointArray[i]);
            }

        }else if (hostDescriptionType instanceof GsisshHostType){
            GsisshHostType gsisshHostType = (GsisshHostType) hostDescriptionType;
            hostType.add(HostTypes.GSISSH_HOST_TYPE);

            String[] gridFTPEndPointArray = gsisshHostType.getGridFTPEndPointArray();
            for (int i = 0; i < gridFTPEndPointArray.length ; i++){
                gridFTPEndPoint.add(gridFTPEndPointArray[i]);
            }
        }  else if (hostDescriptionType instanceof  Ec2HostType) {
            Ec2HostType ec2HostType = (Ec2HostType) hostDescriptionType;
            hostType.add(HostTypes.EC2_HOST_TYPE);

            String[] imageIDArray = ec2HostType.getImageIDArray();
            for (int i = 0; i < imageIDArray.length ; i++){
                imageID.add(imageIDArray[i]);
            }

            String[] instanceIDArray = ec2HostType.getInstanceIDArray();
            for (int i = 0; i < instanceIDArray.length ; i++){
                instanceID.add(instanceIDArray[i]);
            }
        } else {
            hostType.add(HostTypes.HOST_DESCRIPTION_TYPE);
        }
        hostDescriptor.setGlobusGateKeeperEndPoint(globusGateKeeperEndPoint);
        hostDescriptor.setGridFTPEndPoint(gridFTPEndPoint);
        hostDescriptor.setImageID(imageID);
        hostDescriptor.setInstanceID(instanceID);
        hostDescriptor.setHostType(hostType);
        return hostDescriptor;
    }

    public static HostDescription createHostDescription (HostDescriptor hostDescriptor){
        HostDescription hostDescription = new HostDescription();
        hostDescription.getType().setHostAddress(hostDescriptor.getHostAddress());
        hostDescription.getType().setHostName(hostDescriptor.getHostname());

        if (hostDescriptor.getHostType() != null && !hostDescriptor.getHostType().isEmpty()) {
            if (hostDescriptor.getHostType().equals(HostTypes.GLOBUS_HOST_TYPE)) {
                ((GlobusHostType) hostDescription.getType()).addGlobusGateKeeperEndPoint(hostDescriptor.getGlobusGateKeeperEndPoint().get(0));
                ((GlobusHostType) hostDescription.getType()).addGridFTPEndPoint(hostDescriptor.getGridFTPEndPoint().get(0));
            } else if (hostDescriptor.getHostType().equals(HostTypes.GSISSH_HOST_TYPE)) {
                ((GsisshHostType) hostDescription).addGridFTPEndPoint(hostDescriptor.getGridFTPEndPoint().get(0));
            } else if (hostDescriptor.getHostType().equals(HostTypes.EC2_HOST_TYPE)) {
                ((Ec2HostType) hostDescription).addImageID(hostDescriptor.getImageID().get(0));
                ((Ec2HostType) hostDescription).addInstanceID(hostDescriptor.getInstanceID().get(0));
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

    public static ApplicationDeploymentDescription createApplicationDescription(ApplicationDescriptor applicationDescriptor){
        ApplicationDeploymentDescription applicationDeploymentDescription = new ApplicationDeploymentDescription();
        ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
        name.setStringValue(applicationDescriptor.getName());
        applicationDeploymentDescription.getType().setApplicationName(name);
        applicationDeploymentDescription.getType().setExecutableLocation(applicationDescriptor.getExecutablePath());
        applicationDeploymentDescription.getType().setOutputDataDirectory(applicationDescriptor.getWorkingDir());

        //set advanced options according app desc type
        if(applicationDescriptor.getApplicationDescType() != null && !applicationDescriptor.getApplicationDescType().isEmpty()){
            if (applicationDescriptor.getApplicationDescType().equals(ApplicationDescriptorTypes.GRAM_APP_DEP_DESC_TYPE)){
                ApplicationDeploymentDescription appDesc = new ApplicationDeploymentDescription(GramApplicationDeploymentType.type);
                appDesc.getType().setApplicationName(name);
                appDesc.getType().setExecutableLocation(applicationDescriptor.getExecutablePath());
                appDesc.getType().setOutputDataDirectory(applicationDescriptor.getWorkingDir());
                GramApplicationDeploymentType app = (GramApplicationDeploymentType) appDesc.getType();
                app.setCpuCount(applicationDescriptor.getCpuCount());
                app.setJobType(JobTypeType.Enum.forString(applicationDescriptor.getJobType()));
                app.setMaxMemory(applicationDescriptor.getMaxMemory());
                app.setMinMemory(applicationDescriptor.getMinMemory());
                app.setMaxWallTime(applicationDescriptor.getMaxWallTime());
                app.setNodeCount(applicationDescriptor.getNodeCount());
                app.setProcessorsPerNode(applicationDescriptor.getProcessorsPerNode());
                return appDesc;
            } else if (applicationDescriptor.getApplicationDescType().equals(ApplicationDescriptorTypes.BATCH_APP_DEP_DESC_TYPE)){
                ApplicationDeploymentDescription appDesc = new ApplicationDeploymentDescription(BatchApplicationDeploymentDescriptionType.type);
                appDesc.getType().setApplicationName(name);
                appDesc.getType().setExecutableLocation(applicationDescriptor.getExecutablePath());
                appDesc.getType().setOutputDataDirectory(applicationDescriptor.getWorkingDir());
                BatchApplicationDeploymentDescriptionType applicationDeploymentType = (BatchApplicationDeploymentDescriptionType) appDesc.getType();
                applicationDeploymentType.setCpuCount(applicationDescriptor.getCpuCount());
                applicationDeploymentType.setJobType(JobTypeType.Enum.forString(applicationDescriptor.getJobType()));
                applicationDeploymentType.setMaxMemory(applicationDescriptor.getMaxMemory());
                applicationDeploymentType.setMinMemory(applicationDescriptor.getMinMemory());
                applicationDeploymentType.setMaxWallTime(applicationDescriptor.getMaxWallTime());
                applicationDeploymentType.setNodeCount(applicationDescriptor.getNodeCount());
                applicationDeploymentType.setProcessorsPerNode(applicationDescriptor.getProcessorsPerNode());
                return appDesc;
            }
        }
        return applicationDeploymentDescription;
    }

    public static ApplicationDescriptor createApplicationDescriptor (ApplicationDeploymentDescription applicationDeploymentDescription){
        ApplicationDescriptor applicationDescriptor = new ApplicationDescriptor();
        applicationDescriptor.setName(applicationDeploymentDescription.getType().getApplicationName().getStringValue());
        applicationDescriptor.setExecutablePath(applicationDeploymentDescription.getType().getExecutableLocation());
        applicationDescriptor.setWorkingDir(applicationDeploymentDescription.getType().getOutputDataDirectory());
        if(applicationDeploymentDescription.getType() != null){
            if(applicationDeploymentDescription.getType() instanceof GramApplicationDeploymentType){
                GramApplicationDeploymentType gramApplicationDeploymentType = (GramApplicationDeploymentType)applicationDeploymentDescription.getType();
                if(gramApplicationDeploymentType != null){
                    applicationDescriptor.setCpuCount(gramApplicationDeploymentType.getCpuCount());
                    applicationDescriptor.setNodeCount(gramApplicationDeploymentType.getNodeCount());
                    applicationDescriptor.setMaxMemory(gramApplicationDeploymentType.getMaxMemory());
                    applicationDescriptor.setMinMemory(gramApplicationDeploymentType.getMinMemory());
                    applicationDescriptor.setMaxWallTime(gramApplicationDeploymentType.getMaxWallTime());
                    if(gramApplicationDeploymentType.getQueue() != null){
                        applicationDescriptor.setQueueName(gramApplicationDeploymentType.getQueue().getQueueName());
                    }
                }
            } else if (applicationDeploymentDescription.getType() instanceof BatchApplicationDeploymentDescriptionType){
                BatchApplicationDeploymentDescriptionType batchApplicationDeploymentDescriptionType = (BatchApplicationDeploymentDescriptionType)applicationDeploymentDescription.getType();
                if (batchApplicationDeploymentDescriptionType != null){
                    applicationDescriptor.setCpuCount(batchApplicationDeploymentDescriptionType.getCpuCount());
                    applicationDescriptor.setNodeCount(batchApplicationDeploymentDescriptionType.getNodeCount());
                    applicationDescriptor.setMaxMemory(batchApplicationDeploymentDescriptionType.getMaxMemory());
                    applicationDescriptor.setMinMemory(batchApplicationDeploymentDescriptionType.getMinMemory());
                    applicationDescriptor.setMaxWallTime(batchApplicationDeploymentDescriptionType.getMaxWallTime());
                    if (batchApplicationDeploymentDescriptionType.getQueue() != null){
                        applicationDescriptor.setQueueName(batchApplicationDeploymentDescriptionType.getQueue().getQueueName());
                    }

                }

            }
        }

        return applicationDescriptor;
    }

}
