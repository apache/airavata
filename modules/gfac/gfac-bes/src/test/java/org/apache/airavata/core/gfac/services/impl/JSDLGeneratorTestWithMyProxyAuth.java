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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

//public class JSDLGeneratorTestWithMyProxyAuth {
//
//	public static final String[] hostArray = new String[] { "https://zam1161v01.zam.kfa-juelich.de:8002/INTEROP1/services/BESFactory?res=default_bes_factory" };
//	public static final String gridftpAddress = "gsiftp://gridftp.blacklight.psc.teragrid.org:2811";
//	public static final String hostAddress = "zam1161v01.zam.kfa-juelich.de";
//	public static final String hostName = "DEMO-INTEROP-SITE";
//	public static final String scratchDir = "/scratch/msmemon/airavata";
//
//	protected JobExecutionContext jobExecutionContext;
//
//
//	@Test
//	public void testSerialJSDLWithStdout() throws Exception{
//
//		JobTypeType jobType = JobTypeType.Factory.newInstance();
//		jobType.set(JobTypeType.SERIAL);
//		ApplicationContext appContext = getApplicationContext();
//		appContext.setApplicationDeploymentDescription(getApplicationDesc(jobType, true));
//		jobExecutionContext.setApplicationContext(appContext);
//
//		JobDefinitionDocument jobDefDoc = JSDLGenerator.buildJSDLInstance(jobExecutionContext);
//
//		assertTrue (jobDefDoc.getJobDefinition().getJobDescription().getApplication().toString().contains("/bin/cat"));
//		assertTrue(jobDefDoc.getJobDefinition().getJobDescription().getDataStagingArray().length > 2);
//
//		assertTrue(jobDefDoc.getJobDefinition().getJobDescription().getJobIdentification().getJobProjectArray().length > 0);
//
//		assertFalse(JSDLUtils.getPOSIXApplication(jobDefDoc.getJobDefinition())==null);
//
//		assertEquals("jsdl_stdout", JSDLUtils.getOrCreatePOSIXApplication(jobDefDoc.getJobDefinition()).getOutput().getStringValue().toString());
//
//	}
//
//	@Test
//	public void testSerialJSDLWithoutStdout() throws Exception{
//
//		JobTypeType jobType = JobTypeType.Factory.newInstance();
//		jobType.set(JobTypeType.SERIAL);
//		ApplicationContext appContext = getApplicationContext();
//		appContext.setApplicationDeploymentDescription(getApplicationDesc(jobType, false));
//		jobExecutionContext.setApplicationContext(appContext);
//
//		JobDefinitionDocument jobDefDoc = JSDLGenerator.buildJSDLInstance(jobExecutionContext);
//
//		assertTrue (jobDefDoc.getJobDefinition().getJobDescription().getApplication().toString().contains("/bin/cat"));
//		assertTrue(jobDefDoc.getJobDefinition().getJobDescription().getDataStagingArray().length > 2);
//
//		assertTrue(jobDefDoc.getJobDefinition().getJobDescription().getJobIdentification().getJobProjectArray().length > 0);
//
//		assertFalse(JSDLUtils.getPOSIXApplication(jobDefDoc.getJobDefinition())==null);
//
//		assertEquals("stdout", JSDLUtils.getOrCreatePOSIXApplication(jobDefDoc.getJobDefinition()).getOutput().getStringValue().toString());
//		assertEquals("stderr", JSDLUtils.getOrCreatePOSIXApplication(jobDefDoc.getJobDefinition()).getError().getStringValue().toString());
//
//	}
//
//
//	@Test
//	public void testMPIJSDL() throws Exception{
//
//		JobTypeType jobType = JobTypeType.Factory.newInstance();
//		jobType.set(JobTypeType.MPI);
//		ApplicationContext appContext = getApplicationContext();
//		appContext.setApplicationDeploymentDescription(getApplicationDesc(jobType, true));
//		jobExecutionContext.setApplicationContext(appContext);
//
//		JobDefinitionDocument jobDefDoc = JSDLGenerator.buildJSDLInstance(jobExecutionContext);
//
//		assertTrue (jobDefDoc.getJobDefinition().getJobDescription().getApplication().toString().contains("/bin/cat"));
//		assertTrue(jobDefDoc.getJobDefinition().getJobDescription().getDataStagingArray().length > 2);
//
//		assertTrue(jobDefDoc.getJobDefinition().getJobDescription().getJobIdentification().getJobProjectArray().length > 0);
//
//		assertEquals("jsdl_stdout", JSDLUtils.getOrCreateSPMDApplication(jobDefDoc.getJobDefinition()).getOutput().getStringValue().toString());
//
//		assertFalse(JSDLUtils.getSPMDApplication(jobDefDoc.getJobDefinition())==null);
//
//
//	}
//
//	protected GFacConfiguration getGFACConfig() throws Exception{
//        URL resource = ApplicationSettings.loadFile(org.apache.airavata.common.utils.Constants.GFAC_CONFIG_XML);
//        System.out.println(resource.getFile());
//        GFacConfiguration gFacConfiguration = GFacConfiguration.create(new File(resource.getPath()),null,null);
//		return gFacConfiguration;
//	}
//
//
//	protected ApplicationContext getApplicationContext() {
//		ApplicationContext applicationContext = new ApplicationContext();
//		applicationContext.setHostDescription(getHostDesc());
//
//		applicationContext.setServiceDescription(getServiceDesc());
//		return applicationContext;
//	}
//
//	protected ApplicationDescription getApplicationDesc(JobTypeType jobType, boolean setOuput) {
//		ApplicationDescription appDesc = new ApplicationDescription(
//				HpcApplicationDeploymentType.type);
//		HpcApplicationDeploymentType app = (HpcApplicationDeploymentType) appDesc
//				.getType();
//		ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory
//				.newInstance();
//		name.setStringValue("EchoLocal");
//		app.setApplicationName(name);
//		ProjectAccountType projectAccountType = app.addNewProjectAccount();
//		projectAccountType.setProjectAccountNumber("TG-AST110064");
//
//		QueueType queueType = app.addNewQueue();
//		queueType.setQueueName("development");
//
//		app.setCpuCount(1);
//		// TODO: also handle parallel jobs
//		if((jobType.enumValue() == JobTypeType.SERIAL) || (jobType.enumValue() == JobTypeType.SINGLE)) {
//			app.setJobType(JobTypeType.SERIAL);
//		}
//		else if (jobType.enumValue() == JobTypeType.MPI) {
//			app.setJobType(JobTypeType.MPI);
//		}
//		else {
//			app.setJobType(JobTypeType.OPEN_MP);
//		}
//
//		app.setNodeCount(1);
//		app.setProcessorsPerNode(1);
//
//		/*
//		 * Use bat file if it is compiled on Windows
//		 */
//		app.setExecutableLocation("/bin/cat");
//
//		/*
//		 * Default tmp location
//		 */
//		String date = (new Date()).toString();
//		date = date.replaceAll(" ", "_");
//		date = date.replaceAll(":", "_");
//
//		String remoteTempDir = scratchDir + File.separator + "SimpleEcho" + "_" + date + "_"
//				+ UUID.randomUUID();
//
//		System.out.println(remoteTempDir);
//
//		// no need of these parameters, as unicore manages by itself
//		app.setScratchWorkingDirectory(remoteTempDir);
//		app.setStaticWorkingDirectory(remoteTempDir);
//		app.setInputDataDirectory(remoteTempDir + File.separator + "inputData");
//		app.setOutputDataDirectory(remoteTempDir + File.separator + "outputData");
//
//		if(setOuput) {
//			app.setStandardOutput(app.getOutputDataDirectory()+"/jsdl_stdout");
//			app.setStandardError(app.getOutputDataDirectory()+"/jsdl_stderr");
//		}
//		return appDesc;
//	}
//
//	protected HostDescription getHostDesc() {
//		HostDescription host = new HostDescription(UnicoreHostType.type);
//		host.getType().setHostAddress(hostAddress);
//		host.getType().setHostName(hostName);
//		((UnicoreHostType) host.getType()).setUnicoreBESEndPointArray(hostArray);
//		((UnicoreHostType) host.getType()).setGridFTPEndPointArray(new String[]{gridftpAddress});
//		return host;
//	}
//
//	protected ServiceDescription getServiceDesc() {
//		ServiceDescription serv = new ServiceDescription();
//		serv.getType().setName("SimpleCat");
//
//		List<InputParameterType> inputList = new ArrayList<InputParameterType>();
//		InputParameterType input = InputParameterType.Factory.newInstance();
//		input.setParameterName("echo_input");
//		input.setParameterType(StringParameterType.Factory.newInstance());
//		inputList.add(input);
//		InputParameterType[] inputParamList = inputList
//				.toArray(new InputParameterType[inputList.size()]);
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
//
//		return serv;
//	}
//
//	protected MessageContext getInMessageContext() {
//		MessageContext inMessage = new MessageContext();
//
//        ActualParameter i1 = new ActualParameter();
//        i1.getType().changeType(URIParameterType.type);
//        ((URIParameterType)i1.getType()).setValue("file:///tmp/ifile1");
//        inMessage.addParameter("i1", i1);
//
//        ActualParameter i2 = new ActualParameter();
//        i2.getType().changeType(URIParameterType.type);
//        ((URIParameterType)i2.getType()).setValue("file:///tmp/ifile2");
//        inMessage.addParameter("i2", i2);
//
//        ActualParameter i3 = new ActualParameter();
//        i2.getType().changeType(URIParameterType.type);
//        ((URIParameterType)i2.getType()).setValue("///tmp/ifile2");
//        inMessage.addParameter("i3", i2);
//
//        ActualParameter simpleArg = new ActualParameter();
//        simpleArg.getType().changeType(StringParameterType.type);
//        ((StringParameterType)simpleArg.getType()).setValue("argument1");
//        inMessage.addParameter("a1", simpleArg);
//
//        ActualParameter nameValueArg = new ActualParameter();
//        nameValueArg.getType().changeType(StringParameterType.type);
//        ((StringParameterType)nameValueArg.getType()).setValue("name1=value1");
//        inMessage.addParameter("nameValueArg", nameValueArg);
//
//		ActualParameter echo_input = new ActualParameter();
//		((StringParameterType) echo_input.getType())
//				.setValue("echo_output=hello");
//		inMessage.addParameter("echo_input", echo_input);
//
//		return inMessage;
//	}
//
//	protected MessageContext getOutMessageContext() {
//		MessageContext om1 = new MessageContext();
//
//		// TODO: Aint the output parameters are only the name of the files staged out to the gridftp endpoint?
//		ActualParameter o1 = new ActualParameter();
//		((StringParameterType) o1.getType())
//		.setValue("tempfile");
//		om1.addParameter("o1", o1);
//
//		ActualParameter o2 = new ActualParameter();
//		o2.getType().changeType(URIParameterType.type);
//
//		((URIParameterType)o2.getType()).setValue("http://path/to/upload");
//		om1.addParameter("o2", o2);
//
//
//
//		return om1;
//	}
//
//	@Before
//	public void initJobContext() throws Exception {
//		PropertyConfigurator.configure("src/test/resources/logging.properties");
//		jobExecutionContext = new JobExecutionContext(getGFACConfig(), getServiceDesc().getType().getName());
//		jobExecutionContext.setApplicationContext(getApplicationContext());
//		jobExecutionContext.setInMessageContext(getInMessageContext());
//		jobExecutionContext.setOutMessageContext(getOutMessageContext());
//	}
//
//
//}
