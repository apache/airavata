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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.airavata.commons.gfac.api.impl.JCRRegistry;
import org.apache.airavata.commons.gfac.type.DataType;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.Parameter;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.commons.gfac.type.app.ShellApplicationDeployment;
import org.apache.airavata.commons.gfac.type.parameter.AbstractParameter;
import org.apache.airavata.commons.gfac.type.parameter.StringParameter;
import org.apache.airavata.core.gfac.context.invocation.impl.DefaultExecutionContext;
import org.apache.airavata.core.gfac.context.invocation.impl.DefaultInvocationContext;
import org.apache.airavata.core.gfac.context.message.impl.ParameterContextImpl;
import org.apache.airavata.core.gfac.notification.impl.LoggingNotification;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PropertiesBasedServiceImplTest {
	@Before
	public void setUp() throws Exception {
		/*
		 * Create database
		 */
		JCRRegistry jcrRegistry = new JCRRegistry(
				"org.apache.jackrabbit.core.RepositoryFactoryImpl", "admin",
				"admin", null);

		/*
		 * Host
		 */
		HostDescription host = new HostDescription();
		host.setName("localhost");

		/*
		 * App
		 */
		ShellApplicationDeployment app = new ShellApplicationDeployment();
		app.setName("EchoLocal");
		app.setExecutable("/bin/echo");
		app.setTmpDir("/tmp");
		app.setWorkingDir("/tmp");
		app.setInputDir("/tmp/input");
		app.setOutputDir("/tmp/output");
		app.setStdOut("/tmp/echo.stdout");
		app.setStdErr("/tmp/echo.stdout");
		app.setEnv(new HashMap<String, String>());

		/*
		 * Service
		 */
		ServiceDescription serv = new ServiceDescription();
		serv.setName("SimpleEcho");

		Parameter input = new Parameter();
		input.setName("echo_input");
		input.setType(DataType.String);
		List<Parameter> inputList = new ArrayList<Parameter>();
		inputList.add(input);

		Parameter output = new Parameter();
		output.setName("echo_output");
		output.setType(DataType.String);
		List<Parameter> outputList = new ArrayList<Parameter>();
		outputList.add(output);

		serv.setInputParameters(inputList);
		serv.setOutputParameters(outputList);

		/*
		 * Save to registry
		 */
		jcrRegistry.saveHostDescription(host.getName(), host);
		jcrRegistry.saveDeploymentDescription(serv.getName(), host.getName(),
				app);
		jcrRegistry.saveServiceDescription(serv.getName(), serv);
		jcrRegistry.deployServiceOnHost(serv.getName(), host.getName());
	}

	@Test
	public void testExecute() {
		try {

		    DefaultInvocationContext ct = new DefaultInvocationContext();
			DefaultExecutionContext ec = new DefaultExecutionContext();
			ec.addNotifiable(new LoggingNotification());
			ct.setExecutionContext(ec);

			ct.setServiceName("SimpleEcho");

			/*
			 * Input
			 */
			ParameterContextImpl input = new ParameterContextImpl();
			StringParameter echo_input = new StringParameter();
			echo_input.parseStringVal("echo_output=hello");
			input.add("echo_input", echo_input);

			/*
			 * Output
			 */
			ParameterContextImpl output = new ParameterContextImpl();
			StringParameter echo_output = new StringParameter();
			output.add("echo_output", echo_output);

			// parameter
			ct.setInput(input);
			ct.setOutput(output);

			PropertiesBasedServiceImpl service = new PropertiesBasedServiceImpl();
			service.init();
			service.execute(ct);

			Assert.assertNotNull(ct.getOutput());
			Assert.assertNotNull(ct.getOutput()
					.getValue("echo_output"));
			Assert.assertEquals("hello",
					((AbstractParameter) ct.getOutput()
							.getValue("echo_output")).toStringVal());

		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR");
		}
	}
}
