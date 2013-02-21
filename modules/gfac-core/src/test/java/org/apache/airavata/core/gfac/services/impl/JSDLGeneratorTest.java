//package org.apache.airavata.core.gfac.services.impl;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//import java.io.File;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.UUID;
//
//import org.apache.airavata.commons.gfac.type.ActualParameter;
//import org.apache.airavata.commons.gfac.type.ApplicationDescription;
//import org.apache.airavata.commons.gfac.type.HostDescription;
//import org.apache.airavata.commons.gfac.type.ServiceDescription;
//import org.apache.airavata.gfac.GFacConfiguration;
//import org.apache.airavata.gfac.context.ApplicationContext;
//import org.apache.airavata.gfac.context.JobExecutionContext;
//import org.apache.airavata.gfac.context.MessageContext;
//import org.apache.airavata.gfac.provider.utils.JSDLGenerator;
//import org.apache.airavata.gfac.provider.utils.JSDLUtils;
//import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
//import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
//import org.apache.airavata.schemas.gfac.InputParameterType;
//import org.apache.airavata.schemas.gfac.JobTypeType;
//import org.apache.airavata.schemas.gfac.OutputParameterType;
//import org.apache.airavata.schemas.gfac.ProjectAccountType;
//import org.apache.airavata.schemas.gfac.QueueType;
//import org.apache.airavata.schemas.gfac.StringParameterType;
//import org.apache.airavata.schemas.gfac.URIParameterType;
//import org.apache.airavata.schemas.gfac.UnicoreHostType;
//import org.apache.log4j.PropertyConfigurator;
//import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
//import org.junit.Before;
//import org.junit.Test;
//
//public class JSDLGeneratorTest {
//
//
//	private static final String[] hostArray = new String[] { "https://zam1161v01.zam.kfa-juelich.de:8002/INTEROP1/services/BESFactory?res=default_bes_factory" };
//	private static final String gridftpAddress = "gsiftp://gridftp.blacklight.psc.teragrid.org:2811";
//	private static final String hostAddress = "zam1161v01.zam.kfa-juelich.de";
//	private static final String hostName = "DEMO-INTEROP-SITE";
//	private static final String scratchDir = "/scratch/msmemon/airavata";
//
//	private JobExecutionContext jobExecutionContext;
//
//	@Test
//	public void testJSDLUtils() throws Exception{
//		JobDefinitionDocument jobDefDoc = JSDLGenerator.buildJSDLInstance(jobExecutionContext);
//
//		assertTrue (jobDefDoc.getJobDefinition().getJobDescription().getApplication().toString().contains("/bin/cat"));
//		assertTrue(jobDefDoc.getJobDefinition().getJobDescription().getDataStagingArray().length > 2);
//
//		assertTrue(jobDefDoc.getJobDefinition().getJobDescription().getJobIdentification().getJobProjectArray().length > 0);
//
//		assertEquals("jsdl_stdout", JSDLUtils.getOrCreatePOSIXApplication(jobDefDoc.getJobDefinition()).getOutput().getStringValue().toString());
//
//		System.out.println(jobDefDoc);
//
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
//	private GFacConfiguration getGFACConfig() throws Exception{
//        URL resource = BESProviderTest.class.getClassLoader().getResource("gfac-config.xml");
//        System.out.println(resource.getFile());
//        GFacConfiguration gFacConfiguration = GFacConfiguration.create(new File(resource.getPath()),null,null);
//		gFacConfiguration.setMyProxyLifeCycle(3600);
//		gFacConfiguration.setMyProxyServer("");
//		gFacConfiguration.setMyProxyUser("");
//		gFacConfiguration.setMyProxyPassphrase("");
//		gFacConfiguration.setTrustedCertLocation("");
//		return gFacConfiguration;
//	}
//
//
//	private ApplicationContext getApplicationContext() {
//		ApplicationContext applicationContext = new ApplicationContext();
//		applicationContext.setHostDescription(getHostDesc());
//		applicationContext
//				.setApplicationDeploymentDescription(getApplicationDesc());
//
//		applicationContext.setServiceDescription(getServiceDesc());
//		return applicationContext;
//	}
//
//	private ApplicationDescription getApplicationDesc() {
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
//		app.setJobType(JobTypeType.SERIAL);
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
//		app.setStandardOutput(app.getOutputDataDirectory()+"/jsdl_stdout");
//
//		app.setStandardError(app.getOutputDataDirectory()+"/jsdl_stderr");
//
//		return appDesc;
//	}
//
//	private HostDescription getHostDesc() {
//		HostDescription host = new HostDescription(UnicoreHostType.type);
//		host.getType().setHostAddress(hostAddress);
//		host.getType().setHostName(hostName);
//		((UnicoreHostType) host.getType()).setUnicoreHostAddressArray(hostArray);
//		((UnicoreHostType) host.getType()).setGridFTPEndPointArray(new String[]{gridftpAddress});
//		return host;
//	}
//
//	private ServiceDescription getServiceDesc() {
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
//	private MessageContext getInMessageContext() {
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
//	private MessageContext getOutMessageContext() {
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
//
//
//}
