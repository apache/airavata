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

package org.apache.airavata.rest;


import org.apache.airavata.rest.client.*;

import java.net.URI;
import java.net.URISyntaxException;

public class Test {
    public static void main(String[] args) {
//        configurationResourceClientTest();
//        hostDescriptorClientTest();
//        serviceDescriptorClientTest();
//          appDescriptorClientTest();
//        projectRegistryClientTest();
//        experimentRegistryClient();
//        userWFClientTest();
//        publishWFClientTest();
//        provenanceClientTest();
    }


    public static void configurationResourceClientTest(){
        //configuration resource test
        ConfigurationResourceClient configurationResourceClient = new ConfigurationResourceClient("admin",
                "http://localhost:9080/airavata-services/", new PasswordCallbackImpl("admin", "admin"));

//        System.out.println("###############getConfiguration###############");
//        Object configuration = configurationResourceClient.getConfiguration("gfac.url");
//        System.out.println(configuration.toString());


//        System.out.println("###############getConfiguration###############");
//        Object configuration = configurationResourceClient.getConfiguration("key3");
//        System.out.println(configuration.toString());
//
//        System.out.println("###############getConfigurationList###############");
        try {
            configurationResourceClient.addWFInterpreterURI(new URI("http://192.168.17.1:8080/axis2/services/WorkflowInterpretor2"));
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
//        List<Object> configurationList = configurationResourceClient.getConfigurationList("testKey1");
//        for(Object object : configurationList){
//            System.out.println(object.toString());
//        }
//
//        System.out.println("###############setConfiguration###############");
//        configurationResourceClient.setConfiguration("testKey1", "testVal1", "2012-11-12 00:12:31");
//
//        System.out.println("###############addConfiguration###############");
//        configurationResourceClient.addConfiguration("testKey1", "testVal2", "2012-11-12 05:12:31");

//        System.out.println("###############remove all configuration ###############");
//        configurationResourceClient.removeAllConfiguration("testKey1");
//
//        System.out.println("###############remove configuration ###############");
//        configurationResourceClient.setConfiguration("testKey2", "testVal2", "2012-11-12 00:12:31");
//        configurationResourceClient.removeAllConfiguration("testKey2");
//
//        System.out.println("###############get GFAC URI ###############");
//        configurationResourceClient.addGFacURI("http://192.168.17.1:8080/axis2/services/GFacService2");
//        List<URI> gFacURIs = configurationResourceClient.getGFacURIs();
//        for (URI uri : gFacURIs){
//            System.out.println(uri.toString());
//        }

//        System.out.println("###############get WF interpreter URIs ###############");
//        List<URI> workflowInterpreterURIs = configurationResourceClient.getWorkflowInterpreterURIs();
//        for (URI uri : workflowInterpreterURIs){
//            System.out.println(uri.toString());
//        }
//
//        System.out.println("###############get eventing URI ###############");
//        URI eventingURI = configurationResourceClient.getEventingURI();
//        System.out.println(eventingURI.toString());
//
//        System.out.println("###############get message Box URI ###############");
//        URI mesgBoxUri = configurationResourceClient.getMsgBoxURI();
//        System.out.println(mesgBoxUri.toString());
//
//        System.out.println("###############Set eventing URI ###############");
//        configurationResourceClient.setEventingURI("http://192.168.17.1:8080/axis2/services/EventingService2");
//
//        System.out.println("###############Set MSGBox URI ###############");
//        configurationResourceClient.setEventingURI("http://192.168.17.1:8080/axis2/services/MsgBoxService2");
//
//        System.out.println("###############Add GFAC URI by date ###############");
//        configurationResourceClient.addGFacURIByDate("http://192.168.17.1:8080/axis2/services/GFacService3", "2012-11-12 00:12:27");
//
//        System.out.println("###############Add WF interpreter URI by date ###############");
//        configurationResourceClient.addWorkflowInterpreterURI("http://192.168.17.1:8080/axis2/services/WorkflowInterpretor3", "2012-11-12 00:12:27");

//        System.out.println("###############Set eventing URI by date ###############");
//        configurationResourceClient.setEventingURIByDate("http://192.168.17.1:8080/axis2/services/EventingService3", "2012-11-12 00:12:27");
//
//        System.out.println("###############Set MsgBox URI by date ###############");
//        configurationResourceClient.setMessageBoxURIByDate("http://192.168.17.1:8080/axis2/services/MsgBoxService3", "2012-11-12 00:12:27");

//        System.out.println("############### Remove GFac URI ###############");
//        configurationResourceClient.removeGFacURI("http://192.168.17.1:8080/axis2/services/GFacService3");
//
//        System.out.println("############### Remove all GFac URI ###############");
//        configurationResourceClient.removeAllGFacURI();
//
//        System.out.println("############### Remove removeWorkflowInterpreter URI ###############");
//        configurationResourceClient.removeWorkflowInterpreterURI("http://192.168.17.1:8080/axis2/services/WorkflowInterpretor3");

//        System.out.println("############### Remove removeAllWorkflowInterpreterURI ###############");
//        configurationResourceClient.removeAllWorkflowInterpreterURI();
//
//        System.out.println("############### Remove eventing URI ###############");
//        configurationResourceClient.unsetEventingURI();
//
//        System.out.println("############### unsetMessageBoxURI ###############");
//        configurationResourceClient.unsetMessageBoxURI();
    }

    public static void hostDescriptorClientTest(){
//        DescriptorResourceClient descriptorResourceClient = new DescriptorResourceClient();

//        boolean localHost = descriptorResourceClient.isHostDescriptorExists("LocalHost");
//        System.out.println(localHost);

//        HostDescription descriptor = new HostDescription(GlobusHostType.type);
//        descriptor.getType().setHostName("testHost");
//        descriptor.getType().setHostAddress("testHostAddress2");
//        descriptorResourceClient.addHostDescriptor(descriptor);

//        HostDescription localHost = descriptorResourceClient.getHostDescriptor("purdue.teragrid.org");
//        List<HostDescription> hostDescriptors = descriptorResourceClient.getHostDescriptors();
//        for(HostDescription hostDescription : hostDescriptors){
//            System.out.println(hostDescription.getType().getHostName());
//            System.out.println(hostDescription.getType().getHostAddress());
//        }
//
//        List<String> hostDescriptorNames = descriptorResourceClient.getHostDescriptorNames();
//        for (String hostName : hostDescriptorNames){
//            System.out.println(hostName);
//        }

//        descriptorResourceClient.removeHostDescriptor("testHost");

//        System.out.println(localHost.getType().getHostName());
//        System.out.println(localHost.getType().getHostAddress());

    }

    public static void serviceDescriptorClientTest (){

        DescriptorResourceClient descriptorResourceClient = new DescriptorResourceClient("admin",
                "http://localhost:9080/airavata-services/api", new PasswordCallbackImpl("admin", "admin"));
        //service descriptor exists
        boolean exists = descriptorResourceClient.isServiceDescriptorExists("echo");
        System.out.println(exists);

        //service descriptor save
//        ServiceDescription serviceDescription = new ServiceDescription();
//        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
//        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
//        serviceDescription.getType().setName("testServiceDesc");
//        serviceDescription.getType().setDescription("testDescription");
//        InputParameterType parameter = InputParameterType.Factory.newInstance();
//        parameter.setParameterName("input1");
//        parameter.setParameterDescription("testDesc");
//        ParameterType parameterType = parameter.addNewParameterType();
//        parameterType.setType(DataType.STRING);
//        parameterType.setName("testParamtype");
//        inputParameters.add(parameter);
//
//        OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
//        outputParameter.setParameterName("output1");
//        outputParameter.setParameterDescription("testDesc");
//        ParameterType outputParaType = outputParameter.addNewParameterType();
//        outputParaType.setType(DataType.STRING);
//        outputParaType.setName("testParamtype");
//        outputParameters.add(outputParameter);
//
//        serviceDescription.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[]{}));
//        serviceDescription.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[]{}));
//
//        descriptorResourceClient.saveServiceDescriptor(serviceDescription);

        // Service descriptor update
//        ServiceDescription testServiceDesc = descriptorResourceClient.getServiceDescriptor("testServiceDesc");
//        testServiceDesc.getType().setDescription("testDescription2");
//        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
//        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
//        InputParameterType parameter = InputParameterType.Factory.newInstance();
//        parameter.setParameterName("input2");
//        parameter.setParameterDescription("testDesc2");
//        ParameterType parameterType = parameter.addNewParameterType();
//        parameterType.setType(DataType.STRING);
//        parameterType.setName("testParamtype2");
//        inputParameters.add(parameter);
//
//        OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
//        outputParameter.setParameterName("output2");
//        outputParameter.setParameterDescription("testDesc2");
//        ParameterType outputParaType = outputParameter.addNewParameterType();
//        outputParaType.setType(DataType.STRING);
//        outputParaType.setName("testParamtype2");
//        outputParameters.add(outputParameter);
//
//        testServiceDesc.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[]{}));
//        testServiceDesc.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[]{}));
//
//        descriptorResourceClient.updateServiceDescriptor(testServiceDesc);

         //getServiceDescriptor
//        ServiceDescription testServiceDesc = descriptorResourceClient.getServiceDescriptor("testServiceDesc");
//        System.out.println(testServiceDesc.getType().getName());
//        System.out.println(testServiceDesc.getType().getDescription());

        //removeServiceDescriptor
//        descriptorResourceClient.removeServiceDescriptor("testServiceDesc");

        //getServiceDescriptors
//        List<ServiceDescription> serviceDescriptors = descriptorResourceClient.getServiceDescriptors();
//        for (ServiceDescription serviceDescription : serviceDescriptors){
//            System.out.println(serviceDescription.getType().getName());
//            System.out.println(serviceDescription.getType().getDescription());
//        }
    }

    public static void appDescriptorClientTest (){
//        DescriptorResourceClient descriptorResourceClient = new DescriptorResourceClient();

        //isApplicationDescriptorExist
//        boolean descriptorExist = descriptorResourceClient.isApplicationDescriptorExist("echo", "LocalHost", "LocalHost_application");
//        System.out.println(descriptorExist);

        // addApplicationDescriptor
//        ServiceDescription serviceDescription = new ServiceDescription();
//        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
//        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
//        serviceDescription.getType().setName("testServiceDesc");
//        serviceDescription.getType().setDescription("testDescription");
//        InputParameterType parameter = InputParameterType.Factory.newInstance();
//        parameter.setParameterName("input1");
//        parameter.setParameterDescription("testDesc");
//        ParameterType parameterType = parameter.addNewParameterType();
//        parameterType.setType(DataType.STRING);
//        parameterType.setName("testParamtype");
//        inputParameters.add(parameter);
//
//        OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
//        outputParameter.setParameterName("output1");
//        outputParameter.setParameterDescription("testDesc");
//        ParameterType outputParaType = outputParameter.addNewParameterType();
//        outputParaType.setType(DataType.STRING);
//        outputParaType.setName("testParamtype");
//        outputParameters.add(outputParameter);
//
//        serviceDescription.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[]{}));
//        serviceDescription.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[]{}));
//
//        HostDescription hostDescription = new HostDescription(GlobusHostType.type);
//        hostDescription.getType().setHostName("testHost");
//        hostDescription.getType().setHostAddress("testHostAddress");
//        descriptorResourceClient.addHostDescriptor(hostDescription);
//
//        ApplicationDeploymentDescription applicationDeploymentDescription = new ApplicationDeploymentDescription(ApplicationDeploymentDescriptionType.type);
//        ApplicationDeploymentDescriptionType.ApplicationName applicationName = applicationDeploymentDescription.getType().addNewApplicationName();
//        applicationName.setStringValue("testApplication");
//        applicationDeploymentDescription.getType().setApplicationName(applicationName);
//        applicationDeploymentDescription.getType().setInputDataDirectory("/bin");
//        applicationDeploymentDescription.getType().setExecutableLocation("/bin/echo");
//        applicationDeploymentDescription.getType().setOutputDataDirectory("/tmp");
//
//        descriptorResourceClient.addApplicationDescriptor(serviceDescription, hostDescription, applicationDeploymentDescription);

        //addApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor)
//        ApplicationDeploymentDescription applicationDeploymentDescription = new ApplicationDeploymentDescription(ApplicationDeploymentDescriptionType.type);
//        ApplicationDeploymentDescriptionType.ApplicationName applicationName = applicationDeploymentDescription.getType().addNewApplicationName();
//        applicationName.setStringValue("testApplication2");
//        applicationDeploymentDescription.getType().setApplicationName(applicationName);
//        applicationDeploymentDescription.getType().setInputDataDirectory("/bin");
//        applicationDeploymentDescription.getType().setExecutableLocation("/bin/echo");
//        applicationDeploymentDescription.getType().setOutputDataDirectory("/tmp");
//        descriptorResourceClient.addApplicationDescriptor("testServiceDesc", "testHost", applicationDeploymentDescription);

        //udpateApplicationDescriptor
//        ApplicationDeploymentDescription applicationDescriptor = descriptorResourceClient.getApplicationDescriptor("testServiceDesc", "testHost", "testApplication2");
//        applicationDescriptor.getType().setInputDataDirectory("/bin1");
//        applicationDescriptor.getType().setExecutableLocation("/bin/echo1");
//        applicationDescriptor.getType().setOutputDataDirectory("/tmp1");
//        descriptorResourceClient.updateApplicationDescriptor("testServiceDesc", "testHost", applicationDescriptor);

        //getApplicationDescriptors(String serviceName, String hostname)
//        ApplicationDeploymentDescription applicationDescriptors = descriptorResourceClient.getApplicationDescriptors("testServiceDesc", "testHost");
//        System.out.println(applicationDescriptors.getType().getApplicationName().getStringValue());
//        System.out.println(applicationDescriptors.getType().getExecutableLocation());
//        System.out.println(applicationDescriptors.getType().getOutputDataDirectory());

        //getApplicationDescriptors(String serviceName)
//        Map<String,ApplicationDeploymentDescription> testServiceDesc = descriptorResourceClient.getApplicationDescriptors("testServiceDesc");
//        for (String host : testServiceDesc.keySet()){
//            System.out.println(host);
//            ApplicationDeploymentDescription applicationDeploymentDescription = testServiceDesc.get(host);
//            System.out.println(applicationDeploymentDescription.getType().getApplicationName().getStringValue());
//        }
         //getApplicationDescriptors()
//        Map<String[], ApplicationDeploymentDescription> applicationDescriptors = descriptorResourceClient.getApplicationDescriptors();
//        for (String[] desc : applicationDescriptors.keySet()){
//            System.out.println(desc[0]);
//            System.out.println(desc[1]);
//            ApplicationDeploymentDescription applicationDeploymentDescription = applicationDescriptors.get(desc);
//            System.out.println(applicationDeploymentDescription.getType().getApplicationName().getStringValue());
//        }

        //getApplicationDescriptorNames ()
//        List<String> applicationDescriptorNames = descriptorResourceClient.getApplicationDescriptorNames();
//        for (String appName : applicationDescriptorNames){
//            System.out.println(appName);
//        }
    }

    public static void projectRegistryClientTest(){
//        ProjectResourceClient projectResourceClient = new ProjectResourceClient();
        //isWorkspaceProjectExists
//        boolean projectExists = projectResourceClient.isWorkspaceProjectExists("default");
//        System.out.println(projectExists);
       //isWorkspaceProjectExists(String projectName, boolean createIfNotExists)
//        projectResourceClient.isWorkspaceProjectExists("testproject", true);
        // addWorkspaceProject
//        projectResourceClient.addWorkspaceProject("testproject2");

        //updateWorkspaceProject(String projectName)
//        projectResourceClient.updateWorkspaceProject("testproject");

        // deleteWorkspaceProject
//        projectResourceClient.deleteWorkspaceProject("testproject");

        //getWorkspaceProject
//        WorkspaceProject project = projectResourceClient.getWorkspaceProject("default");
//        System.out.println(project.getGateway().getGatewayName());

        //getWorkspaceProjects
//        List<WorkspaceProject> workspaceProjects = projectResourceClient.getWorkspaceProjects();
//        for (WorkspaceProject workspaceProject : workspaceProjects){
//            System.out.println(workspaceProject.getProjectName());
//        }

    }

    public static void experimentRegistryClient(){
//        ExperimentResourceClient experimentResourceClient = new ExperimentResourceClient();
        //add experiment
//        try {
//            AiravataExperiment experiment = new AiravataExperiment();
//            experiment.setExperimentId("testExperiment2");
//            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Date formattedDate = dateFormat.parse("2012-11-13 11:50:32");
//            experiment.setSubmittedDate(formattedDate);
//            experimentResourceClient.addExperiment("default", experiment);
//        } catch (ParseException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }

        //remove experiment
//        experimentResourceClient.removeExperiment("testExperiment");

//        List<AiravataExperiment> experiments = experimentResourceClient.getExperiments();
//        System.out.println(experiments.size());
        //getExperiments(Date from, Date to)
//        try{
//            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Date date1 = dateFormat.parse("2012-11-01 11:50:32");
//            Date date2 = dateFormat.parse("2012-11-15 11:50:32");
//            List<AiravataExperiment> experiments = experimentResourceClient.getExperiments(date1, date2);
//            System.out.println(experiments.size());
//        } catch (ParseException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }

        //getExperiments(String projectName, Date from, Date to)
//        try{
//            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Date date1 = dateFormat.parse("2012-11-01 11:50:32");
//            Date date2 = dateFormat.parse("2012-11-15 11:50:32");
//            List<AiravataExperiment> experiments = experimentResourceClient.getExperiments("default",date1, date2);
//            System.out.println(experiments.size());
//        } catch (ParseException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }

          //isExperimentExists(String experimentId)
//        boolean exists = experimentResourceClient.isExperimentExists("testExperiment");
//        System.out.println(exists);


        //isExperimentExists(String experimentId, boolean createIfNotPresent)
//        experimentResourceClient.isExperimentExists("testExp", true);


    }

    public static void userWFClientTest (){
//        UserWorkflowResourceClient userWorkflowResourceClient = new UserWorkflowResourceClient();
//        boolean exists = userWorkflowResourceClient.isWorkflowExists("Workflow1");
//        System.out.println(exists);
//         userWorkflowResourceClient.addWorkflow("workflow5", "testworlflowcontent");
//         userWorkflowResourceClient.updateWorkflow("worklfow5", "updatedtestworlflowcontent");
//        String workflow2 = userWorkflowResourceClient.getWorkflowGraphXML("workflow2");
//        System.out.println(workflow2);
//        Map<String, String> workflows = userWorkflowResourceClient.getWorkflows();
//        System.out.println(workflows.size());
//        userWorkflowResourceClient.removeWorkflow("workflow5");
    }

    public static void publishWFClientTest () {
//        PublishedWorkflowResourceClient publishedWorkflowResourceClient = new PublishedWorkflowResourceClient();
//        boolean exists = publishedWorkflowResourceClient.isPublishedWorkflowExists("Workflow2");
//        System.out.println(exists);

//        publishedWorkflowResourceClient.publishWorkflow("workflow3", "publishedWF3");
//        publishedWorkflowResourceClient.publishWorkflow("workflow4");
//        String workflow1 = publishedWorkflowResourceClient.getPublishedWorkflowGraphXML("Workflow2");
//        System.out.println(workflow1);
//        List<String> publishedWorkflowNames = publishedWorkflowResourceClient.getPublishedWorkflowNames();
//        System.out.println(publishedWorkflowNames.size());

//        publishedWorkflowResourceClient.removePublishedWorkflow("workflow4");

    }

    public static void provenanceClientTest()  {
//        ProvenanceResourceClient provenanceResourceClient = new ProvenanceResourceClient();
//        provenanceResourceClient.updateExperimentExecutionUser("eb9e67cf-6fe3-46f1-b50b-7b42936d347d", "aaa");
//        String experimentExecutionUser = provenanceResourceClient.getExperimentExecutionUser("eb9e67cf-6fe3-46f1-b50b-7b42936d347d");
//        System.out.println(experimentExecutionUser);
//        boolean nameExist = provenanceResourceClient.isExperimentNameExist("exp1");
//        System.out.println(nameExist);
//        String experimentName = provenanceResourceClient.getExperimentName("eb9e67cf-6fe3-46f1-b50b-7b42936d347d");
//        System.out.println(experimentName);
//        String workflowExecutionTemplateName = provenanceResourceClient.getWorkflowExecutionTemplateName("e00ddc5e-f8d5-4492-9eb2-10372efb103c");
//        System.out.println(workflowExecutionTemplateName);
//        provenanceResourceClient.setWorkflowInstanceTemplateName("eb9e67cf-6fe3-46f1-b50b-7b42936d347d", "wftemplate2");
//        List<WorkflowInstance> experimentWorkflowInstances = provenanceResourceClient.getExperimentWorkflowInstances("e00ddc5e-f8d5-4492-9eb2-10372efb103c");
//        System.out.println(experimentWorkflowInstances.size());
//        System.out.println(provenanceResourceClient.isWorkflowInstanceExists("e00ddc5e-f8d5-4492-9eb2-10372efb103c"));
//        provenanceResourceClient.isWorkflowInstanceExists("testInstance", true);

//        try {
//            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Date date = dateFormat.parse("2012-11-01 11:50:32");
//            WorkflowInstanceStatus workflowInstanceStatus = new WorkflowInstanceStatus(new WorkflowInstance("testInstance", "testInstance"), WorkflowInstanceStatus.ExecutionStatus.FINISHED, date);
//            provenanceResourceClient.updateWorkflowInstanceStatus(workflowInstanceStatus);
//        } catch (ParseException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }

//        System.out.println(provenanceResourceClient.getWorkflowInstanceStatus("testInstance").getExecutionStatus().name());
//        WorkflowInstanceNode workflowInstanceNode = new WorkflowInstanceNode(provenanceResourceClient.getWorkflowInstanceData("TempConvertSoap_FahrenheitToCelsius").getWorkflowInstance(), "TempConvertSoap_FahrenheitToCelsius");
//        provenanceResourceClient.updateWorkflowNodeInput(workflowInstanceNode, "testInput2");

//        WorkflowInstanceNode workflowInstanceNode = new WorkflowInstanceNode(provenanceResourceClient.getWorkflowInstanceData("TempConvertSoap_FahrenheitToCelsius").getWorkflowInstance(), "TempConvertSoap_FahrenheitToCelsius");
//        provenanceResourceClient.updateWorkflowNodeOutput(workflowInstanceNode, "testOutput");

//        ExperimentData experiment = provenanceResourceClient.getExperiment("ff7338c9-f9ad-4d86-b486-1e8e9c3a9cc4");
//        String experimentName = experiment.getExperimentName();
//        System.out.println(experimentName);

//        ExperimentData experiment = provenanceResourceClient.getExperimentMetaInformation("ff7338c9-f9ad-4d86-b486-1e8e9c3a9cc4");
//        String experimentName = experiment.getExperimentName();
//        System.out.println(experimentName);

//        List<ExperimentData> admin = provenanceResourceClient.getAllExperimentMetaInformation("admin");
//        System.out.println(admin.size());

//        List<ExperimentData> experimentDataList = provenanceResourceClient.searchExperiments("admin", "exp");
//        System.out.println(experimentDataList.size());

//        List<String> admin = provenanceResourceClient.getExperimentIdByUser("admin");
//        for (String exp : admin){
//            System.out.println(exp);
//        }
//        List<ExperimentData> experimentByUser = provenanceResourceClient.getExperimentByUser("admin");
//        System.out.println(experimentByUser.size());

//        WorkflowInstanceNode workflowInstanceNode = new WorkflowInstanceNode(provenanceResourceClient.getWorkflowInstanceData("TempConvertSoap_FahrenheitToCelsius").getWorkflowInstance(), "TempConvertSoap_FahrenheitToCelsius");
//        WorkflowInstanceNodeStatus workflowInstanceNodeStatus = new WorkflowInstanceNodeStatus(workflowInstanceNode, WorkflowInstanceStatus.ExecutionStatus.STARTED);
//        provenanceResourceClient.updateWorkflowNodeStatus(workflowInstanceNodeStatus);

//        WorkflowInstanceNode workflowInstanceNode = new WorkflowInstanceNode(provenanceResourceClient.getWorkflowInstanceData("TempConvertSoap_FahrenheitToCelsius").getWorkflowInstance(), "TempConvertSoap_FahrenheitToCelsius");
//        provenanceResourceClient.updateWorkflowNodeStatus(workflowInstanceNode, WorkflowInstanceStatus.ExecutionStatus.FINISHED);

//        WorkflowInstanceNode workflowInstanceNode = new WorkflowInstanceNode(provenanceResourceClient.getWorkflowInstanceData("TempConvertSoap_FahrenheitToCelsius").getWorkflowInstance(), "TempConvertSoap_FahrenheitToCelsius");
//        WorkflowInstanceNodeStatus workflowNodeStatus = provenanceResourceClient.getWorkflowNodeStatus(workflowInstanceNode);
//        System.out.println(workflowNodeStatus.getExecutionStatus().name());

//        WorkflowInstanceNode workflowInstanceNode = new WorkflowInstanceNode(provenanceResourceClient.getWorkflowInstanceData("TempConvertSoap_FahrenheitToCelsius").getWorkflowInstance(), "TempConvertSoap_FahrenheitToCelsius");
//        Date workflowNodeStartTime = provenanceResourceClient.getWorkflowNodeStartTime(workflowInstanceNode);
//        System.out.println(workflowNodeStartTime.toString());

//        WorkflowInstance tempConvertSoap_fahrenheitToCelsius = provenanceResourceClient.getWorkflowInstanceData("TempConvertSoap_FahrenheitToCelsius").getWorkflowInstance();
//        Date workflowStartTime = provenanceResourceClient.getWorkflowStartTime(tempConvertSoap_fahrenheitToCelsius);
//        System.out.println(workflowStartTime.toString());

//        WorkflowNodeGramData workflowNodeGramData = new WorkflowNodeGramData("TempConvertSoap_FahrenheitToCelsius", "TempConvertSoap_FahrenheitToCelsius", "rsl", "invokedHost", "jobID");
//        provenanceResourceClient.updateWorkflowNodeGramData(workflowNodeGramData);

//        WorkflowInstanceData instanceData = provenanceResourceClient.getWorkflowInstanceData("TempConvertSoap_FahrenheitToCelsius");
//        System.out.println(instanceData.getWorkflowInstance().getExperimentId());

//        System.out.println(provenanceResourceClient.isWorkflowInstanceNodePresent("TempConvertSoap_FahrenheitToCelsius", "TempConvertSoap_FahrenheitToCelsius"));

//        provenanceResourceClient.isWorkflowInstanceNodePresent("TempConvertSoap_FahrenheitToCelsius", "TempConvertSoap_FahrenheitToCelsius1", true);

//        provenanceResourceClient.addWorkflowInstance("testInstance", "testWF", "testWotrfklow");
//        WorkflowInstanceNode workflowInstanceNode = new WorkflowInstanceNode(provenanceResourceClient.getWorkflowInstanceData("TempConvertSoap_FahrenheitToCelsius").getWorkflowInstance(), "TempConvertSoap_FahrenheitToCelsius");
//        WorkflowNodeType workflowNodeType = new WorkflowNodeType();
//        workflowNodeType.setNodeType(WorkflowNodeType.WorkflowNode.SERVICENODE);
//        provenanceResourceClient.updateWorkflowNodeType(workflowInstanceNode, workflowNodeType);

//         provenanceResourceClient.addWorkflowInstanceNode("TempConvertSoap_FahrenheitToCelsius", "testNode");
    }


}
