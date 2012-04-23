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
import org.apache.airavata.registry.api.impl.AiravataJCRRegistry;
import org.apache.airavata.schemas.gfac.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.*;

import static org.junit.Assert.fail;

public class FileBreedGramTest {

    public static final String MYPROXY = "myproxy";
    public static final String GRAM_PROPERTIES = "gram.properties";
    private AiravataJCRRegistry jcrRegistry = null;

    @Before
    public void setUp() throws Exception {
        /*
        * Create database
        */
        Map<String,String> config = new HashMap<String,String>();
            config.put("org.apache.jackrabbit.repository.home","target");

        jcrRegistry = new AiravataJCRRegistry(null,
                "org.apache.jackrabbit.core.RepositoryFactoryImpl", "admin",
                "admin", config);
    
        /*
        * Host Description Document
        */

        URL url = this.getClass().getClassLoader().getResource(GRAM_PROPERTIES);
        Properties properties = new Properties();
        properties.load(url.openStream());
        HostDescription host = new HostDescription();
        host.getType().changeType(GlobusHostType.type);
        host.getType().setHostName(properties.getProperty("host.commom.name"));
        host.getType().setHostAddress(properties.getProperty("host.fqdn.name"));
        ((GlobusHostType) host.getType()).setGridFTPEndPointArray(new String[]{properties.getProperty("gridftp.endpoint")});
        ((GlobusHostType) host.getType()).setGlobusGateKeeperEndPointArray(new String[]{properties.getProperty("gram.endpoints")});


        /*
        * Application deployment description
        */
        ApplicationDeploymentDescription appDesc = new ApplicationDeploymentDescription(GramApplicationDeploymentType.type);
        GramApplicationDeploymentType app = (GramApplicationDeploymentType) appDesc.getType();
        app.setCpuCount(1);
        app.setNodeCount(1);
        ApplicationDeploymentDescriptionType.ApplicationName name = appDesc.getType().addNewApplicationName();
        name.setStringValue("FileBreed");
        app.setExecutableLocation("/bin/echo");
        app.setScratchWorkingDirectory(properties.getProperty("scratch.working.directory"));
        app.setCpuCount(1);
        ProjectAccountType projectAccountType = ((GramApplicationDeploymentType) appDesc.getType()).addNewProjectAccount();
        projectAccountType.setProjectAccountNumber(properties.getProperty("allocation.charge.number"));
        QueueType queueType = app.addNewQueue();
        queueType.setQueueName(properties.getProperty("defualt.queue"));
        
        /*
        * Application Service
        */
        ServiceDescription serv = new ServiceDescription();
        serv.getType().setName("FileBreedTest");

        InputParameterType inputParameter = InputParameterType.Factory.newInstance();
        inputParameter.setParameterName("Input_File");
        inputParameter.setParameterDescription("File to Replicate");
        ParameterType inputParameterType = inputParameter.addNewParameterType();
        inputParameterType.setName(DataType.URI.toString());
        inputParameterType.setType(DataType.URI);

//        parameterType.setType("");
        List<InputParameterType> inputList = new ArrayList<InputParameterType>();
        inputList.add(inputParameter);
        InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList
                .size()]);
        
        OutputParameterType outputParameter = OutputParameterType.Factory.newInstance();
        ParameterType outputParameterType = outputParameter.addNewParameterType();
        outputParameterType.setName("replicated_file");
        outputParameterType.setType(DataType.URI);
        List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();
        outputList.add(outputParameter);
        OutputParameterType[] outputParamList = outputList
                .toArray(new OutputParameterType[outputList.size()]);
        serv.getType().setInputParametersArray(inputParamList);
        serv.getType().setOutputParametersArray(outputParamList);

        /*
        * Save deployment descriptions to registry
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
            ec.setRegistryService(jcrRegistry);
            ct.setExecutionContext(ec);


            GSISecurityContext gsiSecurityContext = new GSISecurityContext();
            gsiSecurityContext.setMyproxyServer(properties.getProperty("myproxy.server"));
            gsiSecurityContext.setMyproxyUserName(properties.getProperty("myproxy.username"));
            gsiSecurityContext.setMyproxyPasswd(properties.getProperty("myproxy.password"));
            gsiSecurityContext.setMyproxyLifetime(14400);
            gsiSecurityContext.setTrustedCertLoc(properties.getProperty("ca.certificates.directory"));

            ct.addSecurityContext(MYPROXY, gsiSecurityContext);

            ct.setServiceName("FileBreedTest");

            /*
            * Input
            */
            ParameterContextImpl input = new ParameterContextImpl();
            ActualParameter input_file = new ActualParameter();
            String InputFile = "/gpfs1/u/ac/ccguser/alatop.inp";
            ((StringParameterType) input_file.getType()).setValue(InputFile);
            input.add("input_file", input_file);

            /*
            * Output
            */
            ParameterContextImpl output = new ParameterContextImpl();
            ActualParameter replicated_file = new ActualParameter();
            output.add("replicated_file", replicated_file);

            // parameter
            ct.setInput(input);
            ct.setOutput(output);

            PropertiesBasedServiceImpl service = new PropertiesBasedServiceImpl();
            service.init();
            service.execute(ct);

            Assert.assertNotNull(ct.getOutput());

        } catch (Exception e) {
            e.printStackTrace();
            fail("ERROR");
        }
    }
}
