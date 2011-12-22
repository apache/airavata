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
package org.apache.airavata.registry.api.impl;

import junit.framework.Assert;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.StringParameterType;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

public class JCRRegistrySearchTest {
    @Before
    public void setUp() throws Exception {
        /*
        * Create database
        */
        JCRRegistry jcrRegistry = new JCRRegistry(null,
                "org.apache.jackrabbit.core.RepositoryFactoryImpl", "admin",
                "admin", null);

        /*
        * Host
        */
        HostDescription host = new HostDescription();
        host.getType().setHostName("localhostsearch");
        host.getType().setHostAddress("localhost");

        HostDescription host1 = new HostDescription();
        host1.getType().setHostName("localhost");
        host1.getType().setHostAddress("121.121.12.121");

        /*
        * App
        */
        ApplicationDeploymentDescription appDesc = new ApplicationDeploymentDescription();
        ApplicationDeploymentDescriptionType app = appDesc.getType();
        ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
        name.setStringValue("EchoLocalSearch");
        app.setApplicationName(name);
        app.setExecutableLocation("/bin/echo");
        app.setScratchWorkingDirectory("/tmp");
        app.setStaticWorkingDirectory("/tmp");
        app.setInputDataDirectory("/tmp/input");
        app.setOutputDataDirectory("/tmp/output");
        app.setStandardOutput("/tmp/echo.stdout");
        app.setStandardError("/tmp/echo.stdout");

        /*
        * Service
        */
        ServiceDescription serv = new ServiceDescription();
        serv.getType().setName("SimpleEchoSearch");

        ServiceDescription serv1 = new ServiceDescription();
        serv1.getType().setName("MathService");

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

        serv1.getType().setInputParametersArray(inputParamList);
        serv1.getType().setOutputParametersArray(outputParamList);

        /*
        * Save to registry
        */
        jcrRegistry.saveHostDescription(host);
        jcrRegistry.saveHostDescription(host1);
        jcrRegistry.saveDeploymentDescription(serv.getType().getName(), host
                .getType().getHostName(), appDesc);
        jcrRegistry.saveDeploymentDescription(serv1.getType().getName(), host
                        .getType().getHostName(), appDesc);
        jcrRegistry.saveDeploymentDescription(serv1.getType().getName(), host1
                        .getType().getHostName(), appDesc);
        jcrRegistry.saveServiceDescription(serv);
        jcrRegistry.saveServiceDescription(serv1);
        jcrRegistry.deployServiceOnHost(serv.getType().getName(), host
                .getType().getHostName());
        jcrRegistry.deployServiceOnHost(serv1.getType().getName(), host
                .getType().getHostName());
         jcrRegistry.deployServiceOnHost(serv1.getType().getName(), host1
                .getType().getHostName());

    }

    @Test
    public void searchServiceDescriptionTest() {
        try {
            JCRRegistry jcrRegistry = new JCRRegistry(null,
                   "org.apache.jackrabbit.core.RepositoryFactoryImpl", "admin",
                   "admin", null);
            List<ServiceDescription> simpleEcho = jcrRegistry.searchServiceDescription("SimpleEchoSearch");
            if(simpleEcho.size() == 0){
                Assert.assertTrue(false);
            }else{
                Assert.assertEquals("SimpleEchoSearch",simpleEcho.get(0).getType().getName());
            }
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Assert.assertTrue(true);
    }

     @Test
    public void searchDeploymentDescriptorTest() {
        try {
            JCRRegistry jcrRegistry = new JCRRegistry(null,
                   "org.apache.jackrabbit.core.RepositoryFactoryImpl", "admin",
                   "admin", null);
            jcrRegistry.searchDeploymentDescription()
            List<ServiceDescription> simpleEcho = jcrRegistry.searchServiceDescription("SimpleEchoSearch");
            if(simpleEcho.size() == 0){
                Assert.assertTrue(false);
            }else{
                Assert.assertEquals("SimpleEchoSearch",simpleEcho.get(0).getType().getName());
            }
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Assert.assertTrue(true);
    }
}
