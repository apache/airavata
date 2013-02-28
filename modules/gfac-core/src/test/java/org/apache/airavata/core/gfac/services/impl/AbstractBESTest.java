package org.apache.airavata.core.gfac.services.impl;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.context.ApplicationContext;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.schemas.gfac.JobTypeType;
import org.apache.airavata.schemas.gfac.UnicoreHostType;
import org.apache.log4j.PropertyConfigurator;

public abstract class AbstractBESTest {
	
	protected JobExecutionContext jobExecutionContext;

	public static final String[] hostArray = new String[] { "https://zam1161v01.zam.kfa-juelich.de:8002/INTEROP1/services/BESFactory?res=default_bes_factory" };
	
	
	//directory where data will be copy into and copied out to unicore resources
	
	// private static final String scratchDir = "/brashear/msmemon/airavata";
	
	public static final String gridftpAddress = "gsiftp://gridftp1.ls4.tacc.utexas.edu:2811";
	public static final String scratchDir = "/scratch/02055/msmemon/airavata";
	
	protected String remoteTempDir = null;

	

	protected void initTest() throws Exception {
		PropertyConfigurator.configure("src/test/resources/logging.properties");

		/*
		 * Default tmp location
		 */
		String date = (new Date()).toString();
		date = date.replaceAll(" ", "_");
		date = date.replaceAll(":", "_");

		
		remoteTempDir = scratchDir + File.separator + "BESJOB" + "_" + date + "_"
				+ UUID.randomUUID();
		jobExecutionContext = new JobExecutionContext(getGFACConfig(), getServiceDesc("BES-APP-Service").getType().getName());
		jobExecutionContext.setApplicationContext(getApplicationContext());
		jobExecutionContext.setInMessageContext(getInMessageContext());
		jobExecutionContext.setOutMessageContext(getOutMessageContext());

	}
	
	protected abstract void submitJob() throws Exception;
	
	protected GFacConfiguration getGFACConfig() throws Exception{
        URL resource = this.getClass().getClassLoader().getResource("gfac-config.xml");
        System.out.println(resource.getFile());
        GFacConfiguration gFacConfiguration = GFacConfiguration.create(new File(resource.getPath()),null,null);
		gFacConfiguration.setMyProxyLifeCycle(3600);
		gFacConfiguration.setMyProxyServer("myproxy.teragrid.org");
		gFacConfiguration.setMyProxyUser("msmemon");
		gFacConfiguration.setMyProxyPassphrase("*******");
		gFacConfiguration.setTrustedCertLocation("/home/m.memon/.globus/certificates");
		return gFacConfiguration;
	}
	
	protected ApplicationContext getApplicationContext() {
		ApplicationContext applicationContext = new ApplicationContext();
		applicationContext.setHostDescription(getHostDesc());
		return applicationContext;
	}

	protected HostDescription getHostDesc() {
		HostDescription host = new HostDescription(UnicoreHostType.type);
		host.getType().setHostAddress("zam1161v01.zam.kfa-juelich.de");
		host.getType().setHostName("DEMO-INTEROP-SITE");
		((UnicoreHostType) host.getType()).setUnicoreHostAddressArray(hostArray);
		((UnicoreHostType) host.getType()).setGridFTPEndPointArray(new String[]{gridftpAddress});
		return host;
	}

	
	protected abstract ApplicationDescription getApplicationDesc(JobTypeType jobType); 
	

	protected ServiceDescription getServiceDesc(String serviceName) {
		ServiceDescription serv = new ServiceDescription();
		serv.getType().setName(serviceName);
		return serv;
	}
	
	
	protected abstract MessageContext getInMessageContext();

	
	
	protected abstract MessageContext getOutMessageContext();


}
