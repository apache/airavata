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
import org.apache.airavata.gfac.GFacAPI;
import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.ApplicationContext;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.context.security.SSHSecurityContext;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.SSHHostType;
import org.apache.airavata.schemas.gfac.StringParameterType;
import org.apache.commons.lang.SystemUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SSHProviderTest {
	private JobExecutionContext jobExecutionContext;
    @Before
    public void setUp() throws Exception {

    	URL resource = GramProviderTest.class.getClassLoader().getResource("gfac-config.xml");
        GFacConfiguration gFacConfiguration = GFacConfiguration.create(new File(resource.getPath()),null,null);
//        gFacConfiguration.s
        //have to set InFlwo Handlers and outFlowHandlers
        ApplicationContext applicationContext = new ApplicationContext();
        HostDescription host = new HostDescription(SSHHostType.type);
        host.getType().setHostName("gw85.iu.xsede.org");
        host.getType().setHostAddress("gw85.iu.xsede.org");
        applicationContext.setHostDescription(host);
        /*
           * App
           */
        ApplicationDescription appDesc = new ApplicationDescription();
        ApplicationDeploymentDescriptionType app = appDesc.getType();
        ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
        name.setStringValue("EchoSSH");
        app.setApplicationName(name);

        /*
           * Use bat file if it is compiled on Windows
           */
        if (SystemUtils.IS_OS_WINDOWS) {
            URL url = this.getClass().getClassLoader().getResource("echo.bat");
            app.setExecutableLocation(url.getFile());
        } else {
            //for unix and Mac
            app.setExecutableLocation("/bin/echo");
        }

        /*
         * Job location
        */
        String tempDir = "/tmp";
        String date = (new Date()).toString();
        date = date.replaceAll(" ", "_");
        date = date.replaceAll(":", "_");

        tempDir = tempDir + File.separator
                + "EchoSSH" + "_" + date + "_" + UUID.randomUUID();

        app.setScratchWorkingDirectory(tempDir);
        app.setStaticWorkingDirectory(tempDir);
        app.setInputDataDirectory(tempDir + File.separator + "input");
        app.setOutputDataDirectory(tempDir + File.separator + "output");
        app.setStandardOutput(tempDir + File.separator + "echo.stdout");
        app.setStandardError(tempDir + File.separator + "echo.stderr");

        applicationContext.setApplicationDeploymentDescription(appDesc);

        /*
           * Service
           */
        ServiceDescription serv = new ServiceDescription();
        serv.getType().setName("EchoSSH");

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

        jobExecutionContext = new JobExecutionContext(gFacConfiguration,serv.getType().getName());
        jobExecutionContext.setApplicationContext(applicationContext);

        // Add security context
        jobExecutionContext.addSecurityContext(SSHSecurityContext.SSH_SECURITY_CONTEXT, getSecurityContext());
        /*
        * Host
        */
        applicationContext.setServiceDescription(serv);

        MessageContext inMessage = new MessageContext();
        ActualParameter echo_input = new ActualParameter();
		((StringParameterType)echo_input.getType()).setValue("echo_output=hello");
        inMessage.addParameter("echo_input", echo_input);

        jobExecutionContext.setInMessageContext(inMessage);

        MessageContext outMessage = new MessageContext();
        ActualParameter echo_out = new ActualParameter();
//		((StringParameterType)echo_input.getType()).setValue("echo_output=hello");
        outMessage.addParameter("echo_output", echo_out);

        jobExecutionContext.setOutMessageContext(outMessage);

    }

	private SSHSecurityContext getSecurityContext() {
		SSHSecurityContext context = new SSHSecurityContext();
        context.setUsername("*****");
        context.setPrivateKeyLoc("~/.ssh/id_dsa");
        context.setKeyPass("*****");
		return context;
	}

    @Test
    public void testLocalProvider() throws GFacException {
        GFacAPI gFacAPI = new GFacAPI();
        gFacAPI.submitJob(jobExecutionContext);
        MessageContext outMessageContext = jobExecutionContext.getOutMessageContext();
        Assert.assertEquals(MappingFactory.toString((ActualParameter)outMessageContext.getParameter("echo_output")), "hello");
    }
}
