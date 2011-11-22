/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of4 the License at
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
package org.apache.airavata.test.suite.gfac;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.core.gfac.context.invocation.impl.DefaultExecutionContext;
import org.apache.airavata.core.gfac.context.invocation.impl.DefaultInvocationContext;
import org.apache.airavata.core.gfac.context.message.impl.ParameterContextImpl;
import org.apache.airavata.core.gfac.context.security.impl.GSISecurityContext;
import org.apache.airavata.core.gfac.notification.impl.LoggingNotification;
import org.apache.airavata.core.gfac.services.impl.PropertiesBasedServiceImpl;
import org.apache.airavata.registry.api.impl.JCRRegistry;
import org.apache.airavata.schemas.gfac.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.fail;

public class GramProviderTest {

    public static final String MYPROXY = "myproxy";
    public static final String GRAM_PROPERTIES = "gram.properties";

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

        URL url = this.getClass().getClassLoader().getResource(GRAM_PROPERTIES);
        Properties properties = new Properties();
        properties.load(url.openStream());
        HostDescription host = new HostDescription();
        host.getType().changeType(GlobusHostType.type);
        host.getType().setHostName(properties.getProperty("gram.name"));
        host.getType().setHostAddress(properties.getProperty("gram.host"));
        ((GlobusHostType) host.getType()).setGridFTPEndPointArray(new String[]{properties.getProperty("gridftp.endpoint")});
        ((GlobusHostType) host.getType()).setGlobusGateKeeperEndPointArray(new String[]{properties.getProperty("globus.endpoints")});


        /*
        * App
        */
        ApplicationDeploymentDescription appDesc = new ApplicationDeploymentDescription(GramApplicationDeploymentType.type);
        GramApplicationDeploymentType app = (GramApplicationDeploymentType) appDesc.getType();
        app.setCpuCount(1);
        app.setNodeCount(1);
        ApplicationDeploymentDescriptionType.ApplicationName name = appDesc.getType().addNewApplicationName();
        name.setStringValue("EchoLocal");
        app.setExecutableLocation("/bin/echo");
        app.setScratchWorkingDirectory(properties.getProperty("scratch.directory"));
        app.setCpuCount(1);
        ProjectAccountType projectAccountType = ((GramApplicationDeploymentType) appDesc.getType()).addNewProjectAccount();
        projectAccountType.setProjectAccountNumber(properties.getProperty("project.name"));

        /*
           * Service
           */
        ServiceDescription serv = new ServiceDescription();
        serv.getType().setName("SimpleEcho");

        InputParameterType input = InputParameterType.Factory.newInstance();
        ParameterType parameterType = input.addNewParameterType();
        parameterType.setName("echo_input");
        List<InputParameterType> inputList = new ArrayList<InputParameterType>();
        inputList.add(input);
        InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList
                .size()]);

        OutputParameterType output = OutputParameterType.Factory.newInstance();
        ParameterType parameterType1 = output.addNewParameterType();
        parameterType1.setName("echo_output");
        List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();
        outputList.add(output);
        OutputParameterType[] outputParamList = outputList
                .toArray(new OutputParameterType[outputList.size()]);
        serv.getType().setInputParametersArray(inputParamList);
        serv.getType().setOutputParametersArray(outputParamList);

        /*
           * Save to registry
           */
        jcrRegistry.saveHostDescription(host);
        jcrRegistry.saveDeploymentDescription(serv.getType().getName(), host.getType().getHostName(), appDesc);
        jcrRegistry.saveServiceDescription(serv);
        jcrRegistry.deployServiceOnHost(serv.getType().getName(), host.getType().getHostName());
    }

    @Test
    public void testExecute() {
        try {
            URL url = this.getClass().getClassLoader().getResource(GRAM_PROPERTIES);
            Properties properties = new Properties();
            properties.load(url.openStream());

            DefaultInvocationContext ct = new DefaultInvocationContext();
            DefaultExecutionContext ec = new DefaultExecutionContext();
            ec.addNotifiable(new LoggingNotification());
            ct.setExecutionContext(ec);


            GSISecurityContext gsiSecurityContext = new GSISecurityContext();
            gsiSecurityContext.setMyproxyServer(properties.getProperty("myproxy.server"));
            gsiSecurityContext.setMyproxyUserName(properties.getProperty("myproxy.username"));
            gsiSecurityContext.setMyproxyPasswd(properties.getProperty("myproxy.password"));
            gsiSecurityContext.setMyproxyLifetime(14400);
            gsiSecurityContext.setTrustedCertLoc(properties.getProperty("certificate.path"));

            ct.addSecurityContext(MYPROXY, gsiSecurityContext);

            ct.setServiceName("SimpleEcho");

            /*
            * Input
            */
            ParameterContextImpl input = new ParameterContextImpl();
            ActualParameter echo_input = new ActualParameter();
            ((StringParameterType) echo_input.getType()).setValue("echo_output=hello");
            input.add("echo_input", echo_input);

            /*
            * Output
            */
            ParameterContextImpl output = new ParameterContextImpl();
            ActualParameter echo_output = new ActualParameter();
            output.add("echo_output", echo_output);

            // parameter
            ct.setInput(input);
            ct.setOutput(output);

            PropertiesBasedServiceImpl service = new PropertiesBasedServiceImpl();
            service.init();
            service.execute(ct);

            Assert.assertNotNull(ct.getOutput());
            Assert.assertNotNull(ct.getOutput().getValue("echo_output"));
            Assert.assertEquals("hello", ((StringParameterType) ((ActualParameter) ct.getOutput().getValue("echo_output")).getType()).getValue());


        } catch (Exception e) {
            e.printStackTrace();
            fail("ERROR");
        }
    }
}
