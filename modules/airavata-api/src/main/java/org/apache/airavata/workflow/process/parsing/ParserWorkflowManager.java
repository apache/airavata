/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.workflow.process.parsing;

import io.dapr.workflows.client.DaprWorkflowClient;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.airavata.common.model.ApplicationInterfaceDescription;
import org.apache.airavata.common.model.ParserConnector;
import org.apache.airavata.common.model.ParserInput;
import org.apache.airavata.common.model.ParsingTemplate;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.orchestrator.ParsingHandler;
import org.apache.airavata.orchestrator.ProcessStatusUpdater;
import org.apache.airavata.orchestrator.messaging.MessagingFactory;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.task.parsing.ProcessCompletionMessage;
import org.apache.airavata.telemetry.CounterMetric;
import org.apache.airavata.workflow.common.WorkflowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Workflow Manager which will create and launch Data Parsing DAGs
 *
 * @since 1.0.0-SNAPSHOT
 */
@Component
@Profile("!test")
@ConditionalOnProperty(prefix = "airavata.services.controller", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "airavata.services.parser", name = "enabled", havingValue = "true")
public class ParserWorkflowManager extends WorkflowManager implements ParsingHandler {

    private static final Logger logger = LoggerFactory.getLogger(ParserWorkflowManager.class);
    private static final CounterMetric parserwfCounter = new CounterMetric("parser_wf_counter");

    private final AiravataServerProperties properties;
    private final ApplicationContext applicationContext;
    private final RegistryService registryService;
    private String parserStorageResourceId;

    @Autowired(required = false)
    private DaprWorkflowClient workflowClient;

    public ParserWorkflowManager(
            AiravataServerProperties properties,
            ApplicationContext applicationContext,
            RegistryService registryService,
            MessagingFactory messagingFactory,
            ProcessStatusUpdater statusUpdateHelper) {
        super("parser-workflow-manager", false, registryService, messagingFactory, statusUpdateHelper);
        this.properties = properties;
        this.applicationContext = applicationContext;
        this.registryService = registryService;
    }

    @jakarta.annotation.PostConstruct
    public void initWorkflowManager() {
        if (properties != null) {
            this.workflowManagerName = this.getClass().getSimpleName();
            this.loadBalanceClusters = properties.services().parser().loadBalanceClusters();
            this.parserStorageResourceId = properties.services().parser().storageResourceId();
        }
    }

    @Override
    public String getServerName() {
        return "Parser Workflow Manager";
    }

    @Override
    public String getServerVersion() {
        return "1.0";
    }

    @Override
    public int getPhase() {
        return 25; // Start after pre-workflow manager
    }

    @Override
    public void onParsingMessage(ProcessCompletionMessage message) {
        try {
            boolean success = process(message);
            logger.info("Status of processing parser for experiment {} : {}", message.getExperimentId(), success);
        } catch (Exception e) {
            logger.error("Error processing parsing message for experiment {}", message.getExperimentId(), e);
        }
    }

    @Override
    protected void doStart() throws Exception {
        init();
        logger.info("ParserWorkflowManager started; receives parsing triggers via ParsingHandler.onParsingMessage");
    }

    @Override
    protected void doStop() throws Exception {
        logger.info("ParserWorkflowManager stopped");
    }

    @Override
    public boolean isRunning() {
        return super.isRunning();
    }

    private void init() throws Exception {
        super.initComponents();
    }

