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

package org.apache.airavata.client;

import java.util.List;

import org.apache.airavata.client.api.ExperimentAdvanceOptions;
import org.apache.airavata.client.api.NodeSettings;
import org.apache.airavata.client.api.OutputDataSettings;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.client.tools.NameValuePairType;
import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;

public class AiravataAPIUtils {

	//------------------Deprecated Functions---------------------//
	
	public static WorkflowContextHeaderBuilder createWorkflowContextHeader()
			throws AiravataAPIInvocationException {
		try {
			return new WorkflowContextHeaderBuilder(null,
					null,null,null,null,null);
		} catch (Exception e) {
			throw new AiravataAPIInvocationException(e);
		}
	}
	
	//------------------End of Deprecated Functions---------------------//

	public static WorkflowContextHeaderBuilder createWorkflowContextHeaderBuilder(
			ExperimentAdvanceOptions options, String executionUser, String submissionUser)
			throws AiravataAPIInvocationException {
		WorkflowContextHeaderBuilder builder=createWorkflowContextHeader();
		builder.setUserIdentifier(executionUser);
		builder.setSubmissionUser(submissionUser);
		NodeSettings[] nodeSettingsList = options.getCustomWorkflowSchedulingSettings().getNodeSettingsList();
		for (NodeSettings nodeSettings : nodeSettingsList) {
			List<NameValuePairType> nameValuePairTypes = nodeSettings.getNameValuePair();
			for (NameValuePairType nameValuePairType : nameValuePairTypes) {
				builder.addApplicationSchedulingKeyPair(nodeSettings.getNodeId(),nameValuePairType.getName(), nameValuePairType.getValue(), nameValuePairType.getDescription());
			}
			builder.addApplicationSchedulingContext(nodeSettings.getNodeId(), nodeSettings.getServiceId(), nodeSettings.getHostSettings().getHostId(), nodeSettings.getHostSettings().isWSGRAMPreffered(), nodeSettings.getHostSettings().getGatekeeperEPR(), nodeSettings.getHPCSettings().getJobManager(), nodeSettings.getHPCSettings().getCPUCount(), nodeSettings.getHPCSettings().getNodeCount(), nodeSettings.getHPCSettings().getQueueName(), nodeSettings.getHPCSettings().getMaxWallTime());
		
		}
		OutputDataSettings[] outputDataSettingsList = options.getCustomWorkflowOutputDataSettings().getOutputDataSettingsList();
		for (OutputDataSettings outputDataSettings : outputDataSettingsList) {
			builder.addApplicationOutputDataHandling(outputDataSettings.getNodeId(),outputDataSettings.getOutputDataDirectory(), outputDataSettings.getDataRegistryUrl(), outputDataSettings.isDataPersistent());
		}

        if (options.getCustomSecuritySettings().getAmazonWSSettings().getAccessKeyId() != null) {
            builder.setAmazonWebServices(options.getCustomSecuritySettings().getAmazonWSSettings().getAccessKeyId(),
                    options.getCustomSecuritySettings().getAmazonWSSettings().getSecretAccessKey());
        }

        if (options.getCustomSecuritySettings().getCredentialStoreSecuritySettings() != null) {
            builder.setCredentialManagementService(options.getCustomSecuritySettings().
                    getCredentialStoreSecuritySettings().getTokenId(),
                    executionUser);
        }


		return builder;
	}
}
