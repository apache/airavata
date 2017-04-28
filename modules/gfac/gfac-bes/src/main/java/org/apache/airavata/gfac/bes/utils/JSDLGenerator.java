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
package org.apache.airavata.gfac.bes.utils;


import org.apache.airavata.gfac.core.context.ApplicationContext;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
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

	public synchronized static JobDefinitionDocument buildJSDLInstance(JobExecutionContext context) throws Exception {

		JobDefinitionDocument jobDefDoc = JobDefinitionDocument.Factory
				.newInstance();
		JobDefinitionType value = jobDefDoc.addNewJobDefinition();

		
		// build Identification
		createJobIdentification(value, context);

		ResourceProcessor.generateResourceElements(value, context);

		ApplicationProcessor.generateJobSpecificAppElements(value, context);

		
		return jobDefDoc;
	}

	public synchronized static JobDefinitionDocument buildJSDLInstance(JobExecutionContext context, String smsUrl) throws Exception {

		JobDefinitionDocument jobDefDoc = JobDefinitionDocument.Factory
				.newInstance();
		JobDefinitionType value = jobDefDoc.addNewJobDefinition();

		
		// build Identification
		createJobIdentification(value, context);

		ResourceProcessor.generateResourceElements(value, context);

		ApplicationProcessor.generateJobSpecificAppElements(value, context);

		UASDataStagingProcessor.generateDataStagingElements(value, context, smsUrl);

		return jobDefDoc;
	}

	public synchronized static JobDefinitionDocument buildJSDLInstance(
			JobExecutionContext context, String smsUrl, Object jobDirectoryMode)
			throws Exception {

		JobDefinitionDocument jobDefDoc = JobDefinitionDocument.Factory
				.newInstance();
		JobDefinitionType value = jobDefDoc.addNewJobDefinition();

		// build Identification
		createJobIdentification(value, context);

		ResourceProcessor.generateResourceElements(value, context);

		ApplicationProcessor.generateJobSpecificAppElements(value, context);

		UASDataStagingProcessor.generateDataStagingElements(value, context,
				smsUrl);

		return jobDefDoc;
	}

	private static void createJobIdentification(JobDefinitionType value, JobExecutionContext context) {
		ApplicationContext appCtxt = context.getApplicationContext();
		
		if (appCtxt != null) {
			if (appCtxt.getComputeResourcePreference() != null && appCtxt.getComputeResourcePreference().getAllocationProjectNumber() != null)
				JSDLUtils.addProjectName(value, appCtxt.getComputeResourcePreference().getAllocationProjectNumber());
			
			if (appCtxt.getApplicationInterfaceDescription() != null && appCtxt.getApplicationInterfaceDescription().getApplicationDescription() != null)
				JSDLUtils.getOrCreateJobIdentification(value).setDescription(appCtxt.getApplicationInterfaceDescription().getApplicationDescription());				
			
			if (appCtxt.getApplicationInterfaceDescription() != null && appCtxt.getApplicationInterfaceDescription().getApplicationName() != null)
				JSDLUtils.getOrCreateJobIdentification(value).setJobName(appCtxt.getApplicationInterfaceDescription().getApplicationName());
		}
	}

}