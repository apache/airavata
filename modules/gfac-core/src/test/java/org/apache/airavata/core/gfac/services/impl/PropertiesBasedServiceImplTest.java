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

import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.core.gfac.context.invocation.impl.DefaultExecutionContext;
import org.apache.airavata.core.gfac.context.invocation.impl.DefaultInvocationContext;
import org.apache.airavata.core.gfac.context.message.impl.ParameterContextImpl;
import org.apache.airavata.core.gfac.notification.impl.LoggingNotification;
import org.apache.airavata.registry.api.impl.AiravataJCRRegistry;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType.ApplicationName;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.StringParameterType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PropertiesBasedServiceImplTest {
	@Before
	public void setUp() throws Exception {
		/*
		 * Create database
		 */
        Map<String,String> config = new HashMap<String,String>();
            config.put("org.apache.jackrabbit.repository.home","target");
		AiravataJCRRegistry jcrRegistry = new AiravataJCRRegistry(null,
				"org.apache.jackrabbit.core.RepositoryFactoryImpl", "admin",
				"admin", config);

		/*
		 * Host
		 */
		HostDescription host = new HostDescription();
		host.getType().setHostName("localhost");
		host.getType().setHostAddress("localhost");

		/*
		 * App
		 */
		ApplicationDeploymentDescription appDesc = new ApplicationDeploymentDescription();
		ApplicationDeploymentDescriptionType app = appDesc.getType();
		ApplicationName name = ApplicationName.Factory.newInstance();
		name.setStringValue("EchoLocal");
		app.setApplicationName(name);
		
		/*
		 * Use bat file if it is compiled on Windows
		 */
		if(SystemUtils.IS_OS_WINDOWS){
			URL url = this.getClass().getClassLoader().getResource("echo.bat");
			app.setExecutableLocation(url.getFile());
		}else{
			//for unix and Mac
			app.setExecutableLocation("/bin/echo");	
		}
		
		/*
		 * Default tmp location
		 */
		String tempDir = System.getProperty("java.io.tmpdir");
		if(tempDir == null){
			tempDir = "/tmp";
		}
		
		app.setScratchWorkingDirectory(tempDir);
		app.setStaticWorkingDirectory(tempDir);
		app.setInputDataDirectory(tempDir + File.separator + "input");
		app.setOutputDataDirectory(tempDir + File.separator + "output");
		app.setStandardOutput(tempDir + File.separator + "echo.stdout");
		app.setStandardError(tempDir + File.separator + "echo.stdout");

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

		/*
		 * Save to registry
		 */
		jcrRegistry.saveHostDescription(host);
		jcrRegistry.saveDeploymentDescription(serv.getType().getName(), host
				.getType().getHostName(), appDesc);
		jcrRegistry.saveServiceDescription(serv);
		jcrRegistry.deployServiceOnHost(serv.getType().getName(), host
				.getType().getHostName());
	}

	@Test
	public void testExecute() {
		try {

			DefaultInvocationContext ct = new DefaultInvocationContext();
			DefaultExecutionContext ec = new DefaultExecutionContext();
			ec.addNotifiable(new LoggingNotification());
			ct.setExecutionContext(ec);
             Map<String,String> config = new HashMap<String,String>();
            config.put("org.apache.jackrabbit.repository.home","target");
		    AiravataJCRRegistry jcrRegistry = new AiravataJCRRegistry(null,
				"org.apache.jackrabbit.core.RepositoryFactoryImpl", "admin",
				"admin", config);

            ec.setRegistryService(jcrRegistry);
			ct.setServiceName("SimpleEcho");

			/*
			 * Input
			 */			
			ParameterContextImpl input = new ParameterContextImpl();
			ActualParameter echo_input = new ActualParameter();
			((StringParameterType)echo_input.getType()).setValue("echo_output=hello");
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
			Assert.assertEquals("hello", ((StringParameterType)((ActualParameter)ct.getOutput().getValue("echo_output")).getType()).getValue());

		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR");
		}
	}
}
