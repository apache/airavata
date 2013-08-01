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

package org.apache.airavata.registry.api.test;

import junit.framework.TestCase;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.registry.api.*;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.test.util.Initialize;
import org.apache.airavata.registry.api.workflow.*;
import org.apache.airavata.schemas.gfac.*;

import java.net.URI;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AiravataRegistryAPITest extends TestCase {
    private AiravataRegistry2 registry;
    private  Initialize initialize;
    @Override
    protected void setUp() throws Exception {
        initialize = new Initialize();
        initialize.initializeDB();
        registry = AiravataRegistryFactory.getRegistry(new Gateway("default"), new AiravataUser("admin"));

    }

    public void testAddConfiguration() throws Exception {
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Date currentTime = new Date(d.getTime());
        registry.addConfiguration("configkey", "configval", currentTime);

        assertTrue("configuration add successfully", ResourceUtils.isConfigurationExists("configkey", "configval"));
        Object config = registry.getConfiguration("configkey");
        ResourceUtils.removeConfiguration("configkey", "configval");

    }

    public void testGetConfigurationList() throws Exception {
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Date currentTime = new Date(d.getTime());
        registry.addConfiguration("configkey1", "configval1", currentTime);
        registry.addConfiguration("configkey1", "configval2", currentTime);

        List<Object> list = registry.getConfigurationList("configkey1");
        assertTrue("configurations retrieved successfully", list.size() == 2);

        ResourceUtils.removeConfiguration("configkey1");

    }

    public void testSetConfiguration() throws Exception {
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Date currentTime = new Date(d.getTime());
        registry.addConfiguration("configkey1", "configval1", currentTime);
        registry.setConfiguration("configkey1", "configval1", currentTime);
        registry.setConfiguration("configkey2", "configval2", currentTime);

        assertTrue("Configuration updated successfully", ResourceUtils.isConfigurationExist("configkey1"));
        assertTrue("Configuration updated successfully", ResourceUtils.isConfigurationExist("configkey2"));

        ResourceUtils.removeConfiguration("configkey1");
        ResourceUtils.removeConfiguration("configkey2");
    }

    public void testRemoveAllConfiguration() throws RegistryException {
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Date currentTime = new Date(d.getTime());
        registry.addConfiguration("configkey1", "configval1", currentTime);
        registry.addConfiguration("configkey1", "configval2", currentTime);

        registry.removeAllConfiguration("configkey1");

        assertFalse("configurations removed successfully", ResourceUtils.isConfigurationExist("configkey1"));
    }


    public void testRemoveConfiguration() throws RegistryException {
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Date currentTime = new Date(d.getTime());
        registry.addConfiguration("configkey1", "configval1", currentTime);

        registry.removeConfiguration("configkey1", "configval1");
        assertFalse("comnfiguration removed successfully", ResourceUtils.isConfigurationExists("configkey1", "configval1"));
    }


    public void testGetGFacURIs() throws RegistryException {
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Date currentTime = new Date(d.getTime());
        registry.addConfiguration("gfac.url", "http://192.168.17.1:8080/axis2/services/GFacService", currentTime);
        List<URI> gFacURIs = registry.getGFacURIs();
        assertTrue("gfac urls retrieved successfully", gFacURIs.size() == 1);
        ResourceUtils.removeConfiguration("gfac.url");
    }


    public void testGetWorkflowInterpreterURIs() throws RegistryException {
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Date currentTime = new Date(d.getTime());
        registry.addConfiguration("interpreter.url", "http://192.168.17.1:8080/axis2/services/WorkflowInterpretor", currentTime);
        List<URI> interpreterURIs = registry.getWorkflowInterpreterURIs();
        assertTrue("interpreter urls retrieved successfully", interpreterURIs.size() == 1);
        ResourceUtils.removeConfiguration("interpreter.url");
    }


    public void testGetEventingServiceURI() throws RegistryException {
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Date currentTime = new Date(d.getTime());
        registry.addConfiguration("eventing.url", "http://192.168.17.1:8080/axis2/services/EventingService", currentTime);
        URI eventingServiceURI = registry.getEventingServiceURI();
        assertNotNull("eventing url retrieved successfully", eventingServiceURI);
        ResourceUtils.removeConfiguration("eventing.url");
    }

    public void testGetMessageBoxURI() throws RegistryException {
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Date currentTime = new Date(d.getTime());
        registry.addConfiguration("messagebox.url", "http://192.168.17.1:8080/axis2/services/MsgBoxService", currentTime);
        URI messageBoxURI = registry.getMessageBoxURI();
        assertNotNull("message box url retrieved successfully", messageBoxURI);
        ResourceUtils.removeConfiguration("messagebox.url");
    }


    public void testAddGFacURI() throws RegistryException {
        URI uri = URI.create("http://192.168.17.1:8080/axis2/services/GFacService");
        registry.addGFacURI(uri);
        List<URI> gFacURIs = registry.getGFacURIs();
        assertTrue("gfac url add successfully", gFacURIs.size() == 1);
        registry.removeConfiguration("gfac.url", "http://192.168.17.1:8080/axis2/services/GFacService");
    }


    public void testAddWorkflowInterpreterURI() throws RegistryException {
        URI uri = URI.create("http://192.168.17.1:8080/axis2/services/WorkflowInterpretor");
        registry.addWorkflowInterpreterURI(uri);
        List<URI> interpreterURIs = registry.getWorkflowInterpreterURIs();
        assertTrue("interpreter url add successfully", interpreterURIs.size() == 1);
        registry.removeConfiguration("interpreter.url", "http://192.168.17.1:8080/axis2/services/WorkflowInterpretor");
    }


    public void testSetEventingURI() throws RegistryException {
        URI uri = URI.create("http://192.168.17.1:8080/axis2/services/EventingService");
        registry.setEventingURI(uri);
        URI eventingServiceURI = registry.getEventingServiceURI();
        assertNotNull("eventing url added successfully", eventingServiceURI);
        registry.removeConfiguration("eventing.url", "http://192.168.17.1:8080/axis2/services/EventingService");
    }

    public void testSetMessageBoxURI() throws RegistryException {
        URI uri = URI.create("http://192.168.17.1:8080/axis2/services/MsgBoxService");
        registry.setMessageBoxURI(uri);
        URI messageBoxURI = registry.getMessageBoxURI();
        assertNotNull("message box url added successfully", messageBoxURI);
        registry.removeConfiguration("messagebox.url", "http://192.168.17.1:8080/axis2/services/MsgBoxService");
    }


    public void testAddGFacURIWithExpireDate() throws RegistryException {
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Date currentTime = new Date(d.getTime());
        URI uri = URI.create("http://192.168.17.1:8080/axis2/services/GFacService");
        registry.addGFacURI(uri, currentTime);
        List<URI> gFacURIs = registry.getGFacURIs();
        assertTrue("gfac url add successfully", gFacURIs.size() == 1);
        registry.removeConfiguration("gfac.url", "http://192.168.17.1:8080/axis2/services/GFacService");
    }


    public void testAddWorkflowInterpreterURIWithExpireDate() throws RegistryException {
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Date currentTime = new Date(d.getTime());
        URI uri = URI.create("http://192.168.17.1:8080/axis2/services/WorkflowInterpretor");
        registry.addWorkflowInterpreterURI(uri, currentTime);
        List<URI> interpreterURIs = registry.getWorkflowInterpreterURIs();
        assertTrue("interpreter url add successfully", interpreterURIs.size() == 1);
        registry.removeConfiguration("interpreter.url", "http://192.168.17.1:8080/axis2/services/WorkflowInterpretor");

    }


    public void testSetEventingURIWithExpireDate() throws RegistryException {
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Date currentTime = new Date(d.getTime());
        URI uri = URI.create("http://192.168.17.1:8080/axis2/services/EventingService");
        registry.setEventingURI(uri, currentTime);
        URI eventingServiceURI = registry.getEventingServiceURI();
        assertNotNull("eventing url added successfully", eventingServiceURI);
        registry.removeConfiguration("eventing.url", "http://192.168.17.1:8080/axis2/services/EventingService");
    }


    public void testSetMessageBoxURIWithExpireDate() throws RegistryException {
        Calendar calender = Calendar.getInstance();
        java.util.Date d = calender.getTime();
        Date currentTime = new Date(d.getTime());
        URI uri = URI.create("http://192.168.17.1:8080/axis2/services/MsgBoxService");
        registry.setMessageBoxURI(uri, currentTime);
        URI messageBoxURI = registry.getMessageBoxURI();
        assertNotNull("message box url added successfully", messageBoxURI);
        registry.removeConfiguration("messagebox.url", "http://192.168.17.1:8080/axis2/services/MsgBoxService");
    }


    public void testRemoveGFacURI() throws RegistryException {
        URI uri = URI.create("http://192.168.17.1:8080/axis2/services/GFacService");
        registry.addGFacURI(uri);
        registry.removeGFacURI(uri);
        assertFalse("Gfac uri removed successfully", ResourceUtils.isConfigurationExist("gfac.url"));
    }


    public void testRemoveAllGFacURI() throws RegistryException {
        URI uri = URI.create("http://192.168.17.1:8080/axis2/services/GFacService");
        registry.addGFacURI(uri);
        registry.removeAllGFacURI();
        assertFalse("Gfac uri removed successfully", ResourceUtils.isConfigurationExist("gfac.url"));
    }

    public void testRemoveWorkflowInterpreterURI() throws RegistryException {
        URI uri = URI.create("http://192.168.17.1:8080/axis2/services/WorkflowInterpretor");
        registry.addWorkflowInterpreterURI(uri);
        registry.removeWorkflowInterpreterURI(uri);
        assertFalse("workflow interpreter uri removed successfully", ResourceUtils.isConfigurationExist("interpreter.url"));
    }

    public void testRemoveAllWorkflowInterpreterURI() throws RegistryException {
        URI uri = URI.create("http://192.168.17.1:8080/axis2/services/WorkflowInterpretor");
        registry.addWorkflowInterpreterURI(uri);
        registry.removeAllWorkflowInterpreterURI();
        assertFalse("workflow interpreter uri removed successfully", ResourceUtils.isConfigurationExist("interpreter.url"));
    }

    public void testUnsetEventingURI() throws RegistryException {
        URI uri = URI.create("http://192.168.17.1:8080/axis2/services/EventingService");
        registry.setEventingURI(uri);
        registry.unsetEventingURI();
        assertNotNull("eventing url removed successfully", ResourceUtils.isConfigurationExist("eventing.url"));
    }

    public void testUnsetMessageBoxURI() throws RegistryException {
        URI uri = URI.create("http://192.168.17.1:8080/axis2/services/MsgBoxService");
        registry.setMessageBoxURI(uri);
        registry.unsetMessageBoxURI();
        assertNotNull("message url removed successfully", ResourceUtils.isConfigurationExist("messagebox.url"));
    }

    public void testIsHostDescriptorExists() throws Exception {
        HostDescription descriptor = new HostDescription(GlobusHostType.type);
        descriptor.getType().setHostName("testHost");
        descriptor.getType().setHostAddress("testHostAddress");
        registry.addHostDescriptor(descriptor);
        assertTrue("Host added successfully", registry.isHostDescriptorExists("testHost"));
        registry.removeHostDescriptor("testHost");
    }


    public void testAddHostDescriptor() throws Exception {
        HostDescription descriptor = new HostDescription(GlobusHostType.type);
        descriptor.getType().setHostName("testHost");
        descriptor.getType().setHostAddress("testHostAddress");
        registry.addHostDescriptor(descriptor);
        assertTrue("Host added successfully", registry.isHostDescriptorExists("testHost"));
        registry.removeHostDescriptor("testHost");
    }


    public void testUpdateHostDescriptor() throws Exception {
        HostDescription descriptor = new HostDescription(GlobusHostType.type);
        descriptor.getType().setHostName("testHost");
        descriptor.getType().setHostAddress("testHostAddress");
        registry.addHostDescriptor(descriptor);
        HostDescription hostDescriptor = registry.getHostDescriptor("testHost");
        hostDescriptor.getType().setHostAddress("testHostAddress2");
        registry.updateHostDescriptor(hostDescriptor);
        HostDescription testHost = registry.getHostDescriptor("testHost");
        assertTrue("host updated successfully", testHost.getType().getHostAddress().equals("testHostAddress2"));
        registry.removeHostDescriptor("testHost");
    }


    public void testGetHostDescriptor() throws Exception {
        HostDescription descriptor = new HostDescription(GlobusHostType.type);
        descriptor.getType().setHostName("testHost");
        descriptor.getType().setHostAddress("testHostAddress");
        registry.addHostDescriptor(descriptor);
        HostDescription hostDescriptor = registry.getHostDescriptor("testHost");
        assertNotNull("host descriptor retrieved successfully", hostDescriptor);
        registry.removeHostDescriptor("testHost");
    }

    public void testRemoveHostDescriptor() throws Exception {
        HostDescription descriptor = new HostDescription(GlobusHostType.type);
        descriptor.getType().setHostName("testHost");
        descriptor.getType().setHostAddress("testHostAddress");
        registry.addHostDescriptor(descriptor);
        registry.removeHostDescriptor("testHost");
        assertFalse("host descriptor removed successfully", registry.isHostDescriptorExists("testHost"));
    }

    public void testGetHostDescriptors() throws Exception {
        HostDescription descriptor = new HostDescription(GlobusHostType.type);
        descriptor.getType().setHostName("testHost");
        descriptor.getType().setHostAddress("testHostAddress");
        registry.addHostDescriptor(descriptor);
        List<HostDescription> hostDescriptors = registry.getHostDescriptors();
        assertTrue("host descriptors retrieved successfully", hostDescriptors.size() == 1);
        registry.removeHostDescriptor("testHost");
    }


    public void testIsServiceDescriptorExists() throws Exception {
        ServiceDescription serviceDescription = new ServiceDescription();
        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        serviceDescription.getType().setName("testServiceDesc");
        serviceDescription.getType().setDescription("testDescription");
        InputParameterType parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("input1");
        parameter.setParameterDescription("testDesc");
        ParameterType parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.Enum.forString("testType"));
        parameterType.setName("testParamtype");
        inputParameters.add(parameter);

        OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("input1");
        outputParameter.setParameterDescription("testDesc");
        ParameterType outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.Enum.forString("testType"));
        outputParaType.setName("testParamtype");
        outputParameters.add(outputParameter);

        serviceDescription.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[]{}));
        serviceDescription.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[]{}));

        registry.addServiceDescriptor(serviceDescription);
        assertTrue("Service desc exists", registry.isServiceDescriptorExists("testServiceDesc"));

        registry.removeServiceDescriptor("testServiceDesc");
    }


    public void testAddServiceDescriptor() throws Exception {
        ServiceDescription serviceDescription = new ServiceDescription();
        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        serviceDescription.getType().setName("testServiceDesc");
        serviceDescription.getType().setDescription("testDescription");
        InputParameterType parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("input1");
        parameter.setParameterDescription("testDesc");
        ParameterType parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.Enum.forString("testType"));
        parameterType.setName("testParamtype");
        inputParameters.add(parameter);

        OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("input1");
        outputParameter.setParameterDescription("testDesc");
        ParameterType outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.Enum.forString("testType"));
        outputParaType.setName("testParamtype");
        outputParameters.add(outputParameter);

        serviceDescription.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[]{}));
        serviceDescription.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[]{}));

        registry.addServiceDescriptor(serviceDescription);
        assertTrue("Service desc saved successfully", registry.isServiceDescriptorExists("testServiceDesc"));
        ServiceDescription serviceDescriptor = registry.getServiceDescriptor("testServiceDesc");
        registry.removeServiceDescriptor("testServiceDesc");
    }


    public void testUpdateServiceDescriptor() throws Exception {
        ServiceDescription serviceDescription = new ServiceDescription();
        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        serviceDescription.getType().setName("testServiceDesc");
        serviceDescription.getType().setDescription("testDescription1");
        InputParameterType parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("input1");
        parameter.setParameterDescription("testDesc");
        ParameterType parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.Enum.forString("testType"));
        parameterType.setName("testParamtype");
        inputParameters.add(parameter);

        OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("input1");
        outputParameter.setParameterDescription("testDesc");
        ParameterType outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.Enum.forString("testType"));
        outputParaType.setName("testParamtype");
        outputParameters.add(outputParameter);

        serviceDescription.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[]{}));
        serviceDescription.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[]{}));

        registry.addServiceDescriptor(serviceDescription);
        ServiceDescription testServiceDesc = registry.getServiceDescriptor("testServiceDesc");
        testServiceDesc.getType().setDescription("testDescription2");

        parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("input2");
        parameter.setParameterDescription("testDesc2");
        parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.Enum.forString("testType2"));
        parameterType.setName("testParamtype2");
        inputParameters.add(parameter);

        outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("input2");
        outputParameter.setParameterDescription("testDesc2");
        outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.Enum.forString("testType2"));
        outputParaType.setName("testParamtype2");
        outputParameters.add(outputParameter);

        testServiceDesc.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[]{}));
        testServiceDesc.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[]{}));

        registry.updateServiceDescriptor(testServiceDesc);
        assertTrue("Service updated successfully", registry.getServiceDescriptor("testServiceDesc").getType().getDescription().equals("testDescription2"));
        registry.removeServiceDescriptor("testServiceDesc");
    }


    public void testGetServiceDescriptor() throws Exception {
        ServiceDescription serviceDescription = new ServiceDescription();
        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        serviceDescription.getType().setName("testServiceDesc");
        serviceDescription.getType().setDescription("testDescription1");
        InputParameterType parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("input1");
        parameter.setParameterDescription("testDesc");
        ParameterType parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.Enum.forString("testType"));
        parameterType.setName("testParamtype");
        inputParameters.add(parameter);

        OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("input1");
        outputParameter.setParameterDescription("testDesc");
        ParameterType outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.Enum.forString("testType"));
        outputParaType.setName("testParamtype");
        outputParameters.add(outputParameter);

        serviceDescription.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[]{}));
        serviceDescription.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[]{}));

        registry.addServiceDescriptor(serviceDescription);
        ServiceDescription testServiceDesc = registry.getServiceDescriptor("testServiceDesc");

        assertNotNull("service descriptor retrieved successfully", testServiceDesc);
        registry.removeServiceDescriptor("testServiceDesc");
    }


    public void testRemoveServiceDescriptor() throws Exception {
        ServiceDescription serviceDescription = new ServiceDescription();
        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        serviceDescription.getType().setName("testServiceDesc");
        InputParameterType parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("input1");
        parameter.setParameterDescription("testDesc");
        ParameterType parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.Enum.forString("testType"));
        parameterType.setName("testParamtype");
        inputParameters.add(parameter);

        OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("input1");
        outputParameter.setParameterDescription("testDesc");
        ParameterType outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.Enum.forString("testType"));
        outputParaType.setName("testParamtype");
        outputParameters.add(outputParameter);

        serviceDescription.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[]{}));
        serviceDescription.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[]{}));

        registry.addServiceDescriptor(serviceDescription);
        registry.removeServiceDescriptor("testServiceDesc");
        assertFalse("Service desc removed successfully", registry.isServiceDescriptorExists("testServiceDesc"));
    }


    public void testGetServiceDescriptors() throws Exception {
        ServiceDescription serviceDescription = new ServiceDescription();
        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        serviceDescription.getType().setName("testServiceDesc");
        InputParameterType parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("input1");
        parameter.setParameterDescription("testDesc");
        ParameterType parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.Enum.forString("testType"));
        parameterType.setName("testParamtype");
        inputParameters.add(parameter);

        OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("input1");
        outputParameter.setParameterDescription("testDesc");
        ParameterType outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.Enum.forString("testType"));
        outputParaType.setName("testParamtype");
        outputParameters.add(outputParameter);

        serviceDescription.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[]{}));
        serviceDescription.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[]{}));
        registry.addServiceDescriptor(serviceDescription);
        List<ServiceDescription> serviceDescriptors = registry.getServiceDescriptors();

        assertTrue("Service desc retrieved successfully", serviceDescriptors.size() == 1);
        registry.removeServiceDescriptor("testServiceDesc");
    }


    public void testIsApplicationDescriptorExists() throws Exception {
        ApplicationDescription applicationDescription = new ApplicationDescription(ApplicationDeploymentDescriptionType.type);
        ApplicationDeploymentDescriptionType.ApplicationName applicationName = applicationDescription.getType().addNewApplicationName();
        applicationName.setStringValue("testApplication");
        applicationDescription.getType().setApplicationName(applicationName);
        applicationDescription.getType().setInputDataDirectory("/bin");
        applicationDescription.getType().setExecutableLocation("/bin/echo");
        applicationDescription.getType().setOutputDataDirectory("/tmp");

        registry.addApplicationDescriptor("testService", "testHost", applicationDescription);
        assertTrue("application descriptor exists", registry.isApplicationDescriptorExists("testService", "testHost", "testApplication"));
        registry.removeApplicationDescriptor("testService", "testHost", "testApplication");
    }


    public void testAddApplicationDescriptorWithOtherDescriptors() throws Exception {
        ApplicationDescription applicationDescription = new ApplicationDescription(ApplicationDeploymentDescriptionType.type);
        ApplicationDeploymentDescriptionType.ApplicationName applicationName = applicationDescription.getType().addNewApplicationName();
        applicationName.setStringValue("testApplication");
        applicationDescription.getType().setApplicationName(applicationName);
        applicationDescription.getType().setInputDataDirectory("/bin");
        applicationDescription.getType().setExecutableLocation("/bin/echo");
        applicationDescription.getType().setOutputDataDirectory("/tmp");

        HostDescription hostDescription = new HostDescription(GlobusHostType.type);
        hostDescription.getType().setHostName("testHost");
        hostDescription.getType().setHostAddress("testHostAddress");

        ServiceDescription serviceDescription = new ServiceDescription();
        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        serviceDescription.getType().setName("testServiceDesc");
        InputParameterType parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("input1");
        parameter.setParameterDescription("testDesc");
        ParameterType parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.Enum.forString("testType"));
        parameterType.setName("testParamtype");
        inputParameters.add(parameter);

        OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("input1");
        outputParameter.setParameterDescription("testDesc");
        ParameterType outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.Enum.forString("testType"));
        outputParaType.setName("testParamtype");
        outputParameters.add(outputParameter);

        serviceDescription.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[]{}));
        serviceDescription.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[]{}));

        registry.addApplicationDescriptor(serviceDescription, hostDescription, applicationDescription);
        assertTrue("application hostDescription added successfully", registry.isApplicationDescriptorExists("testServiceDesc", "testHost", "testApplication"));
        registry.removeApplicationDescriptor("testServiceDesc", "testHost", "testApplication");
    }


    public void testAddApplicationDescriptor() throws Exception {
        ApplicationDescription applicationDescription = new ApplicationDescription(ApplicationDeploymentDescriptionType.type);
        ApplicationDeploymentDescriptionType.ApplicationName applicationName = applicationDescription.getType().addNewApplicationName();
        applicationName.setStringValue("testApplication");
        applicationDescription.getType().setApplicationName(applicationName);
        applicationDescription.getType().setInputDataDirectory("/bin");
        applicationDescription.getType().setExecutableLocation("/bin/echo");
        applicationDescription.getType().setOutputDataDirectory("/tmp");

        registry.addApplicationDescriptor("testService", "testHost", applicationDescription);
        assertTrue("application descriptor added successfully", registry.isApplicationDescriptorExists("testService", "testHost", "testApplication"));
        registry.removeApplicationDescriptor("testService", "testHost", "testApplication");
    }


    public void testUpdateApplicationDescriptorWithOtherDescriptors() throws Exception {
        ApplicationDescription applicationDescription = new ApplicationDescription(ApplicationDeploymentDescriptionType.type);
        ApplicationDeploymentDescriptionType.ApplicationName applicationName = applicationDescription.getType().addNewApplicationName();
        applicationName.setStringValue("testApplication");
        applicationDescription.getType().setApplicationName(applicationName);
        applicationDescription.getType().setInputDataDirectory("/bin");
        applicationDescription.getType().setExecutableLocation("/bin/echo");
        applicationDescription.getType().setOutputDataDirectory("/tmp");

        HostDescription hostDescription = new HostDescription(GlobusHostType.type);
        hostDescription.getType().setHostName("testHost");
        hostDescription.getType().setHostAddress("testHostAddress");

        ServiceDescription serviceDescription = new ServiceDescription();
        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();
        serviceDescription.getType().setName("testServiceDesc");
        InputParameterType parameter = InputParameterType.Factory.newInstance();
        parameter.setParameterName("input1");
        parameter.setParameterDescription("testDesc");
        ParameterType parameterType = parameter.addNewParameterType();
        parameterType.setType(DataType.Enum.forString("testType"));
        parameterType.setName("testParamtype");
        inputParameters.add(parameter);

        OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
        outputParameter.setParameterName("input1");
        outputParameter.setParameterDescription("testDesc");
        ParameterType outputParaType = outputParameter.addNewParameterType();
        outputParaType.setType(DataType.Enum.forString("testType"));
        outputParaType.setName("testParamtype");
        outputParameters.add(outputParameter);

        serviceDescription.getType().setInputParametersArray(inputParameters.toArray(new InputParameterType[]{}));
        serviceDescription.getType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[]{}));

        registry.addApplicationDescriptor(serviceDescription, hostDescription, applicationDescription);

        ApplicationDescription applicationDescriptor = registry.getApplicationDescriptor("testServiceDesc", "testHost", "testApplication");
        applicationDescriptor.getType().setExecutableLocation("/bin/echo1");

        registry.udpateApplicationDescriptor(serviceDescription, hostDescription, applicationDescriptor);

        ApplicationDescription descriptor = registry.getApplicationDescriptor("testServiceDesc", "testHost", "testApplication");
        String executableLocation = descriptor.getType().getExecutableLocation();

        assertTrue("application descriptor updated successfully", executableLocation.equals("/bin/echo1"));
        registry.removeApplicationDescriptor("testServiceDesc", "testHost", "testApplication");

    }

    public void testUpdateApplicationDescriptor() throws Exception {
        ApplicationDescription applicationDescription = new ApplicationDescription(ApplicationDeploymentDescriptionType.type);
        ApplicationDeploymentDescriptionType.ApplicationName applicationName = applicationDescription.getType().addNewApplicationName();
        applicationName.setStringValue("testApplication");
        applicationDescription.getType().setApplicationName(applicationName);
        applicationDescription.getType().setInputDataDirectory("/bin");
        applicationDescription.getType().setExecutableLocation("/bin/echo");
        applicationDescription.getType().setOutputDataDirectory("/tmp");

        registry.addApplicationDescriptor("testService", "testHost", applicationDescription);
        ApplicationDescription applicationDescriptor = registry.getApplicationDescriptor("testService", "testHost", "testApplication");
        applicationDescriptor.getType().setExecutableLocation("/bin/echo1");
        registry.updateApplicationDescriptor("testService", "testHost", applicationDescriptor);

        ApplicationDescription descriptor = registry.getApplicationDescriptor("testService", "testHost", "testApplication");
        String executableLocation = descriptor.getType().getExecutableLocation();

        assertTrue("application descriptor updated successfully", executableLocation.equals("/bin/echo1"));
        registry.removeApplicationDescriptor("testService", "testHost", "testApplication");

    }


    public void testGetApplicationDescriptor() throws Exception {
        ApplicationDescription applicationDescription = new ApplicationDescription(ApplicationDeploymentDescriptionType.type);
        ApplicationDeploymentDescriptionType.ApplicationName applicationName = applicationDescription.getType().addNewApplicationName();
        applicationName.setStringValue("testApplication");
        applicationDescription.getType().setApplicationName(applicationName);
        applicationDescription.getType().setInputDataDirectory("/bin");
        applicationDescription.getType().setExecutableLocation("/bin/echo");
        applicationDescription.getType().setOutputDataDirectory("/tmp");

        registry.addApplicationDescriptor("testService", "testHost", applicationDescription);
        ApplicationDescription applicationDescriptor = registry.getApplicationDescriptor("testService", "testHost", "testApplication");
        applicationDescriptor.getType().setExecutableLocation("/bin/echo1");
        registry.updateApplicationDescriptor("testService", "testHost", applicationDescriptor);

        ApplicationDescription descriptor = registry.getApplicationDescriptor("testService", "testHost", "testApplication");
        assertNotNull("application descriptor retrieved successfully", descriptor);
        registry.removeApplicationDescriptor("testService", "testHost", "testApplication");
    }


    public void testGetApplicationDescriptorsForServiceAndHost() throws Exception {
        ApplicationDescription applicationDescription = new ApplicationDescription(ApplicationDeploymentDescriptionType.type);
        ApplicationDeploymentDescriptionType.ApplicationName applicationName = applicationDescription.getType().addNewApplicationName();
        applicationName.setStringValue("testApplication");
        applicationDescription.getType().setApplicationName(applicationName);
        applicationDescription.getType().setInputDataDirectory("/bin");
        applicationDescription.getType().setExecutableLocation("/bin/echo");
        applicationDescription.getType().setOutputDataDirectory("/tmp");

        registry.addApplicationDescriptor("testService", "testHost", applicationDescription);

        ApplicationDescription description = registry.getApplicationDescriptors("testService", "testHost");
        assertNotNull("application descriptor retrieved successfully", description);
        registry.removeApplicationDescriptor("testService", "testHost", "testApplication");
    }

    public void testGetApplicationDescriptorsForService() throws Exception {
        ApplicationDescription applicationDescription = new ApplicationDescription(ApplicationDeploymentDescriptionType.type);
        ApplicationDeploymentDescriptionType.ApplicationName applicationName = applicationDescription.getType().addNewApplicationName();
        applicationName.setStringValue("testApplication");
        applicationDescription.getType().setApplicationName(applicationName);
        applicationDescription.getType().setInputDataDirectory("/bin");
        applicationDescription.getType().setExecutableLocation("/bin/echo");
        applicationDescription.getType().setOutputDataDirectory("/tmp");

        registry.addApplicationDescriptor("testService", "testHost1", applicationDescription);
        registry.addApplicationDescriptor("testService", "testHost2", applicationDescription);

        Map<String,ApplicationDescription> applicationDescriptors = registry.getApplicationDescriptors("testService");
        assertTrue("application retrieved successfully", applicationDescriptors.size()==2);

        registry.removeApplicationDescriptor("testService", "testHost1", "testApplication");
        registry.removeApplicationDescriptor("testService", "testHost2", "testApplication");
    }


    public void testGetApplicationDescriptors() throws Exception {
        ApplicationDescription applicationDescription = new ApplicationDescription(ApplicationDeploymentDescriptionType.type);
        ApplicationDeploymentDescriptionType.ApplicationName applicationName = applicationDescription.getType().addNewApplicationName();
        applicationName.setStringValue("testApplication");
        applicationDescription.getType().setApplicationName(applicationName);
        applicationDescription.getType().setInputDataDirectory("/bin");
        applicationDescription.getType().setExecutableLocation("/bin/echo");
        applicationDescription.getType().setOutputDataDirectory("/tmp");

        registry.addApplicationDescriptor("testService", "testHost1", applicationDescription);
        registry.addApplicationDescriptor("testService", "testHost2", applicationDescription);

        Map<String[], ApplicationDescription> applicationDescriptors = registry.getApplicationDescriptors();
        assertTrue("application retrieved successfully", applicationDescriptors.size()==2);

        registry.removeApplicationDescriptor("testService", "testHost1", "testApplication");
        registry.removeApplicationDescriptor("testService", "testHost2", "testApplication");

    }


    public void testRemoveApplicationDescriptor() throws Exception {
        ApplicationDescription applicationDescription = new ApplicationDescription(ApplicationDeploymentDescriptionType.type);
        ApplicationDeploymentDescriptionType.ApplicationName applicationName = applicationDescription.getType().addNewApplicationName();
        applicationName.setStringValue("testApplication");
        applicationDescription.getType().setApplicationName(applicationName);
        applicationDescription.getType().setInputDataDirectory("/bin");
        applicationDescription.getType().setExecutableLocation("/bin/echo");
        applicationDescription.getType().setOutputDataDirectory("/tmp");

        registry.addApplicationDescriptor("testService", "testHost", applicationDescription);
        registry.removeApplicationDescriptor("testService", "testHost", "testApplication");

        assertFalse("application descriptor removed successfully", registry.isApplicationDescriptorExists("testService", "testHost", "testApplication"));
    }

    public void testIsWorkspaceProjectExists() throws Exception {
        WorkspaceProject workspaceProject = new WorkspaceProject("testProject", registry);
        registry.addWorkspaceProject(workspaceProject);

        assertTrue("workspace project exists", registry.isWorkspaceProjectExists("testProject"));
        registry.deleteWorkspaceProject("testProject");
    }


    public void testAddWorkspaceProject() throws Exception {
        WorkspaceProject workspaceProject = new WorkspaceProject("testProject", registry);
        registry.addWorkspaceProject(workspaceProject);

        assertTrue("workspace project added successfully", registry.isWorkspaceProjectExists("testProject"));
        registry.deleteWorkspaceProject("testProject");
    }


