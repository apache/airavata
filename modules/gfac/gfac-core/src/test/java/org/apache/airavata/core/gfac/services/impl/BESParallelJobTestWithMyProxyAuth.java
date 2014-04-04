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
import java.util.Date;
import java.util.UUID;

import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.gfac.context.ApplicationContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.cpi.GFacImpl;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.ExtendedKeyValueType;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.apache.airavata.schemas.gfac.JobTypeType;
import org.junit.Before;
import org.junit.Test;

public class BESParallelJobTestWithMyProxyAuth extends AbstractBESTestWithMyProxyAuth {
	
	
	@Before
	public void initJobContext() throws Exception {
		initTest();
	}

	
	@Test
	public void submitJob() throws Exception {
		JobTypeType jobType = JobTypeType.Factory.newInstance();
		jobType.set(JobTypeType.MPI);
		ApplicationContext appContext = getApplicationContext();
		appContext.setApplicationDeploymentDescription(getApplicationDesc(jobType));
		jobExecutionContext.setApplicationContext(appContext);
        GFacImpl gFacAPI = new GFacImpl();
		gFacAPI.submitJob(jobExecutionContext);
	}
	
	
	protected ApplicationDescription getApplicationDesc(JobTypeType jobType) {
		ApplicationDescription appDesc = new ApplicationDescription(
				HpcApplicationDeploymentType.type);
		HpcApplicationDeploymentType appDepType = (HpcApplicationDeploymentType) appDesc
				.getType();
		ApplicationDeploymentDescriptionType.ApplicationName name = ApplicationDeploymentDescriptionType.ApplicationName.Factory
				.newInstance();
		name.setStringValue("MPIRemote");
		appDepType.setApplicationName(name);
//		ProjectAccountType projectAccountType = appDepType.addNewProjectAccount();
//		projectAccountType.setProjectAccountNumber("TG-AST110064");

//		QueueType queueType = appDepType.addNewQueue();
//		queueType.setQueueName("development");

		// TODO: also handle parallel jobs
		if((jobType.enumValue() == JobTypeType.SERIAL) || (jobType.enumValue() == JobTypeType.SINGLE)) {
			appDepType.setJobType(JobTypeType.SERIAL);
		}
		else if (jobType.enumValue() == JobTypeType.MPI) {
			appDepType.setJobType(JobTypeType.MPI);
		}
		else {
			appDepType.setJobType(JobTypeType.OPEN_MP);
		}
		
		appDepType.setNodeCount(1);
		appDepType.setProcessorsPerNode(1);

		/*
		 * Use bat file if it is compiled on Windows
		 */
		appDepType.setExecutableLocation("/home/bes/mpiexamples/a.out");
		
//		appDepType.setExecutableLocation("/lustre/jhome11/zam/m.memon/mpiexamples");
		
		
		
		ExtendedKeyValueType extKV = appDepType.addNewKeyValuePairs();
		// using jsdl spmd standard
		extKV.setName("NumberOfProcesses");
		// this will be transformed into mpiexec -n 4
		extKV.setStringValue("1"); 
		
		/*
		 * Default tmp location
		 */
		String date = (new Date()).toString();
		date = date.replaceAll(" ", "_");
		date = date.replaceAll(":", "_");

		String remoteTempDir = scratchDir + File.separator + "SimpleEcho" + "_" + date + "_"
				+ UUID.randomUUID();

		System.out.println(remoteTempDir);
		
		// no need of these parameters, as unicore manages by itself
		appDepType.setScratchWorkingDirectory(remoteTempDir);
		appDepType.setStaticWorkingDirectory(remoteTempDir);
		appDepType.setInputDataDirectory(remoteTempDir + File.separator + "inputData");
		appDepType.setOutputDataDirectory(remoteTempDir + File.separator + "outputData");
		
		appDepType.setStandardOutput(appDepType.getOutputDataDirectory()+"/jsdl_stdout");
		
		appDepType.setStandardError(appDepType.getOutputDataDirectory()+"/jsdl_stderr");

		return appDesc;
	}
	protected MessageContext getInMessageContext() {
		MessageContext inMessage = new MessageContext();
        return inMessage;
	}

	
	
	protected MessageContext getOutMessageContext() {
		MessageContext outMessage = new MessageContext();
		return outMessage;
	}



}
