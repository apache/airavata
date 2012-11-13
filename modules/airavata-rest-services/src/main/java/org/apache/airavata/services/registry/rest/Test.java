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

package org.apache.airavata.services.registry.rest;


import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.services.registry.rest.client.ConfigurationResourceClient;
import org.apache.airavata.services.registry.rest.client.DescriptorResourceClient;
import org.apache.airavata.services.registry.rest.resourcemappings.HostDescriptor;

import java.net.URI;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        configurationResourceClientTest();
//        descriptorClientTest();
    }

    public static void configurationResourceClientTest(){
        //configuration resource test
        ConfigurationResourceClient configurationResourceClient = new ConfigurationResourceClient();

//        System.out.println("###############getConfiguration###############");
//        Object configuration = configurationResourceClient.getConfiguration("interpreter.url");
//        System.out.println(configuration.toString());
//
//        System.out.println("###############getConfigurationList###############");
//        configurationResourceClient.addWFInterpreterURI("http://192.168.17.1:8080/axis2/services/WorkflowInterpretor2");
        List<Object> configurationList = configurationResourceClient.getConfigurationList("testKey1");
        for(Object object : configurationList){
            System.out.println(object.toString());
        }
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

    public static void descriptorClientTest(){
        DescriptorResourceClient descriptorResourceClient = new DescriptorResourceClient();

//        boolean localHost = descriptorResourceClient.isHostDescriptorExists("LocalHost");
//        System.out.println(localHost);

//        HostDescription descriptor = new HostDescription(GlobusHostType.type);
//        descriptor.getType().setHostName("testHost");
//        descriptor.getType().setHostAddress("testHostAddress2");
//        descriptorResourceClient.addHostDescriptor(descriptor);

//        HostDescription localHost = descriptorResourceClient.getHostDescriptor("purdue.teragrid.org");
        List<HostDescription> hostDescriptors = descriptorResourceClient.getHostDescriptors();
//        System.out.println(localHost.getType().getHostName());
//        System.out.println(localHost.getType().getHostAddress());

    }


}
