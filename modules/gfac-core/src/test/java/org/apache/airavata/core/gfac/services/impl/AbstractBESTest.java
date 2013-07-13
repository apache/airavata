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

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

import org.apache.airavata.common.utils.DatabaseTestCases;
import org.apache.airavata.common.utils.DerbyUtil;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.credential.store.store.impl.CredentialReaderImpl;
import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.RequestData;
import org.apache.airavata.gfac.context.ApplicationContext;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.context.security.GSISecurityContext;
import org.apache.airavata.schemas.gfac.JobTypeType;
import org.apache.airavata.schemas.gfac.UnicoreHostType;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;

public abstract class AbstractBESTest extends GFacBaseTestCase {

	protected JobExecutionContext jobExecutionContext;

	public static final String[] hostArray = new String[] { "https://zam1161v01.zam.kfa-juelich.de:8002/INTEROP1/services/BESFactory?res=default_bes_factory" };


	// public static final String[] hostArray = new String[] {
	// "https://deisa-unic.fz-juelich.de:9111/FZJ_JUROPA/services/BESFactory?res=default_bes_factory"
	// };

	// directory where data will be copy into and copied out to unicore
	// resources

	// public static final String gridftpAddress =
	// "gsiftp://gridftp1.ls4.tacc.utexas.edu:2811";
	// public static final String scratchDir = "/scratch/01129/tg804076/";

	// public static final String gridftpAddress =
	// "gsiftp://gridftp.psc.xsede.org:2811";
	// public static final String scratchDir = "/brashear/msmemon/airavata/";

	public static final String gridftpAddress = "gsiftp://osg-xsede.grid.iu.edu:2811/";
	public static final String scratchDir = "/home/ramifnu/airavata/";

	protected String remoteTempDir = null;

	protected void initTest() throws Exception {
		PropertyConfigurator.configure("src/test/resources/logging.properties");
		/*
		 * Default tmp location
		 */
		String date = (new Date()).toString();
		date = date.replaceAll(" ", "_");
		date = date.replaceAll(":", "_");

		remoteTempDir = scratchDir + File.separator + "BESJOB" + "_" + date
				+ "_" + UUID.randomUUID();
		jobExecutionContext = new JobExecutionContext(getGFACConfig(),
				getServiceDesc("BES-APP-Service").getType().getName());
		jobExecutionContext.setApplicationContext(getApplicationContext());
		jobExecutionContext.setInMessageContext(getInMessageContext());
		jobExecutionContext.setOutMessageContext(getOutMessageContext());
		jobExecutionContext.addSecurityContext(
				GSISecurityContext.GSI_SECURITY_CONTEXT, getSecurityContext());

	}

	protected abstract void submitJob() throws Exception;

	protected GFacConfiguration getGFACConfig() throws Exception {
		URL resource = this.getClass().getClassLoader()
				.getResource("gfac-config.xml");
		System.out.println(resource.getFile());
		return GFacConfiguration.create(new File(resource.getPath()), null,
				null);
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
		((UnicoreHostType) host.getType())
				.setUnicoreBESEndPointArray(hostArray);
		((UnicoreHostType) host.getType())
				.setGridFTPEndPointArray(new String[] { gridftpAddress });
		return host;
	}

	protected abstract ApplicationDescription getApplicationDesc(
			JobTypeType jobType);

	protected ServiceDescription getServiceDesc(String serviceName) {
		ServiceDescription serv = new ServiceDescription();
		serv.getType().setName(serviceName);
		return serv;
	}

	protected abstract MessageContext getInMessageContext();

	protected abstract MessageContext getOutMessageContext();

}
