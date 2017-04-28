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
package org.apache.airavata.workflow.engine;

import org.apache.airavata.registry.cpi.WorkflowCatalog;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.impl.RabbitMQStatusPublisher;
import org.apache.airavata.model.error.AiravataClientConnectException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.orchestrator.client.OrchestratorClientFactory;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.airavata.registry.cpi.ExperimentCatalogModelType;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.workflow.catalog.WorkflowCatalogFactory;
import org.apache.airavata.workflow.engine.interpretor.WorkflowInterpreter;
import org.apache.airavata.workflow.engine.interpretor.WorkflowInterpreterConfiguration;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowEngineImpl implements WorkflowEngine {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowEngineImpl.class);
    private Publisher rabbitMQPublisher;
    WorkflowEngineImpl() {
        try {
            rabbitMQPublisher = new RabbitMQStatusPublisher();
        } catch (Exception e) {
            logger.error("Failed to instantiate RabbitMQPublisher", e);
        }
    }

	@Override
	public void launchExperiment(String experimentId, String token)
			throws WorkflowEngineException {
		try {
            ExperimentCatalog experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            Experiment experiment = (Experiment) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, experimentId);
            WorkflowCatalog workflowCatalog = WorkflowCatalogFactory.getWorkflowCatalog();
			WorkflowInterpreterConfiguration config = new WorkflowInterpreterConfiguration(new Workflow(workflowCatalog.getWorkflow(experiment.getApplicationId()).getGraph()));
			final WorkflowInterpreter workflowInterpreter = new WorkflowInterpreter(experiment, token, config , getOrchestratorClient(), rabbitMQPublisher);
			new Thread(){
				public void run() {
					try {
						workflowInterpreter.scheduleDynamically();
					} catch (WorkflowException e) {
                        logger.error(e.getMessage(), e);
					} catch (RegistryException e) {
                        logger.error(e.getMessage(), e);
					} catch (AiravataException e) {
                        logger.error(e.getMessage(), e);
                    }
                };
			}.start();
        } catch (Exception e) {
            logger.error("Error while retrieving the experiment", e);
            WorkflowEngineException exception = new WorkflowEngineException("Error while launching the workflow experiment. More info : " + e.getMessage());
            throw exception;
        }

	}

	private OrchestratorService.Client getOrchestratorClient() throws AiravataClientConnectException{
		final int serverPort = Integer.parseInt(ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ORCHESTRATOR_SERVER_PORT,"8940"));
        final String serverHost = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ORCHESTRATOR_SERVER_HOST, null);
       	return OrchestratorClientFactory.createOrchestratorClient(serverHost, serverPort);
	}
}
