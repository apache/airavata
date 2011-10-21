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

import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.Parameter;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.commons.gfac.type.app.ShellApplicationDeployment;
import org.apache.airavata.commons.gfac.type.parameter.AbstractParameter;
import org.apache.airavata.commons.gfac.type.parameter.ParameterFactory;
import org.apache.airavata.core.gfac.context.invocation.impl.DefaultExecutionContext;
import org.apache.airavata.core.gfac.context.invocation.impl.DefaultInvocationContext;
import org.apache.airavata.core.gfac.context.message.impl.ParameterContextImpl;
import org.apache.airavata.core.gfac.notification.impl.LoggingNotification;
import org.apache.airavata.registry.api.impl.JCRRegistry;
import org.apache.airavata.schemas.gfac.ShellApplicationDeploymentType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

public class PropertiesBasedServiceImplTest {
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
		host.setId("localhost");
		host.setAddress("localhost");

		/*
		 * App
		 */
		ShellApplicationDeployment app = new ShellApplicationDeployment();
		app.setId("EchoLocal");
		app.setExecutable("/bin/echo");
		app.setTmpDir("/tmp");
		app.setWorkingDir("/tmp");
		app.setInputDir("/tmp/input");
		app.setOutputDir("/tmp/output");
		app.setStdOut("/tmp/echo.stdout");
		app.setStdErr("/tmp/echo.stdout");
		app.setEnv(ShellApplicationDeploymentType.Factory.newInstance().getEnv());

		/*
		 * Service
		 */
		ServiceDescription serv = new ServiceDescription();
		serv.setId("SimpleEcho");

		Parameter input = new Parameter();
		input.setName("echo_input");
		input.setType(ParameterFactory.getInstance().getType("String"));
		List<Parameter> inputList = new ArrayList<Parameter>();
		inputList.add(input);

		Parameter output = new Parameter();
		output.setName("echo_output");
		output.setType(ParameterFactory.getInstance().getType("String"));
		List<Parameter> outputList = new ArrayList<Parameter>();
		outputList.add(output);

		serv.setInputParameters(inputList);
		serv.setOutputParameters(outputList);

		/*
		 * Save to registry
		 */
		jcrRegistry.saveHostDescription(host);
		jcrRegistry.saveDeploymentDescription(serv.getId(), host.getId(),
				app);
		jcrRegistry.saveServiceDescription(serv);
		jcrRegistry.deployServiceOnHost(serv.getId(), host.getId());
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
			AbstractParameter echo_input = ParameterFactory.getInstance().createActualParameter("String");
			echo_input.parseStringVal("echo_output=hello");
			input.add("echo_input", echo_input);

			/*
			 * Output
			 */
			ParameterContextImpl output = new ParameterContextImpl();
			AbstractParameter echo_output = ParameterFactory.getInstance().createActualParameter("String");
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
