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
package org.apache.airavata.core.gfac.services.impl;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacAPI;
import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.ApplicationContext;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.context.security.GSISecurityContext;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.JobTypeType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.ProjectAccountType;
import org.apache.airavata.schemas.gfac.QueueType;
import org.apache.airavata.schemas.gfac.StringParameterType;
import org.apache.airavata.schemas.gfac.URIParameterType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GramProviderTest {
    private JobExecutionContext jobExecutionContext;


//    private static final String hostAddress = "blacklight.psc.teragrid.org";
//    private static final String hostName = "Blacklight";
//    private static final String gridftpAddress = "gsiftp://gridftp.blacklight.psc.teragrid.org:2812";
//    private static final String gramAddress = "";

    //FIXME: move job properties to configuration file
    private static final String hostAddress = "lonestar.tacc.teragrid.org";
    private static final String hostName = "lonestar";
    private static final String gridftpAddress = "gsiftp://gridftp1.ls4.tacc.utexas.edu:2811";
    private static final String gramAddress = "gridftp1.ls4.tacc.utexas.edu:2119/jobmanager-sge";

    @Before
    public void setUp() throws Exception {
        URL resource = GramProviderTest.class.getClassLoader().getResource("gfac-config.xml");
        System.out.println(resource.getFile());
        GFacConfiguration gFacConfiguration = GFacConfiguration.create(new File(resource.getPath()),null,null);
//        gFacConfiguration.setMyProxyLifeCycle(3600);
//        gFacConfiguration.setMyProxyServer("myproxy.teragrid.org");
//        gFacConfiguration.setMyProxyUser("*****");
//        gFacConfiguration.setMyProxyPassphrase("*****");
//        gFacConfiguration.setTrustedCertLocation("./certificates");
//        //have to set InFlwo Handlers and outFlowHandlers
//        gFacConfiguration.setInHandlers(Arrays.asList(new String[] {"org.apache.airavata.gfac.handler.GramDirectorySetupHandler","org.apache.airavata.gfac.handler.GridFTPInputHandler"}));
//        gFacConfiguration.setOutHandlers(Arrays.asList(new String[] {"org.apache.airavata.gfac.handler.GridFTPOutputHandler"}));

        /*
           * Host
           */
        HostDescription host = new HostDescription(GlobusHostType.type);
        host.getType().setHostAddress(hostAddress);
        host.getType().setHostName(hostName);
        ((GlobusHostType)host.getType()).setGlobusGateKeeperEndPointArray(new String[]{gramAddress});
        ((GlobusHostType)host.getType()).setGridFTPEndPointArray(new String[]{gridftpAddress});
        /*
           * App
           */
        ApplicationDescription appDesc = new ApplicationDescription(HpcApplicationDeploymentType.type);
        HpcApplicationDeploymentType app = (HpcApplicationDeploymentType)appDesc.getType();
        ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
        name.setStringValue("EchoLocal");
        app.setApplicationName(name);
        ProjectAccountType projectAccountType = app.addNewProjectAccount();
        projectAccountType.setProjectAccountNumber("TG-STA110014S");

        QueueType queueType = app.addNewQueue();
        queueType.setQueueName("development");

        app.setCpuCount(1);
        app.setJobType(JobTypeType.SERIAL);
        app.setNodeCount(1);
        app.setProcessorsPerNode(1);

        /*
           * Use bat file if it is compiled on Windows
           */
        app.setExecutableLocation("/bin/echo");

        /*
           * Default tmp location
           */
        String tempDir = "/scratch/01437/ogce/test/";
        String date = (new Date()).toString();
        date = date.replaceAll(" ", "_");
        date = date.replaceAll(":", "_");

        tempDir = tempDir + File.separator
                + "SimpleEcho" + "_" + date + "_" + UUID.randomUUID();

        System.out.println(tempDir);
        app.setScratchWorkingDirectory(tempDir);
        app.setStaticWorkingDirectory(tempDir);
        app.setInputDataDirectory(tempDir + File.separator + "inputData");
        app.setOutputDataDirectory(tempDir + File.separator + "outputData");
        app.setStandardOutput(tempDir + File.separator + app.getApplicationName().getStringValue() + ".stdout");
        app.setStandardError(tempDir + File.separator + app.getApplicationName().getStringValue() + ".stderr");


        /*
           * Service
           */
        ServiceDescription serv = new ServiceDescription();
        serv.getType().setName("SimpleEcho");

        List<InputParameterType> inputList = new ArrayList<InputParameterType>();

        InputParameterType input = InputParameterType.Factory.newInstance();
        input.setParameterName("echo_input");
        input.setParameterType(StringParameterType.Factory.newInstance());
        inputList.add(input);

        InputParameterType input1 = InputParameterType.Factory.newInstance();
        input.setParameterName("myinput");
        URIParameterType uriType = URIParameterType.Factory.newInstance();
        uriType.setValue("gsiftp://gridftp1.ls4.tacc.utexas.edu:2811//home1/01437/ogce/gram_20130215.log");
        input.setParameterType(uriType);
        inputList.add(input1);


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

        jobExecutionContext = new JobExecutionContext(gFacConfiguration,serv.getType().getName());
        // Adding security context
        jobExecutionContext.addSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT, getSecurityContext());
        ApplicationContext applicationContext = new ApplicationContext();
        jobExecutionContext.setApplicationContext(applicationContext);
        applicationContext.setServiceDescription(serv);
        applicationContext.setApplicationDeploymentDescription(appDesc);
        applicationContext.setHostDescription(host);

        MessageContext inMessage = new MessageContext();
        ActualParameter echo_input = new ActualParameter();
        ((StringParameterType)echo_input.getType()).setValue("echo_output=hello");
        inMessage.addParameter("echo_input", echo_input);

        // added extra
        ActualParameter copy_input = new ActualParameter();
        copy_input.getType().changeType(URIParameterType.type);
        ((URIParameterType)copy_input.getType()).setValue("file:///tmp/tmpstrace");

        ActualParameter outlocation = new ActualParameter();
        ((StringParameterType)outlocation.getType()).setValue("./outputData/.");
        inMessage.addParameter("copy_input", copy_input);
        inMessage.addParameter("outputlocation", outlocation);

        // added extra



        jobExecutionContext.setInMessageContext(inMessage);

        MessageContext outMessage = new MessageContext();
        ActualParameter echo_out = new ActualParameter();
//		((StringParameterType)echo_input.getType()).setValue("echo_output=hello");
        outMessage.addParameter("echo_output", echo_out);

        jobExecutionContext.setOutMessageContext(outMessage);

    }

	private GSISecurityContext getSecurityContext() {
		GSISecurityContext context = new GSISecurityContext("myproxy.teragrid.org", "xxx", "xxx", 3600, "/Users/path");
		return context;
	}

    @Test
    public void testGramProvider() throws GFacException {
        GFacAPI gFacAPI = new GFacAPI();
        gFacAPI.submitJob(jobExecutionContext);
        MessageContext outMessageContext = jobExecutionContext.getOutMessageContext();
        Assert.assertEquals(MappingFactory.toString((ActualParameter)outMessageContext.getParameter("echo_output")), "hello");
    }
}