    private boolean process(ProcessCompletionMessage completionMessage) {

        try {
            ProcessModel processModel;
            ApplicationInterfaceDescription appDescription;
            try {
                processModel = registryService.getProcess(completionMessage.getProcessId());
                appDescription = registryService.getApplicationInterface(processModel.getApplicationInterfaceId());

            } catch (Exception e) {
                logger.error(
                        "Failed to fetch process or application description from registry associated with process id "
                                + completionMessage.getProcessId(),
                        e);
                throw new Exception(
                        "Failed to fetch process or application description from registry associated with process id "
                                + completionMessage.getProcessId(),
                        e);
            }

            // All the templates should be run
            // getParsingTemplatesForExperiment uses ApplicationInterfaceId internally
            List<ParsingTemplate> parsingTemplates = registryService.getParsingTemplatesForExperiment(
                    completionMessage.getExperimentId(), completionMessage.getGatewayId());

            logger.info("Found " + parsingTemplates.size() + " parsing template for experiment "
                    + completionMessage.getExperimentId());

            Map<String, Map<String, Set<ParserConnector>>> parentToChildParsers = new HashMap<>();

            for (ParsingTemplate template : parsingTemplates) {
                for (ParserConnector connector : template.getParserConnections()) {

                    Map<String, Set<ParserConnector>> parentToChildLocal = new HashMap<>();
                    if (parentToChildParsers.containsKey(template.getId())) {
                        parentToChildLocal = parentToChildParsers.get(template.getId());
                    } else {
                        parentToChildParsers.put(template.getId(), parentToChildLocal);
                    }

                    Set<ParserConnector> childLocal = new HashSet<>();
                    if (parentToChildLocal.containsKey(connector.getParentParserId())) {
                        childLocal = parentToChildLocal.get(connector.getParentParserId());
                    } else {
                        parentToChildLocal.put(connector.getParentParserId(), childLocal);
                    }

                    if (!connector.getParentParserId().equals(connector.getChildParserId())) {
                        childLocal.add(connector);
                    }
                }
            }

            for (ParsingTemplate template : parsingTemplates) {

                logger.info("Launching parsing template " + template.getId());
                ParserInput parserInput = registryService.getParserInput(
                        template.getInitialInputs().get(0).getTargetInputId(), template.getGatewayId());
                String parentParserId = parserInput.getParserId();

                if (!parentToChildParsers.isEmpty()) {
                    for (String parentId :
                            parentToChildParsers.get(template.getId()).keySet()) {
                        boolean found = false;
                        for (Set<ParserConnector> dagElements :
                                parentToChildParsers.get(template.getId()).values()) {
                            Optional<ParserConnector> first = dagElements.stream()
                                    .filter(dagElement ->
                                            dagElement.getChildParserId().equals(parentId))
                                    .findFirst();
                            if (first.isPresent()) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            parentParserId = parentId;
                            break;
                        }
                    }
                }

                if (parentParserId == null) {
                    throw new Exception("Could not find a parent parser for template " + template.getId());
                }

                // Use durable workflow for parsing - workflows handle parser orchestration
                if (workflowClient != null) {
                    try {
                        // Schedule the workflow (returns instance ID)
                        logger.info("Scheduling ParsingWorkflow for process {}", completionMessage.getProcessId());
                        String workflowInstanceId =
                                workflowClient.scheduleNewWorkflow(ParsingWorkflow.class, completionMessage);

                        logger.info(
                                "Successfully scheduled ParsingWorkflow {} for process {}",
                                workflowInstanceId,
                                completionMessage.getProcessId());
                        parserwfCounter.inc();

                    } catch (Exception e) {
                        logger.error(
                                "Failed to schedule ParsingWorkflow for process {}",
                                completionMessage.getProcessId(),
                                e);
                        // Continue with other templates even if one fails
                    }
                } else {
                    // Fallback: log warning if workflow client is not available
                    logger.warn(
                            "Workflow client not available; cannot launch parsing workflow for process {}. "
                                    + "Enable airavata.services.controller.enabled=true",
                            completionMessage.getProcessId());
                    // Still log workflow name for compatibility
                    String workflow = org.apache.airavata.orchestrator.internal.workflow.WorkflowNaming.parsingWorkflow(
                            completionMessage.getProcessId());
                    logger.info("Launched workflow " + workflow);
                    parserwfCounter.inc();
                }
            }

            return true;

        } catch (Exception e) {
            logger.error("Failed to schedule parsing workflows", e);
            return false;
        }
    }
}
