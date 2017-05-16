/**
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
 */
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package org.apache.airavata.xbaya.interpreter;
//
//import org.apache.airavata.commons.gfac.type.ApplicationDescription;
//import org.apache.airavata.registry.api.exception.RegistryException;
//import org.apache.airavata.commons.gfac.type.HostDescription;
//import org.apache.airavata.commons.gfac.type.ServiceDescription;
//import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.ExpectedException;
//import org.junit.rules.MethodRule;
//import org.junit.rules.TestWatchman;
//import org.junit.runners.model.FrameworkMethod;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
////FIXME: Add tests for new registry. Airavata-592
//public class RegistryServiceTest {
//	@Rule
//	public ExpectedException exception = ExpectedException.none();
//
//	@Rule
//	public MethodRule watchman = new TestWatchman() {
//		public void starting(FrameworkMethod method) {
//			logger.info("{} being run...", method.getName());
//		}
//	};
//
//	final Logger logger = LoggerFactory.getLogger(RegistryServiceTest.class);
//
//	@Before
//	public void testExecute() throws RegistryException {
//
//	}
//
//	@After
//	public void cleanup() throws RegistryException {
//	}
//
//	private HostDescription createHostDescription() {
//		HostDescription host = new HostDescription();
//		host.getType().setHostName("localhost");
//		host.getType().setHostAddress("localhost");
//		return host;
//	}
//
//	private ServiceDescription createServiceDescription() {
//		ServiceDescription serv = new ServiceDescription();
//		serv.getType().setName("SimpleEcho");
//		return serv;
//	}
//
//	private ApplicationDescription createAppDeploymentDescription() {
//		ApplicationDescription appDesc = new ApplicationDescription();
//		ApplicationDeploymentDescriptionType app = appDesc.getType();
//		ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory
//				.newInstance();
//		name.setStringValue("EchoLocal");
//		app.setApplicationName(name);
//		app.setExecutableLocation("/bin/echo");
//		app.setScratchWorkingDirectory("/tmp");
//		app.setStaticWorkingDirectory("/tmp");
//		app.setInputDataDirectory("/tmp/input");
//		app.setOutputDataDirectory("/tmp/output");
//		app.setStandardOutput("/tmp/echo.stdout");
//		app.setStandardError("/tmp/echo.stdout");
//		return appDesc;
//	}
//
//	@Test
//	public void getFromRegistry() throws RegistryException {
//	}
//
//	@Test
//	public void searchRegistry() throws RegistryException {
//	}
//
//	@Test
//	public void deleteFromRegistry() throws RegistryException {
//
//	}
//
//}