//    public void testUpdateWorkspaceProject() throws Exception {
//        WorkspaceProject workspaceProject = new WorkspaceProject("testProject", registry);
//        registry.addWorkspaceProject(workspaceProject);
//        WorkspaceProject testProject = registry.getWorkspaceProject("testProject");
//        testProject.setProjectName("testProject2");
//
//        registry.updateWorkspaceProject(testProject);
//
//        assertTrue("workspace project updated", registry.isWorkspaceProjectExists("testProject2"));
//        registry.deleteWorkspaceProject("testProject2");
//    }


    public void testDeleteWorkspaceProject() throws Exception {
        WorkspaceProject workspaceProject = new WorkspaceProject("testProject", registry);
        registry.addWorkspaceProject(workspaceProject);

        registry.deleteWorkspaceProject("testProject");
        assertFalse("workspace project deleted successfully", registry.isWorkspaceProjectExists("testProject"));
    }


    public void testGetWorkspaceProject() throws Exception {
        WorkspaceProject workspaceProject = new WorkspaceProject("testProject", registry);
        registry.addWorkspaceProject(workspaceProject);
        WorkspaceProject testProject = registry.getWorkspaceProject("testProject");
        assertNotNull("workspace project retrieved successfully", testProject);
        registry.deleteWorkspaceProject("testProject");
    }


    public void testGetWorkspaceProjects() throws Exception {
        WorkspaceProject workspaceProject1 = new WorkspaceProject("testProject1", registry);
        registry.addWorkspaceProject(workspaceProject1);
        WorkspaceProject workspaceProject2 = new WorkspaceProject("testProject2", registry);
        registry.addWorkspaceProject(workspaceProject2);

        List<WorkspaceProject> workspaceProjects = registry.getWorkspaceProjects();
        assertTrue("workspace projects retrieved successfully", workspaceProjects.size() == 2);

        registry.deleteWorkspaceProject("testProject1");
        registry.deleteWorkspaceProject("testProject2");
    }


    public void testAddExperiment() throws Exception {
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject"))  {
            workspaceProject =  new WorkspaceProject("testProject", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExperiment");
        registry.addExperiment("testProject", experiment);

        ExperimentData testExperiment = registry.getExperiment("testExperiment");
        String user = testExperiment.getUser();

        assertTrue("experiment saved successfully", registry.isExperimentExists("testExperiment"));
        registry.removeExperiment("testExperiment");
        registry.deleteWorkspaceProject("testProject");
    }


    public void testRemoveExperiment() throws Exception {
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject"))  {
            workspaceProject =  new WorkspaceProject("testProject", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExperiment");
        registry.addExperiment("testProject", experiment);

        registry.removeExperiment("testExperiment");
        assertFalse("experiment removed successfully", registry.isExperimentExists("testExperiment"));

        registry.deleteWorkspaceProject("testProject");
    }


//    public void testGetExperiments() throws Exception {
//        WorkspaceProject workspaceProject = null;
//        if(!registry.isWorkspaceProjectExists("testProject"))  {
//            workspaceProject =  new WorkspaceProject("testProject", registry);
//            registry.addWorkspaceProject(workspaceProject);
//        }
//        AiravataExperiment experiment1 = new AiravataExperiment();
//        experiment1.setExperimentId("testExperiment1");
//        Calendar calender = Calendar.getInstance();
//        java.util.Date d = calender.getTime();
//        Date currentTime = new Date(d.getTime());
//        experiment1.setSubmittedDate(currentTime);
//        registry.addExperiment("testProject", experiment1);
//
//        AiravataExperiment experiment2 = new AiravataExperiment();
//        experiment2.setExperimentId("testExperiment2");
//        experiment2.setSubmittedDate(currentTime);
//        registry.addExperiment("testProject", experiment2);
//
//        List<AiravataExperiment> experiments = registry.getExperiments();
//
//        assertTrue("experiments retrieved successfully", experiments.size() == 2);
//        registry.removeExperiment("testExperiment1");
//        registry.removeExperiment("testExperiment2");
//        registry.deleteWorkspaceProject("testProject");
//    }


//    public void testGetExperimentsForProject() throws Exception {
//        WorkspaceProject workspaceProject = null;
//        if(!registry.isWorkspaceProjectExists("testProject"))  {
//            workspaceProject =  new WorkspaceProject("testProject", registry);
//            registry.addWorkspaceProject(workspaceProject);
//        }
//        AiravataExperiment experiment1 = new AiravataExperiment();
//        experiment1.setExperimentId("testExperiment1");
//        experiment1.setProject(workspaceProject);
//        Calendar calender = Calendar.getInstance();
//        java.util.Date d = calender.getTime();
//        Date currentTime = new Date(d.getTime());
//        experiment1.setSubmittedDate(currentTime);
//        registry.addExperiment("testProject", experiment1);
//
//        AiravataExperiment experiment2 = new AiravataExperiment();
//        experiment2.setExperimentId("testExperiment2");
//        experiment2.setSubmittedDate(currentTime);
//        experiment2.setProject(workspaceProject);
//        registry.addExperiment("testProject", experiment2);
//
//        List<AiravataExperiment> experiments = registry.getExperiments("testProject");
//
//        assertTrue("experiments retrieved successfully", experiments.size() == 2);
//        registry.removeExperiment("testExperiment1");
//        registry.removeExperiment("testExperiment2");
//        registry.deleteWorkspaceProject("testProject");
//    }


//    public void testGetExperimentsDuringPeriod() throws Exception {
//        WorkspaceProject workspaceProject = null;
//        if(!registry.isWorkspaceProjectExists("testProject"))  {
//            workspaceProject =  new WorkspaceProject("testProject", registry);
//            registry.addWorkspaceProject(workspaceProject);
//        }
//        AiravataExperiment experiment1 = new AiravataExperiment();
//        experiment1.setExperimentId("testExperiment1");
//        Calendar calender = Calendar.getInstance();
//        calender.set(Calendar.YEAR, 2012);
//        calender.set(Calendar.MONTH, Calendar.JANUARY);
//        calender.set(Calendar.DATE, 2);
//        java.util.Date d = calender.getTime();
//        Date currentTime = new Date(d.getTime());
//        experiment1.setSubmittedDate(currentTime);
//        experiment1.setProject(workspaceProject);
//        registry.addExperiment("testProject", experiment1);
//
//        AiravataExperiment experiment2 = new AiravataExperiment();
//        experiment2.setExperimentId("testExperiment2");
//        Calendar c = Calendar.getInstance();
//        java.util.Date date = c.getTime();
//        Date experiment2Time = new Date(date.getTime());
//        experiment2.setSubmittedDate(experiment2Time);
//        experiment2.setProject(workspaceProject);
//        registry.addExperiment("testProject", experiment2);
//
//        Calendar a = Calendar.getInstance();
//        a.set(Calendar.YEAR, 2012);
//        a.set(Calendar.MONTH, Calendar.JANUARY);
//        a.set(Calendar.DATE, 1);
//        java.util.Date da = a.getTime();
//        Date ct = new Date(da.getTime());
//
//        Calendar aa = Calendar.getInstance();
//        aa.set(Calendar.YEAR, 2012);
//        aa.set(Calendar.MONTH, Calendar.DECEMBER);
//        aa.set(Calendar.DATE, 1);
//        java.util.Date dda = aa.getTime();
//
//        List<AiravataExperiment> experiments = registry.getExperiments(ct, dda);
//
//        assertTrue("experiments retrieved successfully", experiments.size() != 0);
//        registry.removeExperiment("testExperiment1");
//        registry.removeExperiment("testExperiment2");
//        registry.deleteWorkspaceProject("testProject");
//
//    }


//    public void testGetExperimentsPerProjectDuringPeriod() throws Exception {
//        WorkspaceProject workspaceProject = null;
//        if(!registry.isWorkspaceProjectExists("testProject"))  {
//            workspaceProject =  new WorkspaceProject("testProject", registry);
//            registry.addWorkspaceProject(workspaceProject);
//        }
//        AiravataExperiment experiment1 = new AiravataExperiment();
//        experiment1.setExperimentId("testExperiment1");
//        Calendar calender = Calendar.getInstance();
//        calender.set(Calendar.YEAR, 2012);
//        calender.set(Calendar.MONTH, Calendar.JANUARY);
//        calender.set(Calendar.DATE, 2);
//        java.util.Date d = calender.getTime();
//        Date currentTime = new Date(d.getTime());
//        experiment1.setSubmittedDate(currentTime);
//        registry.addExperiment("testProject", experiment1);
//
//        AiravataExperiment experiment2 = new AiravataExperiment();
//        experiment2.setExperimentId("testExperiment2");
//        Calendar c = Calendar.getInstance();
//        java.util.Date date = c.getTime();
//        Date experiment2Time = new Date(date.getTime());
//        experiment2.setSubmittedDate(experiment2Time);
//        registry.addExperiment("testProject", experiment2);
//
//        Calendar a = Calendar.getInstance();
//        a.set(Calendar.YEAR, 2012);
//        a.set(Calendar.MONTH, Calendar.JANUARY);
//        a.set(Calendar.DATE, 1);
//        java.util.Date da = a.getTime();
//        Date ct = new Date(da.getTime());
//
//        Calendar aa = Calendar.getInstance();
//        aa.set(Calendar.YEAR, 2012);
//        aa.set(Calendar.MONTH, Calendar.DECEMBER);
//        aa.set(Calendar.DATE, 1);
//        java.util.Date dda = aa.getTime();
//
//        List<AiravataExperiment> experiments = registry.getExperiments("testProject", ct, dda);
//
//        assertTrue("experiments retrieved successfully", experiments.size() == 2);
//        registry.removeExperiment("testExperiment1");
//        registry.removeExperiment("testExperiment2");
//        registry.deleteWorkspaceProject("testProject");
//
//    }


    public void testIsExperimentExists() throws Exception {
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject"))  {
            workspaceProject =  new WorkspaceProject("testProject", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExperiment");
        registry.addExperiment("testProject", experiment);

        assertTrue("experiment exists", registry.isExperimentExists("testExperiment"));
        registry.removeExperiment("testExperiment");
        registry.deleteWorkspaceProject("testProject");
    }

    public void testUpdateExperimentExecutionUser() throws Exception {
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject"))  {
            workspaceProject =  new WorkspaceProject("testProject", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExperiment");
        registry.addExperiment("testProject", experiment);
        registry.updateExperimentExecutionUser("testExperiment", "testUser");
        ExperimentData testExperiment = registry.getExperiment("testExperiment");
        assertTrue("execution user updated successfully", testExperiment.getUser().equals("testUser"));
        registry.removeExperiment("testExperiment");
        registry.deleteWorkspaceProject("testProject");
    }


    public void testGetExperimentExecutionUser() throws Exception {
        Calendar c = Calendar.getInstance();
        java.util.Date date = c.getTime();
        Date currentTime = new Date(date.getTime());
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject"))  {
            workspaceProject =  new WorkspaceProject("testProject", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExp");
        experiment.setSubmittedDate(currentTime);
        registry.addExperiment("testProject", experiment);
        registry.updateExperimentExecutionUser("testExp", "admin");

        ExperimentData testExperiment = registry.getExperiment("testExp");
        assertTrue("execution user retrieved successfully", testExperiment.getUser().equals("admin"));
        registry.removeExperiment("testExp");
        registry.deleteWorkspaceProject("testProject");
    }


    public void testGetExperimentName() throws Exception {
        Calendar c = Calendar.getInstance();
        java.util.Date date = c.getTime();
        Date currentTime = new Date(date.getTime());
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject"))  {
            workspaceProject =  new WorkspaceProject("testProject", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExp");
        experiment.setSubmittedDate(currentTime);
        registry.addExperiment("testProject", experiment);
        registry.updateExperimentExecutionUser("testExp", "admin");
        registry.updateExperimentName("testExp", "testExperiment");

        ExperimentData testExperiment = registry.getExperiment("testExp");
        assertTrue("experiment name retrieved successfully", testExperiment.getExperimentName().equals("testExperiment"));
        registry.removeExperiment("testExp");
        registry.deleteWorkspaceProject("testProject");
    }


    public void testUpdateExperimentName() throws Exception {
        Calendar c = Calendar.getInstance();
        java.util.Date date = c.getTime();
        Date currentTime = new Date(date.getTime());
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject"))  {
            workspaceProject =  new WorkspaceProject("testProject", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExp");
        experiment.setSubmittedDate(currentTime);
        registry.addExperiment("testProject", experiment);
        registry.updateExperimentExecutionUser("testExp", "admin");
        registry.updateExperimentName("testExp", "testExperiment");

        ExperimentData testExperiment = registry.getExperiment("testExp");
        assertTrue("experiment name updated successfully", testExperiment.getExperimentName().equals("testExperiment"));
        registry.removeExperiment("testExp");
        registry.deleteWorkspaceProject("testProject");
    }


    public void testGetExperimentMetadata() throws Exception {
        Calendar c = Calendar.getInstance();
        java.util.Date date = c.getTime();
        Date currentTime = new Date(date.getTime());
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject"))  {
            workspaceProject =  new WorkspaceProject("testProject", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExp");
        experiment.setSubmittedDate(currentTime);
        registry.addExperiment("testProject", experiment);
        registry.updateExperimentMetadata("testExp", "testMetadata");

        assertTrue("experiment metadata retrieved successfully", registry.getExperimentMetadata("testExp").equals("testMetadata"));
        registry.removeExperiment("testExp");
        registry.deleteWorkspaceProject("testProject");
    }


    public void testUpdateExperimentMetadata() throws Exception {
        Calendar c = Calendar.getInstance();
        java.util.Date date = c.getTime();
        Date currentTime = new Date(date.getTime());
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject"))  {
            workspaceProject =  new WorkspaceProject("testProject", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExp");
        experiment.setSubmittedDate(currentTime);
        registry.addExperiment("testProject", experiment);
        registry.updateExperimentMetadata("testExp", "testMetadata");

        assertTrue("experiment metadata updated successfully", registry.getExperimentMetadata("testExp").equals("testMetadata"));
        registry.removeExperiment("testExp");
        registry.deleteWorkspaceProject("testProject");
    }


    public void testGetWorkflowExecutionTemplateName() throws Exception {
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject"))  {
            workspaceProject =  new WorkspaceProject("testProject", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExp");
        registry.addExperiment("testProject", experiment);

        registry.updateExperimentExecutionUser("testExp", "admin");
        registry.updateExperimentName("testExp", "testexperiment");

        ExperimentData experimentData = registry.getExperiment("testExp");

        registry.addWorkflowInstance("testExp", "testWorkflow1", "template1");

        String testWorkflow = registry.getWorkflowExecutionTemplateName("testWorkflow1");
        assertTrue("workflow execution template name retrieved successfully", testWorkflow.equals("template1"));

        registry.removeExperiment("testExp");
        registry.deleteWorkspaceProject("testProject");

    }


    public void testSetWorkflowInstanceTemplateName() throws Exception {
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject"))  {
            workspaceProject =  new WorkspaceProject("testProject", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExp");
        registry.addExperiment("testProject", experiment);

        registry.updateExperimentExecutionUser("testExp", "admin");
        registry.updateExperimentName("testExp", "testexperiment");

        ExperimentData experimentData = registry.getExperiment("testExp");

        registry.addWorkflowInstance("testExp", "testWorkflow2", "template1");
        registry.setWorkflowInstanceTemplateName("testWorkflow2", "template2");

        String testWorkflow = registry.getWorkflowExecutionTemplateName("testWorkflow2");
        assertTrue("workflow execution template name retrieved successfully", testWorkflow.equals("template2"));

        registry.removeExperiment("testExp");
        registry.deleteWorkspaceProject("testProject");
    }


    public void testGetExperimentWorkflowInstances() throws Exception {
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject"))  {
            workspaceProject =  new WorkspaceProject("testProject", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExp");
        registry.addExperiment("testProject", experiment);

        registry.updateExperimentExecutionUser("testExp", "admin");
        registry.updateExperimentName("testExp", "testexperiment");

        registry.addWorkflowInstance("testExp", "testWorkflow3", "template1");
        List<WorkflowExecution> workflowInstances = registry.getExperimentWorkflowInstances("testExp");

        assertTrue("workflow instances retrieved successfully", workflowInstances.size() != 0);
        registry.removeExperiment("testExp");
        registry.deleteWorkspaceProject("testProject");
    }


    public void testIsWorkflowInstanceExists() throws Exception {
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject"))  {
            workspaceProject =  new WorkspaceProject("testProject", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExp");
        registry.addExperiment("testProject", experiment);

        registry.updateExperimentExecutionUser("testExp", "admin");
        registry.updateExperimentName("testExp", "testexperiment");

        registry.addWorkflowInstance("testExp", "testWorkflow4", "template1");

        assertTrue("workflow instance exists", registry.isWorkflowInstanceExists("testWorkflow4"));
        registry.removeExperiment("testExp");
        registry.deleteWorkspaceProject("testProject");
    }

    public void testUpdateWorkflowInstanceStatus() throws Exception {
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject"))  {
            workspaceProject =  new WorkspaceProject("testProject", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExp");
        registry.addExperiment("testProject", experiment);

        registry.updateExperimentExecutionUser("testExp", "admin");
        registry.updateExperimentName("testExp", "testexperiment");

        registry.addWorkflowInstance("testExp", "testWorkflow5", "template1");
        registry.updateWorkflowInstanceStatus("testWorkflow5", WorkflowExecutionStatus.State.STARTED);

        assertTrue("workflow instance status updated successfully", registry.getWorkflowInstanceStatus("testWorkflow5").getExecutionStatus().equals(WorkflowExecutionStatus.State.STARTED));
        registry.removeExperiment("testExp");
        registry.deleteWorkspaceProject("testProject");
    }


    public void testGetWorkflowInstanceStatus() throws Exception {
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject1"))  {
            workspaceProject =  new WorkspaceProject("testProject1", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExp");
        registry.addExperiment("testProject1", experiment);

        registry.updateExperimentExecutionUser("testExp", "admin");
        registry.updateExperimentName("testExp", "testexperiment");

        registry.addWorkflowInstance("testExp", "testWorkflow6", "template1");
        Calendar c = Calendar.getInstance();
        java.util.Date date = c.getTime();
        Date currentTime = new Date(date.getTime());

        registry.updateWorkflowInstanceStatus(new WorkflowExecutionStatus(new WorkflowExecution("testExp", "testWorkflow6"), WorkflowExecutionStatus.State.STARTED,currentTime));
        assertTrue("workflow instance status updated successfully", registry.getWorkflowInstanceStatus("testWorkflow6").getExecutionStatus().equals(WorkflowExecutionStatus.State.STARTED));
        registry.removeExperiment("testExp");
        registry.deleteWorkspaceProject("testProject1");

    }

    public void testUpdateWorkflowNodeInput() throws Exception {
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject1"))  {
            workspaceProject =  new WorkspaceProject("testProject1", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExp");
        registry.addExperiment("testProject1", experiment);

        registry.updateExperimentExecutionUser("testExp", "admin");
        registry.updateExperimentName("testExp", "testexperiment");

        registry.addWorkflowInstance("testExp", "testWorkflow7", "template1");

        WorkflowInstanceNode workflowInstanceNode = new WorkflowInstanceNode(new WorkflowExecution("testExp", "testWorkflow7"), "testNode");
        WorkflowNodeType nodeType = new WorkflowNodeType(WorkflowNodeType.WorkflowNode.INPUTNODE);
        registry.updateWorkflowNodeType(workflowInstanceNode, nodeType);
        registry.updateWorkflowNodeInput(workflowInstanceNode, "testParameter=testData");

        NodeExecutionData nodeData = registry.getWorkflowInstanceNodeData("testWorkflow7", "testNode");
        assertTrue("workflow instance node input saved successfully", nodeData.getInput().equals("testParameter=testData"));

        registry.removeExperiment("testExp");
        registry.deleteWorkspaceProject("testProject1");

    }


    public void testUpdateWorkflowNodeOutput() throws Exception {
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject1"))  {
            workspaceProject =  new WorkspaceProject("testProject1", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExp");
        registry.addExperiment("testProject1", experiment);

        registry.updateExperimentExecutionUser("testExp", "admin");
        registry.updateExperimentName("testExp", "testexperiment");

        registry.addWorkflowInstance("testExp", "testWorkflow8", "template1");

        WorkflowInstanceNode workflowInstanceNode = new WorkflowInstanceNode(new WorkflowExecution("testExp", "testWorkflow8"), "testNode");
        registry.updateWorkflowNodeOutput(workflowInstanceNode, "testData");

        NodeExecutionData nodeData = registry.getWorkflowInstanceNodeData("testWorkflow8", "testNode");
        assertTrue("workflow instance node output saved successfully", nodeData.getOutput().equals("testData"));

        registry.removeExperiment("testExp");
        registry.deleteWorkspaceProject("testProject1");

    }

    public void testGetExperiment() throws Exception {
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject1"))  {
            workspaceProject =  new WorkspaceProject("testProject1", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExp");
        registry.addExperiment("testProject1", experiment);

        registry.updateExperimentExecutionUser("testExp", "admin");
        registry.updateExperimentName("testExp", "testexperiment");

        registry.addWorkflowInstance("testExp", "testWorkflow9", "template1");
        ExperimentData testExp = registry.getExperiment("testExp");

        assertNotNull("experiment data retrieved successfully", testExp);
        registry.removeExperiment("testExp");
        registry.deleteWorkspaceProject("testProject1");
    }


    public void testGetExperimentMetaInformation() throws Exception {
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject1"))  {
            workspaceProject =  new WorkspaceProject("testProject1", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExp");
        registry.addExperiment("testProject1", experiment);

        registry.updateExperimentExecutionUser("testExp", "admin");
        registry.updateExperimentName("testExp", "testexperiment");

        registry.addWorkflowInstance("testExp", "testWorkflow10", "template1");
        ExperimentData testExp = registry.getExperimentMetaInformation("testExp");

        assertNotNull("experiment data retrieved successfully", testExp);
        registry.removeExperiment("testExp");
        registry.deleteWorkspaceProject("testProject1");
    }


    public void testGetAllExperimentMetaInformation() throws Exception {
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject1"))  {
            workspaceProject =  new WorkspaceProject("testProject1", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExp");
        registry.addExperiment("testProject1", experiment);

        registry.updateExperimentExecutionUser("testExp", "admin");
        registry.updateExperimentName("testExp", "testexperiment");

        registry.addWorkflowInstance("testExp", "testWorkflow11", "template1");
        ExperimentData testExp = registry.getExperimentMetaInformation("testExp");

        assertNotNull("experiment data retrieved successfully", testExp);
        registry.removeExperiment("testExp");
        registry.deleteWorkspaceProject("testProject1");
    }


    public void testGetExperimentIdByUser() throws Exception {
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject1"))  {
            workspaceProject =  new WorkspaceProject("testProject1", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment1 = new AiravataExperiment();
        experiment1.setExperimentId("testExp1");
        registry.addExperiment("testProject1", experiment1);

        AiravataExperiment experiment2 = new AiravataExperiment();
        experiment2.setExperimentId("testExp2");
        registry.addExperiment("testProject1", experiment2);

        registry.updateExperimentExecutionUser("testExp1", "admin");
        registry.updateExperimentExecutionUser("testExp2", "admin");
        registry.updateExperimentName("testExp1", "testexperiment1");
        registry.updateExperimentName("testExp2", "testexperiment2");

        registry.addWorkflowInstance("testExp1", "testWorkflow12", "template1");
        registry.addWorkflowInstance("testExp2", "testWorkflow13", "template2");
        List<String> experimentIdByUser = registry.getExperimentIdByUser("admin");

        assertNotNull("experiment ID s for user retrieved successfully", experimentIdByUser.size() != 0);
        registry.removeExperiment("testExp1");
        registry.removeExperiment("testExp2");
        registry.deleteWorkspaceProject("testProject1");
    }


    public void testGetExperimentByUser() throws Exception {
        WorkspaceProject workspaceProject = null;
        if(!registry.isWorkspaceProjectExists("testProject1"))  {
            workspaceProject =  new WorkspaceProject("testProject1", registry);
            registry.addWorkspaceProject(workspaceProject);
        }
        AiravataExperiment experiment1 = new AiravataExperiment();
        experiment1.setExperimentId("testExp1");
        registry.addExperiment("testProject1", experiment1);

        AiravataExperiment experiment2 = new AiravataExperiment();
        experiment2.setExperimentId("testExp2");
        registry.addExperiment("testProject1", experiment2);

        registry.updateExperimentExecutionUser("testExp1", "admin");
        registry.updateExperimentExecutionUser("testExp2", "admin");
        registry.updateExperimentName("testExp1", "testexperiment1");
        registry.updateExperimentName("testExp2", "testexperiment2");

        registry.addWorkflowInstance("testExp1", "testWorkflow14", "template1");
        registry.addWorkflowInstance("testExp2", "testWorkflow15", "template2");
        List<ExperimentData> experimentDataList = registry.getExperimentByUser("admin");

        assertNotNull("experiment ID s for user retrieved successfully", experimentDataList.size() != 0);
        registry.removeExperiment("testExp1");
        registry.removeExperiment("testExp2");
        registry.deleteWorkspaceProject("testProject1");
    }


//    public void testUpdateWorkflowNodeStatus() throws Exception {
//        WorkspaceProject workspaceProject1 = new WorkspaceProject("testProject", registry);
//        registry.addWorkspaceProject(workspaceProject1);
//        AiravataExperiment experiment = new AiravataExperiment();
//        experiment.setExperimentId("testExp");
//        registry.addExperiment("testProject", experiment);
//
//        registry.updateExperimentExecutionUser("testExp", "admin");
//        registry.updateExperimentName("testExp", "testexperiment");
//
//        registry.addWorkflowInstance("testExp", "testWorkflow", "template1");
//
//
//
//        WorkflowNodeType workflowNodeType = new WorkflowNodeType();
//        workflowNodeType.setNodeType(WorkflowNodeType.WorkflowNode.SERVICENODE);
//
//        WorkflowInstanceNode node = new WorkflowInstanceNode(new WorkflowInstance("testExp", "testWorkflow"), "testNode");
//        registry.addWorkflowInstanceNode("testWorkflow", "testNode");
//
//        WorkflowInstanceData workflowInstanceData = registry.getWorkflowInstanceData("testWorkflow");
//        WorkflowInstanceNodeData workflowInstanceNodeData = registry.getWorkflowInstanceNodeData("testWorkflow", "testNode");
//
//        Calendar c = Calendar.getInstance();
//        java.util.Date date = c.getTime();
//        Date currentTime = new Date(date.getTime());
//
//        workflowInstanceNodeData.setStatus(WorkflowInstanceStatus.ExecutionStatus.FINISHED, currentTime);
//        workflowInstanceData.addNodeData(workflowInstanceNodeData);
//
//        registry.updateWorkflowNodeOutput(node, "testData");
//        registry.updateWorkflowNodeType(node,workflowNodeType);
//
//        registry.updateWorkflowNodeStatus("testWorkflow", "testNode", WorkflowInstanceStatus.ExecutionStatus.FINISHED);
//
//        WorkflowInstanceNodeStatus workflowNodeStatus = registry.getWorkflowNodeStatus(node);
//
//        assertTrue("workflow instance node status updated successfully", workflowNodeStatus.getExecutionStatus().equals(WorkflowInstanceStatus.ExecutionStatus.FINISHED));
//
//        registry.removeExperiment("testExp");
//        registry.deleteWorkspaceProject("testProject");
//
//    }

    /*
    public void testGetWorkflowNodeStatus() throws Exception {
        WorkspaceProject workspaceProject1 = new WorkspaceProject("testProject", registry);
        registry.addWorkspaceProject(workspaceProject1);
        AiravataExperiment experiment = new AiravataExperiment();
        experiment.setExperimentId("testExp");
        registry.addExperiment("testProject", experiment);

        registry.updateExperimentExecutionUser("testExp", "admin");
        registry.updateExperimentName("testExp", "testexperiment");

        registry.addWorkflowInstance("testExp", "testWorkflow", "template1");

        WorkflowInstanceNode workflowInstanceNode = new WorkflowInstanceNode(new WorkflowInstance("testExp", "testWorkflow"), "testNode");
        registry.updateWorkflowNodeOutput(workflowInstanceNode, "testData");

        WorkflowInstanceNodeData nodeData = registry.getWorkflowInstanceNodeData("testWorkflow", "testNode");


        assertTrue("workflow instance node output saved successfully", nodeData.getOutput().equals("testData"));

        registry.removeExperiment("testExp");
        registry.deleteWorkspaceProject("testProject");

    }


    public void testGetWorkflowNodeStartTime() throws Exception {
//        WorkspaceProject workspaceProject1 = new WorkspaceProject("testProject", registry);
//        registry.addWorkspaceProject(workspaceProject1);
//        AiravataExperiment experiment = new AiravataExperiment();
//        experiment.setExperimentId("testExp");
//        registry.addExperiment("testProject", experiment);
//
//        registry.updateExperimentExecutionUser("testExp", "admin");
//        registry.updateExperimentName("testExp", "testexperiment");
//
//        registry.addWorkflowInstance("testExp", "testWorkflow", "template1");
//
//        WorkflowNodeType workflowNodeType = new WorkflowNodeType();
//        workflowNodeType.setNodeType(WorkflowNodeType.WorkflowNode.SERVICENODE);
//
//        WorkflowInstanceNode node = new WorkflowInstanceNode(new WorkflowInstance("testExp", "testWorkflow"), "testNode");
//        registry.addWorkflowInstanceNode("testWorkflow", "testNode");
//
//        WorkflowInstanceData workflowInstanceData = registry.getWorkflowInstanceData("testWorkflow");
//        WorkflowInstanceNodeData workflowInstanceNodeData = registry.getWorkflowInstanceNodeData("testWorkflow", "testNode");
//
//        Calendar c = Calendar.getInstance();
//        java.util.Date date = c.getTime();
//        Date currentTime = new Date(date.getTime());
//
//        workflowInstanceNodeData.setStatus(WorkflowInstanceStatus.ExecutionStatus.FINISHED, currentTime);
//        workflowInstanceData.addNodeData(workflowInstanceNodeData);
//
//        registry.updateWorkflowNodeOutput(node, "testData");
//        registry.updateWorkflowNodeType(node,workflowNodeType);
//
//        registry.updateWorkflowNodeStatus("testWorkflow", "testNode", WorkflowInstanceStatus.ExecutionStatus.FINISHED);
//
//        WorkflowInstanceNodeStatus workflowNodeStatus = registry.getWorkflowNodeStatus(node);
//
//        assertTrue("workflow instance node status updated successfully", workflowNodeStatus.getExecutionStatus().equals(WorkflowInstanceStatus.ExecutionStatus.FINISHED));
//
//        registry.removeExperiment("testExp");
//        registry.deleteWorkspaceProject("testProject");
    }


    public void testGetWorkflowStartTime() throws Exception {
    }


    public void testUpdateWorkflowNodeGramData() throws Exception {
    }


    public void testGetWorkflowInstanceData() throws Exception {
    }


    public void testIsWorkflowInstanceNodePresent() throws Exception {
    }

    public void testGetWorkflowInstanceNodeData() throws Exception {
    }


    public void testAddWorkflowInstance() throws Exception {
    }


    public void testUpdateWorkflowNodeType() throws Exception {
    }


    public void testAddWorkflowInstanceNode() throws Exception {
    }   */



    public void testIsPublishedWorkflowExists() throws Exception {
        registry.addWorkflow("workflow1", "testContent");
        registry.publishWorkflow("workflow1");

        assertTrue("published workflow exists", registry.isPublishedWorkflowExists("workflow1"));

        registry.removePublishedWorkflow("workflow1");
        registry.removeWorkflow("workflow1");
    }


    public void testPublishWorkflow() throws Exception {
        registry.addWorkflow("workflow1", "testContent");
        registry.publishWorkflow("workflow1");

        assertTrue("workflow is published", registry.isPublishedWorkflowExists("workflow1"));

        registry.removePublishedWorkflow("workflow1");
        registry.removeWorkflow("workflow1");
    }


    public void testPublishWorkflowWithGivenName() throws Exception {
        registry.addWorkflow("workflow1", "testContent");
        registry.publishWorkflow("workflow1", "publishedWorkflow1");

        assertTrue("workflow published with given name", registry.isPublishedWorkflowExists("publishedWorkflow1"));

        registry.removePublishedWorkflow("publishedWorkflow1");
        registry.removeWorkflow("workflow1");
    }


    public void testGetPublishedWorkflowGraphXML() throws Exception {
        registry.addWorkflow("workflow1", "testContent");
        registry.publishWorkflow("workflow1");

        String graphXML = registry.getPublishedWorkflowGraphXML("workflow1");

        assertTrue("workflow content retrieved successfully", "testContent".equals(graphXML));

        registry.removePublishedWorkflow("workflow1");
        registry.removeWorkflow("workflow1");
    }


    public void testGetPublishedWorkflowNames() throws Exception {
        registry.addWorkflow("workflow1", "testContent");
        registry.publishWorkflow("workflow1", "publishWorkflow1");
        registry.publishWorkflow("workflow1", "publishWorkflow2");

        List<String> publishedWorkflowNames = registry.getPublishedWorkflowNames();

        assertTrue("published workflow names retrieved successfully", publishedWorkflowNames.size() == 2);
        registry.removePublishedWorkflow("publishWorkflow1");
        registry.removePublishedWorkflow("publishWorkflow2");
        registry.removeWorkflow("workflow1");
    }


    public void testGetPublishedWorkflows() throws Exception {
        registry.addWorkflow("workflow1", "testContent");
        registry.publishWorkflow("workflow1", "publishWorkflow1");
        registry.publishWorkflow("workflow1", "publishWorkflow2");

        Map<String, String> publishedWorkflows = registry.getPublishedWorkflows();

        assertTrue("published workflows retrieved successfully", publishedWorkflows.size() == 2);
        registry.removePublishedWorkflow("publishWorkflow1");
        registry.removePublishedWorkflow("publishWorkflow2");
        registry.removeWorkflow("workflow1");
    }


    public void testRemovePublishedWorkflow() throws Exception {
        registry.addWorkflow("workflow1", "testContent");
        registry.publishWorkflow("workflow1");
        registry.removePublishedWorkflow("workflow1");

        assertFalse("publish workflow removed successfully", registry.isPublishedWorkflowExists("workflow1"));
        registry.removeWorkflow("workflow1");
    }


    public void testIsWorkflowExists() throws Exception {
        registry.addWorkflow("workflow1", "testContent");
        assertTrue("user workflow exists", registry.isWorkflowExists("workflow1"));
        registry.removeWorkflow("workflow1");
    }


    public void testAddWorkflow() throws Exception {
        registry.addWorkflow("workflow1", "testContent");
        assertTrue("user workflow added successfully", registry.isWorkflowExists("workflow1"));
        registry.removeWorkflow("workflow1");
    }


    public void testUpdateWorkflow() throws Exception {
        registry.addWorkflow("workflow1", "testContent1");
        registry.updateWorkflow("workflow1", "testContent2");
        assertTrue("user workflow updated successfully", registry.getWorkflowGraphXML("workflow1").equals("testContent2"));
        registry.removeWorkflow("workflow1");
    }


    public void testGetWorkflowGraphXML() throws Exception {
        registry.addWorkflow("workflow1", "testContent1");
        assertTrue("user workflow graph retrieved successfully", registry.getWorkflowGraphXML("workflow1").equals("testContent1"));
        registry.removeWorkflow("workflow1");
    }


    public void testGetWorkflows() throws Exception {
        registry.addWorkflow("workflow1", "testContent1");
        registry.addWorkflow("workflow2", "testContent2");

        Map<String, String> workflows = registry.getWorkflows();
        assertTrue("workflows retrieved successfully", workflows.size() ==2);
        registry.removeWorkflow("workflow1");
        registry.removeWorkflow("workflow2");
    }


    public void testRemoveWorkflow() throws Exception {
        registry.addWorkflow("workflow1", "testContent");
        registry.removeWorkflow("workflow1");
        assertFalse("user workflow removed successfully", registry.isWorkflowExists("workflow1"));
    }

    @Override
    protected void tearDown() throws Exception {
        initialize.stopDerbyServer();
    }
}
