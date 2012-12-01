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

import static org.junit.Assert.fail;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
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
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.JobTypeType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.ParameterType;
import org.apache.airavata.schemas.gfac.ProjectAccountType;
import org.apache.airavata.schemas.gfac.StringParameterType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GramProviderMPIRangerTest {

    public static final String MYPROXY = "myproxy";
    public static final String GRAM_PROPERTIES = "gram-ranger.properties";
    private AiravataAPI airavataAPI = null;

    @Before
    public void setUp() throws Exception {
        Map<String,String> config = new HashMap<String,String>();
            config.put("org.apache.jackrabbit.repository.home","target");

        airavataAPI=AiravataAPIFactory.getAPI("default","admin");


        // Host
        URL url = this.getClass().getClassLoader().getResource(GRAM_PROPERTIES);
        Properties properties = new Properties();
        properties.load(url.openStream());
        HostDescription host = new HostDescription();
        host.getType().changeType(GlobusHostType.type);
        host.getType().setHostName(properties.getProperty("host.commom.name"));
        host.getType().setHostAddress(properties.getProperty("host.fqdn.name"));
        ((GlobusHostType) host.getType()).setGridFTPEndPointArray(new String[]{properties.getProperty("gridftp.endpoint")});
        ((GlobusHostType) host.getType()).setGlobusGateKeeperEndPointArray(new String[]{properties.getProperty("gram.endpoints")});

        /* Application */
        ApplicationDeploymentDescription appDesc = new ApplicationDeploymentDescription(HpcApplicationDeploymentType.type);
        HpcApplicationDeploymentType app = (HpcApplicationDeploymentType) appDesc.getType();
        app.setCpuCount(1);
        app.setNodeCount(1);
        ApplicationDeploymentDescriptionType.ApplicationName name = appDesc.getType().addNewApplicationName();
        name.setStringValue("EchoMPILocal");
        app.setExecutableLocation("/share/home/01437/ogce/airavata-test/mpi-hellow-world");
        app.setScratchWorkingDirectory(properties.getProperty("scratch.working.directory"));
        app.setCpuCount(16);
        app.setJobType(JobTypeType.MPI);
        //app.setMinMemory();
        ProjectAccountType projectAccountType = ((HpcApplicationDeploymentType) appDesc.getType()).addNewProjectAccount();
        projectAccountType.setProjectAccountNumber(properties.getProperty("allocation.charge.number"));

        /* Service */
        ServiceDescription serv = new ServiceDescription();
        serv.getType().setName("SimpleMPIEcho");

        InputParameterType input = InputParameterType.Factory.newInstance();
        ParameterType parameterType = input.addNewParameterType();
        parameterType.setName("echo_mpi_input");
        List<InputParameterType> inputList = new ArrayList<InputParameterType>();
        inputList.add(input);
        InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList
                .size()]);

        OutputParameterType output = OutputParameterType.Factory.newInstance();
        ParameterType parameterType1 = output.addNewParameterType();
        parameterType1.setName("echo_mpi_output");
        List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();
        outputList.add(output);
        OutputParameterType[] outputParamList = outputList
                .toArray(new OutputParameterType[outputList.size()]);
        serv.getType().setInputParametersArray(inputParamList);
        serv.getType().setOutputParametersArray(outputParamList);

        /* Save to Registry */
        airavataAPI.getApplicationManager().saveHostDescription(host);
        airavataAPI.getApplicationManager().saveDeploymentDescription(serv.getType().getName(), host.getType().getHostName(), appDesc);
        airavataAPI.getApplicationManager().saveServiceDescription(serv);
//        jcrRegistry.deployServiceOnHost(serv.getType().getName(), host.getType().getHostName());
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
            ec.setRegistryService(airavataAPI);
            ct.setExecutionContext(ec);


            GSISecurityContext gsiSecurityContext = new GSISecurityContext();
            gsiSecurityContext.setMyproxyServer(properties.getProperty("myproxy.server"));
            gsiSecurityContext.setMyproxyUserName(properties.getProperty("myproxy.username"));
            gsiSecurityContext.setMyproxyPasswd(properties.getProperty("myproxy.password"));
            gsiSecurityContext.setMyproxyLifetime(14400);
            gsiSecurityContext.setTrustedCertLoc(properties.getProperty("ca.certificates.directory"));

            ct.addSecurityContext(MYPROXY, gsiSecurityContext);

            ct.setServiceName("SimpleMPIEcho");

            /* Input */
            ParameterContextImpl input = new ParameterContextImpl();
            ActualParameter echo_input = new ActualParameter();
            ((StringParameterType) echo_input.getType()).setValue("echo_mpi_output=hi");
            input.add("echo_mpi_input", echo_input);

            /* Output */
            ParameterContextImpl output = new ParameterContextImpl();
            ActualParameter echo_output = new ActualParameter();
            output.add("echo_mpi_output", echo_output);

            /* parameter */
            ct.setInput(input);
            ct.setOutput(output);

            PropertiesBasedServiceImpl service = new PropertiesBasedServiceImpl();
            service.init();
            service.execute(ct);

            System.out.println("output              : " + ct.getOutput().toString());
            System.out.println("output from service : " + ct.getOutput().getValue("echo_mpi_output"));

            Assert.assertNotNull(ct.getOutput());
            Assert.assertNotNull(ct.getOutput().getValue("echo_mpi_output"));

            System.out.println("output              : " + ((StringParameterType) ((ActualParameter) ct.getOutput().getValue("echo_mpi_output")).getType()).getValue());

        } catch (Exception e) {
            e.printStackTrace();
            fail("ERROR");
        }
    }
}
