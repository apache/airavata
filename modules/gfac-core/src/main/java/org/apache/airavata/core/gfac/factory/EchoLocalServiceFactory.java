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

package org.apache.airavata.core.gfac.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.airavata.commons.gfac.type.DataType;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.Parameter;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.commons.gfac.type.app.ShellApplicationDeployment;
import org.apache.airavata.core.gfac.services.GenericService;
import org.apache.airavata.core.gfac.services.impl.POJOServiceImpl;

public class EchoLocalServiceFactory extends AbstractServiceFactory {

	private GenericService service;

	public GenericService getGenericService() {
		if (service == null) {
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
			serv.setName("EchoPOJO");

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

			service = new POJOServiceImpl(host, app, serv);
		}
		return service;
	}
}
