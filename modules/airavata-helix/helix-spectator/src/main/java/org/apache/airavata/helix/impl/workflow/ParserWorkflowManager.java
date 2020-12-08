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
 */
package org.apache.airavata.helix.impl.workflow;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.impl.task.parsing.ProcessCompletionMessage;
import org.apache.airavata.helix.impl.task.parsing.kafka.ProcessCompletionMessageDeserializer;
import org.apache.airavata.helix.impl.task.parsing.*;
import org.apache.airavata.helix.impl.task.parsing.models.ParsingTaskInput;
import org.apache.airavata.helix.impl.task.parsing.models.ParsingTaskInputs;
import org.apache.airavata.helix.impl.task.parsing.models.ParsingTaskOutput;
import org.apache.airavata.helix.impl.task.parsing.models.ParsingTaskOutputs;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.parser.*;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.patform.monitoring.CountMonitor;
import org.apache.airavata.patform.monitoring.MonitoringServer;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.thrift.TException;
import org.apache.airavata.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Workflow Manager which will create and launch Data Parsing DAGs
 *
 * @since 1.0.0-SNAPSHOT
 */
public class ParserWorkflowManager extends WorkflowManager {

    private final static Logger logger = LoggerFactory.getLogger(ParserWorkflowManager.class);
    private final static CountMonitor parserwfCounter = new CountMonitor("parser_wf_counter");

    private String parserStorageResourceId = ServerSettings.getSetting("parser.storage.resource.id");

    public ParserWorkflowManager() throws ApplicationSettingsException {
        super(ServerSettings.getSetting("parser.workflow.manager.name"),
                Boolean.parseBoolean(ServerSettings.getSetting("post.workflow.manager.loadbalance.clusters")));
    }

    public static void main(String[] args) throws Exception {

        if (ServerSettings.getBooleanSetting("parser.workflow.manager.monitoring.enabled")) {
            MonitoringServer monitoringServer = new MonitoringServer(
                    ServerSettings.getSetting("parser.workflow.manager.monitoring.host"),
                    ServerSettings.getIntSetting("parser.workflow.manager.monitoring.port"));
            monitoringServer.start();

            Runtime.getRuntime().addShutdownHook(new Thread(monitoringServer::stop));
        }

        ParserWorkflowManager manager = new ParserWorkflowManager();
        manager.init();
        manager.runConsumer();
    }

    private void init() throws Exception {
        super.initComponents();
    }

