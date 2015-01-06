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

package org.apache.airavata.api.server.util;

import java.sql.SQLException;
import java.util.List;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.airavata.appcatalog.cpi.ApplicationInterface;
import org.apache.aiaravata.application.catalog.data.impl.AppCatalogFactory;
import org.apache.airavata.model.util.ExecutionType;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.workflow.catalog.WorkflowCatalogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataModelUtils {
    private static final Logger logger = LoggerFactory.getLogger(DataModelUtils.class);
	public static ExecutionType getExecutionType(Experiment experiment) throws Exception{
		try {
			ApplicationInterface applicationInterface = AppCatalogFactory.getAppCatalog().getApplicationInterface();
			List<String> allApplicationInterfaceIds = applicationInterface.getAllApplicationInterfaceIds();
			String applicationId = experiment.getApplicationId();
			if (allApplicationInterfaceIds.contains(applicationId)){
				return ExecutionType.SINGLE_APP;
			} else {
				List<String> allWorkflows = WorkflowCatalogFactory.getWorkflowCatalog().getAllWorkflows();
				if (allWorkflows.contains(applicationId)){
					return ExecutionType.WORKFLOW;
				}
			}
		} catch (AppCatalogException e) {
            logger.error("Error while retrieving execution type for experiment : " + experiment.getExperimentID(), e);
            throw new Exception("Error while retrieving execution type for experiment : " + experiment.getExperimentID(), e);
		}
		return ExecutionType.UNKNOWN;
	}
}
