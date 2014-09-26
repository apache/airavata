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
package org.apache.airavata.gfac.bes.utils;


import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * Utility class generates a JSDL instance from JobExecutionContext instance
 * 
 * @author shahbaz memon
 * 
 * */

public class JSDLGenerator implements BESConstants {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	public synchronized static JobDefinitionDocument buildJSDLInstance(
			JobExecutionContext context) throws Exception {

		JobDefinitionDocument jobDefDoc = JobDefinitionDocument.Factory
				.newInstance();
		JobDefinitionType value = jobDefDoc.addNewJobDefinition();

		HpcApplicationDeploymentType appDepType = (HpcApplicationDeploymentType) context
				.getApplicationContext().getApplicationDeploymentDescription()
				.getType();

		// build Identification
		createJobIdentification(value, appDepType);

		ResourceProcessor.generateResourceElements(value, context);

		ApplicationProcessor.generateJobSpecificAppElements(value, context);

		DataStagingProcessor.generateDataStagingElements(value, context);

		return jobDefDoc;
	}

	public synchronized static JobDefinitionDocument buildJSDLInstance(
			JobExecutionContext context, String smsUrl) throws Exception {

		JobDefinitionDocument jobDefDoc = JobDefinitionDocument.Factory
				.newInstance();
		JobDefinitionType value = jobDefDoc.addNewJobDefinition();

		HpcApplicationDeploymentType appDepType = (HpcApplicationDeploymentType) context
				.getApplicationContext().getApplicationDeploymentDescription()
				.getType();

		// build Identification
		createJobIdentification(value, appDepType);

		ResourceProcessor.generateResourceElements(value, context);

		ApplicationProcessor.generateJobSpecificAppElements(value, context);

		UASDataStagingProcessor.generateDataStagingElements(value, context,
				smsUrl);

		return jobDefDoc;
	}

	public synchronized static JobDefinitionDocument buildJSDLInstance(
			JobExecutionContext context, DataServiceInfo dataInfo)
			throws Exception {

		JobDefinitionDocument jobDefDoc = JobDefinitionDocument.Factory
				.newInstance();
		JobDefinitionType value = jobDefDoc.addNewJobDefinition();

		HpcApplicationDeploymentType appDepType = (HpcApplicationDeploymentType) context
				.getApplicationContext().getApplicationDeploymentDescription()
				.getType();

		createJobIdentification(value, appDepType);

		ResourceProcessor.generateResourceElements(value, context);

		ApplicationProcessor.generateJobSpecificAppElements(value, context);

		switch (dataInfo.getDirectoryAccesMode()) {
		case SMSBYTEIO:
			if(null == dataInfo.getDataServiceUrl() || "".equals(dataInfo.getDataServiceUrl()))
				throw new Exception("No SMS address found");
			UASDataStagingProcessor.generateDataStagingElements(value, context,
					dataInfo.getDataServiceUrl());
			break;
		case RNSBYTEIO:
		case GridFTP:
		default:
			DataStagingProcessor.generateDataStagingElements(value, context);
			break;

		}
		return jobDefDoc;
	}

	public synchronized static JobDefinitionDocument buildJSDLInstance(
			JobExecutionContext context, String smsUrl, Object jobDirectoryMode)
			throws Exception {

		JobDefinitionDocument jobDefDoc = JobDefinitionDocument.Factory
				.newInstance();
		JobDefinitionType value = jobDefDoc.addNewJobDefinition();

		HpcApplicationDeploymentType appDepType = (HpcApplicationDeploymentType) context
				.getApplicationContext().getApplicationDeploymentDescription()
				.getType();

		// build Identification
		createJobIdentification(value, appDepType);

		ResourceProcessor.generateResourceElements(value, context);

		ApplicationProcessor.generateJobSpecificAppElements(value, context);

		UASDataStagingProcessor.generateDataStagingElements(value, context,
				smsUrl);

		return jobDefDoc;
	}

	private static void createJobIdentification(JobDefinitionType value,
			HpcApplicationDeploymentType appDepType) {
		if (appDepType.getProjectAccount() != null) {

			if (appDepType.getProjectAccount().getProjectAccountNumber() != null)
				JSDLUtils.addProjectName(value, appDepType.getProjectAccount()
						.getProjectAccountNumber());

			if (appDepType.getProjectAccount().getProjectAccountDescription() != null)
				JSDLUtils.getOrCreateJobIdentification(value).setDescription(
						appDepType.getProjectAccount()
								.getProjectAccountDescription());
		}
	}

}