    private boolean process(ProcessCompletionMessage completionMessage) {

        RegistryService.Client registryClient = getRegistryClientPool().getResource();

        try {
            ProcessModel processModel;
            ApplicationInterfaceDescription appDescription;
            try {
                processModel = registryClient.getProcess(completionMessage.getProcessId());
                appDescription = registryClient.getApplicationInterface(processModel.getApplicationInterfaceId());

            } catch (Exception e) {
                logger.error("Failed to fetch process or application description from registry associated with process id " + completionMessage.getProcessId(), e);
                throw new Exception("Failed to fetch process or application description from registry associated with process id " + completionMessage.getProcessId(), e);
            }

            // All the templates should be run
            // FIXME is it ApplicationInterfaceId or ApplicationName
            List<ParsingTemplate> parsingTemplates = registryClient.getParsingTemplatesForExperiment(completionMessage.getExperimentId(),
                    completionMessage.getGatewayId());

            logger.info("Found " + parsingTemplates.size() + " parsing template for experiment " + completionMessage.getExperimentId());

            Map<String, Map<String, Set<ParserConnector>>> parentToChildParsers = new HashMap<>();

            for (ParsingTemplate template : parsingTemplates) {
                for (ParserConnector connector: template.getParserConnections()) {

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
                ParserInput parserInput = registryClient.getParserInput(template.getInitialInputs().get(0).getTargetInputId(), template.getGatewayId());
                String parentParserId = parserInput.getParserId();

                if (!parentToChildParsers.isEmpty()) {
                    for (String parentId : parentToChildParsers.get(template.getId()).keySet()) {
                        boolean found = false;
                        for (Set<ParserConnector> dagElements : parentToChildParsers.get(template.getId()).values()) {
                            Optional<ParserConnector> first = dagElements.stream().filter(dagElement -> dagElement.getChildParserId().equals(parentId)).findFirst();
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

                if (parentParserId == null ) {
                    throw  new Exception("Could not find a parent parser for template " + template.getId());
                }

                Parser parentParser = registryClient.getParser(parentParserId, completionMessage.getGatewayId());

                DataParsingTask parentParserTask = createParentTask(parentParser, completionMessage, template.getInitialInputs(), registryClient);

                List<AbstractTask> allTasks = new ArrayList<>();
                allTasks.add(parentParserTask);

                if (parentToChildParsers.containsKey(template.getId())) {
                    createParserDagRecursively(allTasks, parentParser, parentParserTask, parentToChildParsers.get(template.getId()), completionMessage, registryClient);
                }
                String workflow = getWorkflowOperator().launchWorkflow("Parser-" + completionMessage.getProcessId() + UUID.randomUUID().toString(),
                    allTasks, true, false);
                // TODO: figure out processId and register
                // registerWorkflowForProcess(processId, workflow, "PARSER");
                logger.info("Launched workflow " + workflow);
                parserwfCounter.inc();
            }

            getRegistryClientPool().returnResource(registryClient);


            return true;


        } catch (Exception e) {
            logger.error("Failed to create the DataParsing task DAG", e);
            getRegistryClientPool().returnBrokenResource(registryClient);
            return false;
        }
    }

    private DataParsingTask createParentTask(Parser parserInfo, ProcessCompletionMessage completionMessage,
                                             List<ParsingTemplateInput> templateInputs, RegistryService.Client registryClient) throws Exception {
        DataParsingTask parsingTask = new DataParsingTask();
        parsingTask.setTaskId(normalizeTaskId(completionMessage.getExperimentId() + "-" + parserInfo.getId() + "-" + UUID.randomUUID().toString()));
        parsingTask.setGatewayId(completionMessage.getGatewayId());
        parsingTask.setParserId(parserInfo.getId());
        parsingTask.setLocalDataDir("/tmp");
        try {
            parsingTask.setGroupResourceProfileId(registryClient.getProcess(completionMessage.getProcessId()).getGroupResourceProfileId());
        } catch (TException e) {
            logger.error("Failed while fetching process model for process id  " + completionMessage.getProcessId());
            throw new Exception("Failed while fetching process model for process id  " + completionMessage.getProcessId());
        }

        ParsingTaskInputs inputs = new ParsingTaskInputs();

        for (ParsingTemplateInput templateInput : templateInputs) {

            Optional<ParserInput> parserInputOp = parserInfo.getInputFiles().stream()
                    .filter(inp -> inp.getId().equals(templateInput.getTargetInputId())).findFirst();

            ParsingTaskInput input = new ParsingTaskInput();
            input.setId(templateInput.getTargetInputId());

            if (parserInputOp.isPresent()) {
                input.setType(parserInputOp.get().getType().name());
                input.setName(parserInputOp.get().getName());
            } else {
                throw new Exception("Failed to find an input with id " + templateInput.getTargetInputId());
            }

            if(templateInput.getApplicationOutputName() != null) {
                String applicationOutputName = templateInput.getApplicationOutputName();
                try {
                    ExperimentModel experiment = registryClient.getExperiment(completionMessage.getExperimentId());
                    Optional<OutputDataObjectType> expOutputData;
                    if (applicationOutputName.contains("*")) {
                        expOutputData = experiment.getExperimentOutputs().stream()
                                .filter(outputDataObjectType -> isWildcardMatch(outputDataObjectType.getName(),applicationOutputName)).findFirst();
                    } else {
                        expOutputData = experiment.getExperimentOutputs().stream()
                                .filter(outputDataObjectType -> outputDataObjectType.getName().equals(applicationOutputName)).findFirst();
                    }
                    if (expOutputData.isPresent()) {
                        input.setValue(expOutputData.get().getValue());
                    } else {
                        throw new Exception("Could not find an experiment output with name " + applicationOutputName);
                    }
                } catch (TException e) {
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
            output.setUploadDirectory("parsed-data/" + completionMessage.getExperimentId() + "/" + completionMessage.getProcessId());
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
                i = iIndex+1;
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
        RegistryService.Client registryClient = getRegistryClientPool().getResource();

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
                            return registryClient.getProcess(completionMessage.getProcessId()).getUserName();
                        case "{{project}}":
                            return registryClient.getExperiment(completionMessage.getExperimentId()).getProjectId();
                    }
                }
            }
            getRegistryClientPool().returnResource(registryClient);
            return expression;
        } catch (Exception e) {
            getRegistryClientPool().returnBrokenResource(registryClient);
            throw new Exception("Failed to resolve expression " + expression, e);
        }
    }

    private void createParserDagRecursively(List<AbstractTask> allTasks, Parser parentParserInfo, DataParsingTask parentTask, Map<String, Set<ParserConnector>> parentToChild,
                                            ProcessCompletionMessage completionMessage, RegistryService.Client registryClient) throws Exception {
        if (parentToChild.containsKey(parentParserInfo.getId())) {

            for (ParserConnector connector : parentToChild.get(parentParserInfo.getId())) {
                Parser childParserInfo = registryClient.getParser(connector.getChildParserId(), completionMessage.getGatewayId());
                DataParsingTask parsingTask = new DataParsingTask();
                parsingTask.setTaskId(normalizeTaskId(completionMessage.getExperimentId() + "-" + childParserInfo.getId() + "-" + UUID.randomUUID().toString()));
                parsingTask.setGatewayId(completionMessage.getGatewayId());
                parsingTask.setParserId(childParserInfo.getId());
                parsingTask.setLocalDataDir("/tmp");
                try {
                    parsingTask.setGroupResourceProfileId(registryClient.getProcess(completionMessage.getProcessId()).getGroupResourceProfileId());
                } catch (TException e) {
                    logger.error("Failed while fetching process model for process id  " + completionMessage.getProcessId());
                    throw new Exception("Failed while fetching process model for process id  " + completionMessage.getProcessId());
                }

                ParsingTaskInputs inputs = new ParsingTaskInputs();
                for(ParserConnectorInput connectorInput : connector.getConnectorInputs()) {

                    Optional<ParserInput> parserInputOp = childParserInfo.getInputFiles().stream()
                            .filter(inp -> inp.getId().equals(connectorInput.getInputId())).findFirst();

                    if (parserInputOp.isPresent()) {
                        ParsingTaskInput input = new ParsingTaskInput();
                        // Either context variable or value is set
                        input.setName(parserInputOp.get().getName());
                        if (connectorInput.getParentOutputId() != null) {
                            input.setContextVariableName(connector.getParentParserId() + "-" + connectorInput.getParentOutputId());
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
                    output.setUploadDirectory("parsed-data/" + completionMessage.getExperimentId() + "/" + completionMessage.getProcessId());
                    outputs.addOutput(output);
                });

                parsingTask.setParsingTaskInputs(inputs);
                parsingTask.setParsingTaskOutputs(outputs);
                parentTask.setNextTask(new OutPort(parsingTask.getTaskId(), parentTask));
                allTasks.add(parsingTask);

                createParserDagRecursively(allTasks, childParserInfo, parsingTask, parentToChild, completionMessage, registryClient);
            }
        }
    }

    private void runConsumer() throws ApplicationSettingsException {

        final Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ServerSettings.getSetting("kafka.parsing.broker.url"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, ServerSettings.getSetting("kafka.parser.broker.consumer.group"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ProcessCompletionMessageDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        final Consumer<String, ProcessCompletionMessage> consumer = new KafkaConsumer<>(props);

        consumer.subscribe(Collections.singletonList(ServerSettings.getSetting("kafka.parser.topic")));

        logger.info("Starting the kafka consumer..");

        while (true) {
            final ConsumerRecords<String, ProcessCompletionMessage> consumerRecords = consumer.poll(Long.MAX_VALUE);

            for (TopicPartition partition : consumerRecords.partitions()) {
                List<ConsumerRecord<String, ProcessCompletionMessage>> partitionRecords = consumerRecords.records(partition);
                for (ConsumerRecord<String, ProcessCompletionMessage> record : partitionRecords) {
                    boolean success = process(record.value());
                    logger.info("Status of processing parser for experiment : " + record.value().getExperimentId() + " : " + success);
                    if (success) {
                        consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(record.offset() + 1)));
                    }
                }
            }

            consumerRecords.forEach(record -> process(record.value()));
            consumer.commitAsync();
        }
    }

}
