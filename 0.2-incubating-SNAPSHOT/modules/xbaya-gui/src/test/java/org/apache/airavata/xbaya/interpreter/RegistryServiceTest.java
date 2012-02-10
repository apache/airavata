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

package org.apache.airavata.xbaya.interpreter;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.exception.ServiceDescriptionRetrieveException;
import org.apache.airavata.registry.api.impl.JCRRegistry;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.StringParameterType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RegistryServiceTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    public JCRRegistry jcrRegistry = null;

    @Before
    public void testExecute() throws RegistryException {

        try {
            jcrRegistry = new JCRRegistry(null, "org.apache.jackrabbit.core.RepositoryFactoryImpl", "admin", "admin",
                    null);
        } catch (RepositoryException e) {
            fail("Failed creating the JCR Registry");
        }

        HostDescription host = createHostDescription();
        ApplicationDeploymentDescription appDesc = createAppDeploymentDescription();
        ServiceDescription serv = createServiceDescription();

        List<InputParameterType> inputList = new ArrayList<InputParameterType>();
        InputParameterType input = InputParameterType.Factory.newInstance();
        input.setParameterName("echo_input");
        input.setParameterType(StringParameterType.Factory.newInstance());
        inputList.add(input);
        InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList.size()]);

        List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();
        OutputParameterType output = OutputParameterType.Factory.newInstance();
        output.setParameterName("echo_output");
        input.setParameterType(StringParameterType.Factory.newInstance());
        outputList.add(output);
        OutputParameterType[] outputParamList = outputList.toArray(new OutputParameterType[outputList.size()]);

        serv.getType().setInputParametersArray(inputParamList);
        serv.getType().setOutputParametersArray(outputParamList);

        jcrRegistry.saveHostDescription(host);
        jcrRegistry.saveDeploymentDescription(serv.getType().getName(), host.getType().getHostName(), appDesc);
        jcrRegistry.saveServiceDescription(serv);
        jcrRegistry.deployServiceOnHost(serv.getType().getName(), host.getType().getHostName());

    }

    private HostDescription createHostDescription() {
        HostDescription host = new HostDescription();
        host.getType().setHostName("localhost");
        host.getType().setHostAddress("localhost");
        return host;
    }

    private ServiceDescription createServiceDescription() {
        ServiceDescription serv = new ServiceDescription();
        serv.getType().setName("SimpleEcho");
        return serv;
    }

    private ApplicationDeploymentDescription createAppDeploymentDescription() {
        ApplicationDeploymentDescription appDesc = new ApplicationDeploymentDescription();
        ApplicationDeploymentDescriptionType app = appDesc.getType();
        ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory
                .newInstance();
        name.setStringValue("EchoLocal");
        app.setApplicationName(name);
        app.setExecutableLocation("/bin/echo");
        app.setScratchWorkingDirectory("/tmp");
        app.setStaticWorkingDirectory("/tmp");
        app.setInputDataDirectory("/tmp/input");
        app.setOutputDataDirectory("/tmp/output");
        app.setStandardOutput("/tmp/echo.stdout");
        app.setStandardError("/tmp/echo.stdout");
        return appDesc;
    }

    @Test
    public void getFromRegistry() throws RegistryException {
        /* Checking the registry for the saved descriptors */
        exception.expect(ServiceDescriptionRetrieveException.class);
        assertNotNull(jcrRegistry.getHostDescription("localhost"));
        assertNull(jcrRegistry.getHostDescription("remotehost"));

        assertNull(jcrRegistry.getDeploymentDescription("random1", "random2"));
        assertNotNull(jcrRegistry.getServiceDescription("SimpleEcho"));
        assertNull(jcrRegistry.getServiceDescription("dummyService"));
    }

    @Test
    public void searchRegistry() throws RegistryException {
        /* Searching the registry for descriptors */
        assertNotNull(jcrRegistry.searchHostDescription("localhost"));
        assertNotNull(jcrRegistry.searchDeploymentDescription("EchoLocal", "localhost"));
        assertNotNull(jcrRegistry.searchServiceDescription("SimpleEcho"));
    }

    @Test
    public void deleteFromRegistry() throws RegistryException {
        /* Deleting the descriptors from the registry */
        exception.expect(ServiceDescriptionRetrieveException.class);
        jcrRegistry.deleteHostDescription("localhost");
        assertNull(jcrRegistry.getHostDescription("localhost"));
        assertNull(jcrRegistry.getHostDescription("remotehost"));

        jcrRegistry.deleteDeploymentDescription("SimpleEcho", "localhost", "EchoLocal");
        assertNull(jcrRegistry.getDeploymentDescription("EchoLocal", "localhost"));
        assertNull(jcrRegistry.getDeploymentDescription("EchoLocal", "remotehost"));

        jcrRegistry.deleteServiceDescription("SimpleEcho");
        jcrRegistry.getServiceDescription("SimpleEcho");
        assertNull(jcrRegistry.getServiceDescription("dummyService"));
    }
}
