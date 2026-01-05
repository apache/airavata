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
package org.apache.airavata.helix.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import org.apache.airavata.common.model.ApplicationInterfaceDescription;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.OutputDataObjectType;
import org.apache.airavata.common.model.Parser;
import org.apache.airavata.common.model.ParserConnector;
import org.apache.airavata.common.model.ParserConnectorInput;
import org.apache.airavata.common.model.ParserInput;
import org.apache.airavata.common.model.ParsingTemplate;
import org.apache.airavata.common.model.ParsingTemplateInput;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.helix.task.OutPort;
import org.apache.airavata.helix.task.TaskUtil;
import org.apache.airavata.helix.task.base.AbstractTask;
import org.apache.airavata.helix.task.parsing.DataParsingTask;
import org.apache.airavata.helix.task.parsing.ProcessCompletionMessage;
import org.apache.airavata.helix.task.parsing.kafka.ProcessCompletionMessageDeserializer;
import org.apache.airavata.helix.task.parsing.models.ParsingTaskInput;
import org.apache.airavata.helix.task.parsing.models.ParsingTaskInputs;
import org.apache.airavata.helix.task.parsing.models.ParsingTaskOutput;
import org.apache.airavata.helix.task.parsing.models.ParsingTaskOutputs;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.registry.exception.RegistryServiceException;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.telemetry.CounterMetric;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@ConditionalOnProperty(name = "services.controller.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "services.parser.enabled", havingValue = "true", matchIfMissing = true)
public class ParserWorkflowManager extends WorkflowManager {

    private static final Logger logger = LoggerFactory.getLogger(ParserWorkflowManager.class);
    private static final CounterMetric parserwfCounter = new CounterMetric("parser_wf_counter");

    private final AiravataServerProperties properties;
    private final ApplicationContext applicationContext;
    private final RegistryService registryService;
    private String parserStorageResourceId;
    private Thread consumerThread;
    private volatile Consumer<String, ProcessCompletionMessage> consumer;

    public ParserWorkflowManager(
            AiravataServerProperties properties,
            ApplicationContext applicationContext,
            RegistryService registryService,
            MessagingFactory messagingFactory,
            TaskUtil taskUtil) {
        super("parser-workflow-manager", false, registryService, properties, messagingFactory, taskUtil);
        this.properties = properties;
        this.applicationContext = applicationContext;
        this.registryService = registryService;
    }

    @jakarta.annotation.PostConstruct
    public void initWorkflowManager() {
        if (properties != null) {
            this.workflowManagerName = properties.services.parser.name;
            this.loadBalanceClusters = properties.services.parser.loadBalanceClusters;
            this.parserStorageResourceId = properties.services.parser.storageResourceId;
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
    protected void doStart() throws Exception {
        init();
        // Run consumer in background thread to keep it non-blocking
        consumerThread = new Thread(() -> {
            try {
                runConsumer();
            } catch (Exception e) {
                logger.error("Error in ParserWorkflowManager consumer thread", e);
            }
        });
        consumerThread.setName("ParserWorkflowManager-Consumer");
        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    @Override
    protected void doStop() throws Exception {
        if (consumer != null) {
            try {
                consumer.wakeup();
            } catch (Exception e) {
                logger.warn("Error waking up consumer", e);
            }
        }
        if (consumerThread != null) {
            consumerThread.interrupt();
            try {
                consumerThread.join(5000); // Wait up to 5 seconds
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for consumer thread to stop", e);
                Thread.currentThread().interrupt();
            }
        }
        if (consumer != null) {
            try {
                consumer.close();
            } catch (Exception e) {
                logger.warn("Error closing consumer", e);
            }
        }
    }

    @Override
    public boolean isRunning() {
        return super.isRunning() && consumerThread != null && consumerThread.isAlive();
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
            // FIXME is it ApplicationInterfaceId or ApplicationName
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

                Parser parentParser = registryService.getParser(parentParserId, completionMessage.getGatewayId());

                DataParsingTask parentParserTask =
                        createParentTask(parentParser, completionMessage, template.getInitialInputs(), registryService);

                List<AbstractTask> allTasks = new ArrayList<>();
                allTasks.add(parentParserTask);

                if (parentToChildParsers.containsKey(template.getId())) {
                    createParserDagRecursively(
                            allTasks,
                            parentParser,
                            parentParserTask,
                            parentToChildParsers.get(template.getId()),
                            completionMessage,
                            registryService);
                }
                String workflow = getWorkflowOperator()
                        .launchWorkflow(
                                "Parser-" + completionMessage.getProcessId()
                                        + UUID.randomUUID().toString(),
                                allTasks,
                                true,
                                false);
                // TODO: figure out processId and register
                // registerWorkflowForProcess(processId, workflow, "PARSER");
                logger.info("Launched workflow " + workflow);
                parserwfCounter.inc();
            }

            return true;

        } catch (Exception e) {
            logger.error("Failed to create the DataParsing task DAG", e);
            return false;
        }
    }

    private DataParsingTask createParentTask(
            Parser parserInfo,
            ProcessCompletionMessage completionMessage,
            List<ParsingTemplateInput> templateInputs,
            RegistryService registryService)
            throws Exception {
        DataParsingTask parsingTask = applicationContext.getBean(DataParsingTask.class);
        parsingTask.setTaskId(normalizeTaskId(completionMessage.getExperimentId() + "-" + parserInfo.getId() + "-"
                + UUID.randomUUID().toString()));
        parsingTask.setGatewayId(completionMessage.getGatewayId());
        parsingTask.setParserId(parserInfo.getId());
        parsingTask.setLocalDataDir("/tmp");
        try {
            parsingTask.setGroupResourceProfileId(
                    registryService.getProcess(completionMessage.getProcessId()).getGroupResourceProfileId());
        } catch (RegistryServiceException e) {
            logger.error("Failed while fetching process model for process id  " + completionMessage.getProcessId());
            throw new Exception(
                    "Failed while fetching process model for process id  " + completionMessage.getProcessId());
        }

        ParsingTaskInputs inputs = new ParsingTaskInputs();

        for (ParsingTemplateInput templateInput : templateInputs) {

            Optional<ParserInput> parserInputOp = parserInfo.getInputFiles().stream()
                    .filter(inp -> inp.getId().equals(templateInput.getTargetInputId()))
                    .findFirst();

            ParsingTaskInput input = new ParsingTaskInput();
            input.setId(templateInput.getTargetInputId());

            if (parserInputOp.isPresent()) {
                input.setType(parserInputOp.get().getType().name());
                input.setName(parserInputOp.get().getName());
            } else {
                throw new Exception("Failed to find an input with id " + templateInput.getTargetInputId());
            }

            if (templateInput.getApplicationOutputName() != null) {
                String applicationOutputName = templateInput.getApplicationOutputName();
                try {
                    ExperimentModel experiment = registryService.getExperiment(completionMessage.getExperimentId());
                    Optional<OutputDataObjectType> expOutputData;
                    if (applicationOutputName.contains("*")) {
                        expOutputData = experiment.getExperimentOutputs().stream()
                                .filter(outputDataObjectType ->
                                        isWildcardMatch(outputDataObjectType.getName(), applicationOutputName))
                                .findFirst();
                    } else {
                        expOutputData = experiment.getExperimentOutputs().stream()
                                .filter(outputDataObjectType ->
                                        outputDataObjectType.getName().equals(applicationOutputName))
                                .findFirst();
                    }
                    if (expOutputData.isPresent()) {
                        input.setValue(expOutputData.get().getValue());
                    } else {
                        throw new Exception("Could not find an experiment output with name " + applicationOutputName);
                    }
                } catch (RegistryServiceException e) {
                    logger.error("Failed while fetching experiment " + completionMessage.getExperimentId());
                    throw new Exception("Failed while fetching experiment " + completionMessage.getExperimentId());
                }
            } else {
                input.setValue(processExpression(templateInput.getValue(), completionMessage));
            }
            inputs.addInput(input);
        }

        parsingTask.setParsingTaskInputs(inputs);

        ParsingTaskOutputs outputs = new ParsingTaskOutputs();
        parserInfo.getOutputFiles().forEach(parserOutput -> {
            ParsingTaskOutput output = new ParsingTaskOutput();
            output.setContextVariableName(parserInfo.getId() + "-" + parserOutput.getId());
            output.setStorageResourceId(parserStorageResourceId);
            output.setId(parserOutput.getId());
            output.setUploadDirectory(
                    "parsed-data/" + completionMessage.getExperimentId() + "/" + completionMessage.getProcessId());
            outputs.addOutput(output);
        });
        parsingTask.setParsingTaskOutputs(outputs);

        return parsingTask;
    }

    private boolean isWildcardMatch(String s, String p) {
        int i = 0;
        int j = 0;
        int starIndex = -1;
        int iIndex = -1;

        while (i < s.length()) {
            if (j < p.length() && (p.charAt(j) == '?' || p.charAt(j) == s.charAt(i))) {
                ++i;
                ++j;
            } else if (j < p.length() && p.charAt(j) == '*') {
                starIndex = j;
                iIndex = i;
                j++;
            } else if (starIndex != -1) {
                j = starIndex + 1;
                i = iIndex + 1;
                iIndex++;
            } else {
                return false;
            }
        }
        while (j < p.length() && p.charAt(j) == '*') {
            ++j;
        }
        return j == p.length();
    }

    private String processExpression(String expression, ProcessCompletionMessage completionMessage) throws Exception {
        try {
            if (expression != null) {
                if (expression.startsWith("{{") && expression.endsWith("}}")) {
                    switch (expression) {
                        case "{{experiment}}":
                            return completionMessage.getExperimentId();
                        case "{{process}}":
                            return completionMessage.getProcessId();
                        case "{{gateway}}":
                            return completionMessage.getGatewayId();
                        case "{{user}}":
                            return registryService
                                    .getProcess(completionMessage.getProcessId())
                                    .getUserName();
                        case "{{project}}":
                            return registryService
                                    .getExperiment(completionMessage.getExperimentId())
                                    .getProjectId();
                    }
                }
            }
            return expression;
        } catch (Exception e) {
            throw new Exception("Failed to resolve expression " + expression, e);
        }
    }

    private void createParserDagRecursively(
            List<AbstractTask> allTasks,
            Parser parentParserInfo,
            DataParsingTask parentTask,
            Map<String, Set<ParserConnector>> parentToChild,
            ProcessCompletionMessage completionMessage,
            RegistryService registryService)
            throws Exception {
        if (parentToChild.containsKey(parentParserInfo.getId())) {

            for (ParserConnector connector : parentToChild.get(parentParserInfo.getId())) {
                Parser childParserInfo =
                        registryService.getParser(connector.getChildParserId(), completionMessage.getGatewayId());
                DataParsingTask parsingTask = applicationContext.getBean(DataParsingTask.class);
                parsingTask.setTaskId(normalizeTaskId(completionMessage.getExperimentId() + "-"
                        + childParserInfo.getId() + "-" + UUID.randomUUID().toString()));
                parsingTask.setGatewayId(completionMessage.getGatewayId());
                parsingTask.setParserId(childParserInfo.getId());
                parsingTask.setLocalDataDir("/tmp");
                try {
                    parsingTask.setGroupResourceProfileId(registryService
                            .getProcess(completionMessage.getProcessId())
                            .getGroupResourceProfileId());
                } catch (RegistryServiceException e) {
                    logger.error(
                            "Failed while fetching process model for process id  " + completionMessage.getProcessId());
                    throw new Exception(
                            "Failed while fetching process model for process id  " + completionMessage.getProcessId());
                }

                ParsingTaskInputs inputs = new ParsingTaskInputs();
                for (ParserConnectorInput connectorInput : connector.getConnectorInputs()) {

                    Optional<ParserInput> parserInputOp = childParserInfo.getInputFiles().stream()
                            .filter(inp -> inp.getId().equals(connectorInput.getInputId()))
                            .findFirst();

                    if (parserInputOp.isPresent()) {
                        ParsingTaskInput input = new ParsingTaskInput();
                        // Either context variable or value is set
                        input.setName(parserInputOp.get().getName());
                        if (connectorInput.getParentOutputId() != null) {
                            input.setContextVariableName(
                                    connector.getParentParserId() + "-" + connectorInput.getParentOutputId());
                        }
                        input.setValue(processExpression(connectorInput.getValue(), completionMessage));
                        input.setId(connectorInput.getInputId());
                        input.setType(parserInputOp.get().getType().name());
                        inputs.addInput(input);
                    } else {
                        throw new Exception("Failed to find an input with id " + connectorInput.getId());
                    }
                }

                ParsingTaskOutputs outputs = new ParsingTaskOutputs();
                childParserInfo.getOutputFiles().forEach(parserOutput -> {
                    ParsingTaskOutput output = new ParsingTaskOutput();
                    output.setContextVariableName(connector.getChildParserId() + "-" + parserOutput.getId());
                    output.setStorageResourceId(parserStorageResourceId);
                    output.setId(parserOutput.getId());
                    output.setUploadDirectory("parsed-data/" + completionMessage.getExperimentId() + "/"
                            + completionMessage.getProcessId());
                    outputs.addOutput(output);
                });

                parsingTask.setParsingTaskInputs(inputs);
                parsingTask.setParsingTaskOutputs(outputs);
                parentTask.setNextTask(new OutPort(parsingTask.getTaskId(), parsingTask));
                allTasks.add(parsingTask);

                createParserDagRecursively(
                        allTasks, childParserInfo, parsingTask, parentToChild, completionMessage, registryService);
            }
        }
    }

    private void runConsumer() {
        final Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.kafka.brokerUrl);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, properties.services.parser.brokerConsumerGroup);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ProcessCompletionMessageDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(properties.services.parser.topic));

        logger.info("Starting the kafka consumer..");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down Kafka consumer...");
            consumer.close();
        }));

        try {
            while (true) {
                final ConsumerRecords<String, ProcessCompletionMessage> consumerRecords = consumer.poll(Long.MAX_VALUE);

                for (TopicPartition partition : consumerRecords.partitions()) {
                    List<ConsumerRecord<String, ProcessCompletionMessage>> partitionRecords =
                            consumerRecords.records(partition);
                    for (ConsumerRecord<String, ProcessCompletionMessage> record : partitionRecords) {
                        boolean success = process(record.value());
                        logger.info("Status of processing parser for experiment : "
                                + record.value().getExperimentId() + " : " + success);
                        if (success) {
                            consumer.commitSync(
                                    Collections.singletonMap(partition, new OffsetAndMetadata(record.offset() + 1)));
                        }
                    }
                }

                consumerRecords.forEach(record -> process(record.value()));
                consumer.commitAsync();
            }
        } finally {
            consumer.close();
        }
    }
}
