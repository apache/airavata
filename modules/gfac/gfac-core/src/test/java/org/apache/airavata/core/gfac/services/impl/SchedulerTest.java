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

public class SchedulerTest {
//    private AiravataRegistry2 jcrRegistry;
//    @Before
//	public void setUp() throws Exception {
//		/*
//		 * Create database
//		 */
//        Map<String,String> config = new HashMap<String,String>();
//            config.put("org.apache.jackrabbit.repository.home","target");
//        jcrRegistry = RegistryUtils.getRegistryFromConfig(new URL("test.properties"));
//
//		/*
//		 * Host
//		 */
//		HostDescription host = new HostDescription();
//		host.getType().setHostName("localhost");
//		host.getType().setHostAddress("10.11.111.1");
//
//
//
//
//
//		/*
//		 * App
//		 */
//		ApplicationDeploymentDescription appDesc = new ApplicationDeploymentDescription();
//		ApplicationDeploymentDescriptionType app = appDesc.getType();
//		ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory.newInstance();
//		name.setStringValue("EchoLocal");
//		app.setApplicationName(name);
//		app.setExecutableLocation("/bin/echo");
//		app.setScratchWorkingDirectory("/tmp");
//		app.setStaticWorkingDirectory("/tmp");
//		app.setInputDataDirectory("/tmp/input");
//		app.setOutputDataDirectory("/tmp/output");
//		app.setStandardOutput("/tmp/echo.stdout");
//		app.setStandardError("/tmp/echo.stdout");
//
//
//
//		/*
//		 * Service
//		 */
//		ServiceDescription serv = new ServiceDescription();
//		serv.getType().setName("SimpleEcho");
//
//
//
//		List<InputParameterType> inputList = new ArrayList<InputParameterType>();
//		InputParameterType input = InputParameterType.Factory.newInstance();
//		input.setParameterName("echo_input");
//		input.setParameterType(StringParameterType.Factory.newInstance());
//		inputList.add(input);
//		InputParameterType[] inputParamList = inputList.toArray(new InputParameterType[inputList
//				.size()]);
//
//		List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();
//		OutputParameterType output = OutputParameterType.Factory.newInstance();
//		output.setParameterName("echo_output");
//		output.setParameterType(StringParameterType.Factory.newInstance());
//		outputList.add(output);
//		OutputParameterType[] outputParamList = outputList
//				.toArray(new OutputParameterType[outputList.size()]);
//
//		serv.getType().setInputParametersArray(inputParamList);
//		serv.getType().setOutputParametersArray(outputParamList);
//
//		/*
//		 * Save to registry
//		 */
//		jcrRegistry.addHostDescriptor(host);
//		jcrRegistry.addApplicationDescriptor(serv.getType().getName(), host
//				.getType().getHostName(), appDesc);
//		jcrRegistry.addServiceDescriptor(serv);
////		jcrRegistry.addApplicationDescriptor(serv.getType().getName(), host
////				.getType().getHostName());
//	}
//
//	@Test
//	public void testExecute() {
//		try {
//
//			DefaultInvocationContext ct = new DefaultInvocationContext();
//			DefaultExecutionContext ec = new DefaultExecutionContext();
//			ec.addNotifiable(new LoggingNotification());
//			ct.setExecutionContext(ec);
//
//			ct.setServiceName("SimpleEcho");
//
//			/*
//			 * Input
//			 */
//			ParameterContextImpl input = new ParameterContextImpl();
//			ActualParameter echo_input = new ActualParameter();
//			((StringParameterType)echo_input.getType()).setValue("echo_output=hello");
//			input.add("echo_input", echo_input);
//
//			/*
//			 * Output
//			 */
//			ParameterContextImpl output = new ParameterContextImpl();
//			ActualParameter echo_output = new ActualParameter();
//			output.add("echo_output", echo_output);
//
//			// parameter
//			ct.setInput(input);
//			ct.setOutput(output);
//            ct.getExecutionContext().setRegistryService(jcrRegistry);
//            Scheduler scheduler = new SchedulerImpl();
//            Provider provider = scheduler.schedule(ct);
//
//
//              if(provider instanceof GramProvider){
//                junit.framework.Assert.assertTrue(true);
//            }else {
//                junit.framework.Assert.assertTrue(false);
//            }
//            IOUtil.deleteDirectory(new File((new File(".")).getAbsolutePath() + File.separator + "target"));
//
//        } catch (Exception e) {
//			e.printStackTrace();
//			fail("ERROR");
//		}
//	}
}
