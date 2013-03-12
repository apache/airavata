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
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.gfac.GFacAPI;
import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.ApplicationContext;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.context.security.GSISecurityContext;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.JobTypeType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.ProjectAccountType;
import org.apache.airavata.schemas.gfac.QueueType;
import org.apache.airavata.schemas.gfac.StringParameterType;
import org.apache.airavata.schemas.gfac.URIParameterType;
import org.apache.airavata.schemas.gfac.UnicoreHostType;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;

public class BESProviderTest {
	private JobExecutionContext jobExecutionContext;

	private static final String[] hostArray = new String[] { "https://zam1161v01.zam.kfa-juelich.de:8002/INTEROP1/services/BESFactory?res=default_bes_factory" };
	private static final String gridftpAddress = "gsiftp://gridftp1.ls4.tacc.utexas.edu:2811";

	//directory where data will be copy into and copied out to unicore resources

	// private static final String scratchDir = "/brashear/msmemon/airavata";
	//FIXME: to move all the configurations to property files.
	private static final String scratchDir = "/scratch/01437/ogce/test/";

	private String remoteTempDir = null;

	@Before
	public void initJobContext() throws Exception {
		PropertyConfigurator.configure("src/test/resources/logging.properties");

		/*
		 * Default tmp location
		 */
		String date = (new Date()).toString();
		date = date.replaceAll(" ", "_");
		date = date.replaceAll(":", "_");


		remoteTempDir = scratchDir + File.separator + "SimpleEcho" + "_" + date + "_"
				+ UUID.randomUUID();

		jobExecutionContext = new JobExecutionContext(getGFACConfig(), getServiceDesc().getType().getName());

		// set security context
		jobExecutionContext.addSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT, getSecurityContext());
		jobExecutionContext.setApplicationContext(getApplicationContext());
		jobExecutionContext.setInMessageContext(getInMessageContext());
		jobExecutionContext.setOutMessageContext(getOutMessageContext());
	}

	@Test
	public void testBESProvider() throws GFacException {
		GFacAPI gFacAPI = new GFacAPI();
		gFacAPI.submitJob(jobExecutionContext);
	}

	private GFacConfiguration getGFACConfig() throws Exception{
        URL resource = BESProviderTest.class.getClassLoader().getResource("gfac-config.xml");
        GFacConfiguration gFacConfiguration = GFacConfiguration.create(new File(resource.getPath()),null,null);
    	return gFacConfiguration;
	}

	private ApplicationContext getApplicationContext() {
		ApplicationContext applicationContext = new ApplicationContext();
		applicationContext.setHostDescription(getHostDesc());
		applicationContext
				.setApplicationDeploymentDescription(getApplicationDesc());
		applicationContext.setServiceDescription(getServiceDesc());
		return applicationContext;
	}

	private ApplicationDescription getApplicationDesc() {
		ApplicationDescription appDesc = new ApplicationDescription(
				HpcApplicationDeploymentType.type);
		HpcApplicationDeploymentType app = (HpcApplicationDeploymentType) appDesc
				.getType();
		ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory
				.newInstance();
		name.setStringValue("Simple Cat");
		app.setApplicationName(name);
		ProjectAccountType projectAccountType = app.addNewProjectAccount();
		projectAccountType.setProjectAccountNumber("TG-AST110064");

		QueueType queueType = app.addNewQueue();
		queueType.setQueueName("development");

		app.setCpuCount(1);
		// TODO: also handle parallel jobs
		app.setJobType(JobTypeType.SERIAL);
		app.setNodeCount(1);
		app.setProcessorsPerNode(1);

		/*
		 * Use bat file if it is compiled on Windows
		 */
		app.setExecutableLocation("/bin/cat");


		System.out.println(remoteTempDir);


		app.setScratchWorkingDirectory(remoteTempDir);
		app.setStaticWorkingDirectory(remoteTempDir);


		// this is required
		app.setInputDataDirectory(remoteTempDir + File.separator + "inputData");
		app.setOutputDataDirectory(remoteTempDir + File.separator + "outputData");

		app.setStandardOutput(app.getOutputDataDirectory()+ File.separator + "stdout");
		app.setStandardError(app.getOutputDataDirectory()+ File.separator + "stderr");

		return appDesc;
	}

	private HostDescription getHostDesc() {
		HostDescription host = new HostDescription(UnicoreHostType.type);
		host.getType().setHostAddress("zam1161v01.zam.kfa-juelich.de");
		host.getType().setHostName("DEMO-INTEROP-SITE");
		((UnicoreHostType) host.getType()).setUnicoreHostAddressArray(hostArray);
		((UnicoreHostType) host.getType()).setGridFTPEndPointArray(new String[]{gridftpAddress});
		return host;
	}

	private ServiceDescription getServiceDesc() {
		ServiceDescription serv = new ServiceDescription();
		serv.getType().setName("SimpleCat");

		List<InputParameterType> inputList = new ArrayList<InputParameterType>();
		InputParameterType input = InputParameterType.Factory.newInstance();
		input.setParameterName("copy_input");
		input.setParameterType(URIParameterType.Factory.newInstance());
		inputList.add(input);
		InputParameterType[] inputParamList = inputList
				.toArray(new InputParameterType[inputList.size()]);

		List<OutputParameterType> outputList = new ArrayList<OutputParameterType>();
		OutputParameterType output = OutputParameterType.Factory.newInstance();
		output.setParameterName("echo_output");
		output.setParameterType(StringParameterType.Factory.newInstance());
		outputList.add(output);
		OutputParameterType[] outputParamList = outputList
				.toArray(new OutputParameterType[outputList.size()]);

		serv.getType().setInputParametersArray(inputParamList);
		serv.getType().setOutputParametersArray(outputParamList);
		return serv;
	}

	private MessageContext getInMessageContext() {
		MessageContext inMessage = new MessageContext();

		ActualParameter copy_input = new ActualParameter();
        copy_input.getType().changeType(URIParameterType.type);
        ((URIParameterType)copy_input.getType()).setValue("file:///tmp/rave_db.h2.db ");
        inMessage.addParameter("f1", copy_input);

    	ActualParameter f2 = new ActualParameter();
        f2.getType().changeType(URIParameterType.type);
        ((URIParameterType)f2.getType()).setValue("http://unicore-dev.zam.kfa-juelich.de/maven/cog-globus/cog-jglobus/1.4/cog-jglobus-1.4.jar");
        inMessage.addParameter("f2", f2);

    	ActualParameter f3 = new ActualParameter();
        f3.getType().changeType(URIParameterType.type);
        ((URIParameterType)f3.getType()).setValue(gridftpAddress+"/"+scratchDir+"/README");
        inMessage.addParameter("f3", f3);

        ActualParameter a1 = new ActualParameter();
        a1.getType().changeType(StringParameterType.type);
        ((StringParameterType)a1.getType()).setValue("tmpstrace");
        inMessage.addParameter("arg1", a1);

        return inMessage;
	}
	private GSISecurityContext getSecurityContext(){
	    GSISecurityContext context = new GSISecurityContext();
        context.setMyproxyLifetime(3600);
        context.setMyproxyServer("myproxy.teragrid.org");
        context.setMyproxyUserName("*****");
        context.setMyproxyPasswd("*****");
        context.setTrustedCertLoc("./certificates");
        return context;
	}

	private MessageContext getOutMessageContext() {

		MessageContext outMessage = new MessageContext();
		ActualParameter a1 = new ActualParameter();
		a1.getType().changeType(StringParameterType.type);
		((StringParameterType)a1.getType()).setValue("README");
		outMessage.addParameter("echo_output", a1);

		ActualParameter o1 = new ActualParameter();
		o1.getType().changeType(URIParameterType.type);
		// this may be any gridftp / ftp directory
		((URIParameterType)o1.getType()).setValue(gridftpAddress+"/"+remoteTempDir + "/" + "outputData"+"/"+"cog-jglobus-1.4.jar");
		outMessage.addParameter("o1", o1);

		return outMessage;
	}

}
