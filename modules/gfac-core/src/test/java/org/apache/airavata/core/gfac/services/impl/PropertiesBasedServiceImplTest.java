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

import org.apache.airavata.core.gfac.api.impl.JCRRegistry;
import org.apache.airavata.core.gfac.context.InvocationContext;
import org.apache.airavata.core.gfac.context.impl.ExecutionContextImpl;
import org.apache.airavata.core.gfac.context.impl.ParameterContextImpl;
import org.apache.airavata.core.gfac.notification.DummyNotification;
import org.apache.airavata.core.gfac.type.DataType;
import org.apache.airavata.core.gfac.type.HostDescription;
import org.apache.airavata.core.gfac.type.Parameter;
import org.apache.airavata.core.gfac.type.ServiceDescription;
import org.apache.airavata.core.gfac.type.app.ShellApplicationDeployment;
import org.apache.airavata.core.gfac.type.parameter.AbstractParameter;
import org.apache.airavata.core.gfac.type.parameter.StringParameter;
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

			InvocationContext ct = new InvocationContext();
			ct.setExecutionContext(new ExecutionContextImpl());

			ct.getExecutionContext().setNotificationService(
					new DummyNotification());

			ct.setServiceName("SimpleEcho");

			/*
			 * Input
			 */
			ParameterContextImpl input = new ParameterContextImpl();
			StringParameter echo_input = new StringParameter();
			echo_input.parseStringVal("echo_output=hello");
			input.addParameter("echo_input", echo_input);

			/*
			 * Output
			 */
			ParameterContextImpl output = new ParameterContextImpl();
			StringParameter echo_output = new StringParameter();
			output.addParameter("echo_output", echo_output);

			// parameter
			ct.addMessageContext("input", input);
			ct.addMessageContext("output", output);

			PropertiesBasedServiceImpl service = new PropertiesBasedServiceImpl();
			service.init();
			service.execute(ct);

			Assert.assertNotNull(ct.getMessageContext("output"));
			Assert.assertNotNull(ct.getMessageContext("output")
					.getParameterValue("echo_output"));
			Assert.assertEquals("hello",
					((AbstractParameter) ct.getMessageContext("output")
							.getParameterValue("echo_output")).toStringVal());

		} catch (Exception e) {
			e.printStackTrace();
			fail("ERROR");
		}
	}
}
