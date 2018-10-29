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
import org.apache.airavata.helix.impl.task.completing.ProcessCompletionMessage;
import org.apache.airavata.helix.impl.task.completing.kafka.ProcessCompletionMessageDeserializer;
import org.apache.airavata.helix.impl.task.parsing.*;
import org.apache.airavata.helix.impl.task.parsing.models.ParsingTaskInput;
import org.apache.airavata.helix.impl.task.parsing.models.ParsingTaskInputs;
import org.apache.airavata.helix.impl.task.parsing.models.ParsingTaskOutput;
import org.apache.airavata.helix.impl.task.parsing.models.ParsingTaskOutputs;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.parser.DagElement;
import org.apache.airavata.model.appcatalog.parser.ParserInfo;
import org.apache.airavata.model.appcatalog.parser.ParsingTemplate;
import org.apache.airavata.model.appcatalog.parser.ParsingTemplateInput;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.thrift.TException;
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

    private String parserStorageResourceId = "pgadev.scigap.org_7ddf28fd-d503-4ff8-bbc5-3279a7c3b99e";

    public ParserWorkflowManager() throws ApplicationSettingsException {
        super(ServerSettings.getSetting("parser.workflow.manager.name"));
    }

    public static void main(String[] args) throws Exception {
        ParserWorkflowManager manager = new ParserWorkflowManager();
        manager.init();
        manager.test();
    }

    private void init() throws Exception {
        super.initComponents();
    }

    private boolean process(ProcessCompletionMessage completionMessage) {
        try {

            RegistryService.Client registryClient = getRegistryClientPool().getResource();
            ProcessModel processModel;
            ApplicationInterfaceDescription appDescription;
            try {
                processModel = registryClient.getProcess(completionMessage.getProcessId());
                appDescription = registryClient.getApplicationInterface(processModel.getApplicationInterfaceId());

            } catch (Exception e) {
                logger.error("Failed to fetch process or application description from registry associated with process id " + completionMessage.getProcessId(), e);
                throw new Exception("Failed to fetch process or application description from registry associated with process id " + completionMessage.getProcessId(), e);
            } finally {
                getRegistryClientPool().returnResource(registryClient);
            }

            // All the templates should be run
            // FIXME is it ApplicationInterfaceId or ApplicationName
            List<ParsingTemplate> parsingTemplates = registryClient.getParsingTemplatesForExperiment(completionMessage.getExperimentId());

            Map<String, Map<String, Set<DagElement>>> parentToChildParsers = new HashMap<>();
            Map<String, Map<String, Set<String>>> childToParentParsers = new HashMap<>();

            for (ParsingTemplate template : parsingTemplates) {
                for (DagElement dagElement: template.getParserDag()) {

                    Map<String, Set<DagElement>> parentToChildLocal = new HashMap<>();
                    if (parentToChildParsers.containsKey(template.getId())) {
                        parentToChildLocal = parentToChildParsers.get(template.getId());
                    } else {
                        parentToChildParsers.put(template.getId(), parentToChildLocal);
                    }

                    Set<DagElement> childLocal = new HashSet<>();
                    if (parentToChildLocal.containsKey(dagElement.getParentParserId())) {
                        childLocal = parentToChildLocal.get(dagElement.getParentParserId());
                    } else {
                        parentToChildLocal.put(dagElement.getParentParserId(), childLocal);
                    }

                    childLocal.add(dagElement);

                    Map<String, Set<String>> childToParentLocal = new HashMap<>();
                    if (childToParentParsers.containsKey(template.getId())) {
                        childToParentLocal = childToParentParsers.get(template.getId());
                    } else {
                        childToParentParsers.put(template.getId(), childToParentLocal);
                    }

                    Set<String> parentLocal = new HashSet<>();
                    if (childToParentLocal.containsKey(dagElement.getChildParserId())) {
                        parentLocal = childToParentLocal.get(dagElement.getChildParserId());
                    } else {
                        childToParentLocal.put(dagElement.getChildParserId(), parentLocal);
                    }

                    parentLocal.add(dagElement.getParentParserId());
                }
            }

            for (ParsingTemplate template : parsingTemplates) {

                String parentParserId = null;
                for (String parentId : parentToChildParsers.get(template.getId()).keySet()) {
                    boolean found = false;
                    for (Set<DagElement> dagElements : parentToChildParsers.get(template.getId()).values()) {
                        Optional<DagElement> first = dagElements.stream().filter(dagElement -> dagElement.getChildParserId().equals(parentId)).findFirst();
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

                if (parentParserId == null ) {
                    throw  new Exception("Could not find a parent parser for template " + template.getId());
                }

                ParserInfo parentParserInfo = registryClient.getParserInfo(parentParserId);

                DataParsingTask parentParserTask = createParentTask(parentParserInfo, completionMessage, template.getInitialInputs(), registryClient);

                List<AbstractTask> allTasks = new ArrayList<>();
                allTasks.add(parentParserTask);
                createParserDagRecursively(allTasks, parentParserInfo, parentParserTask, parentToChildParsers.get(template.getId()), completionMessage, registryClient);

                String workflow = getWorkflowOperator().launchWorkflow("Parser-" + completionMessage.getProcessId() + UUID.randomUUID().toString(),
                        allTasks, true, false);
                logger.info("Launched workflow " + workflow);
            }
            return true;


        } catch (Exception e) {
            logger.error("Failed to create the DataParsing task DAG", e);
            return false;
        }
    }

    private DataParsingTask createParentTask(ParserInfo parserInfo, ProcessCompletionMessage completionMessage,
                                             List<ParsingTemplateInput> templateInputs, RegistryService.Client registryClient) throws Exception {
        DataParsingTask parsingTask = new DataParsingTask();
        parsingTask.setTaskId(normalizeTaskId(completionMessage.getExperimentId() + "-" + parserInfo.getId() + "-" + UUID.randomUUID().toString()));
        parsingTask.setGatewayId(completionMessage.getGatewayId());
        parsingTask.setParserInfoId(parserInfo.getId());
        parsingTask.setLocalDataDir("/tmp");
        try {
            parsingTask.setGroupResourceProfileId(registryClient.getProcess(completionMessage.getProcessId()).getGroupResourceProfileId());
        } catch (TException e) {
            logger.error("Failed while fetching process model for process id  " + completionMessage.getProcessId());
            throw new Exception("Failed while fetching process model for process id  " + completionMessage.getProcessId());
        }

        ParsingTaskInputs inputs = new ParsingTaskInputs();

        for (ParsingTemplateInput templateInput : templateInputs) {
            String expression = templateInput.getExpression();
            try {
                ExperimentModel experiment = registryClient.getExperiment(completionMessage.getExperimentId());
                Optional<OutputDataObjectType> outputDataObj = experiment.getExperimentOutputs().stream().filter(outputDataObjectType -> outputDataObjectType.getName().equals(expression)).findFirst();
                if (outputDataObj.isPresent()) {
                    ParsingTaskInput input = new ParsingTaskInput();
                    input.setId(templateInput.getInputId());
                    input.setValue(outputDataObj.get().getValue());
                    inputs.addInput(input);
                }
            } catch (TException e) {
                logger.error("Failed while fetching experiment " + completionMessage.getExperimentId());
                throw new Exception("Failed while fetching experiment " + completionMessage.getExperimentId());
            }
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

    private void createParserDagRecursively(List<AbstractTask> allTasks, ParserInfo parentParserInfo, DataParsingTask parentTask, Map<String, Set<DagElement>> parentToChild,
                                            ProcessCompletionMessage completionMessage, RegistryService.Client registryClient) throws Exception {
        if (parentToChild.containsKey(parentParserInfo.getId())) {

            for (DagElement dagElement : parentToChild.get(parentParserInfo.getId())) {
                ParserInfo childParserInfo = registryClient.getParserInfo(dagElement.getChildParserId());
                DataParsingTask parsingTask = new DataParsingTask();
                parsingTask.setTaskId(normalizeTaskId(completionMessage.getExperimentId() + "-" + childParserInfo.getId() + "-" + UUID.randomUUID().toString()));
                parsingTask.setGatewayId(completionMessage.getGatewayId());
                parsingTask.setParserInfoId(childParserInfo.getId());
                parsingTask.setLocalDataDir("/tmp");
                try {
                    parsingTask.setGroupResourceProfileId(registryClient.getProcess(completionMessage.getProcessId()).getGroupResourceProfileId());
                } catch (TException e) {
                    logger.error("Failed while fetching process model for process id  " + completionMessage.getProcessId());
                    throw new Exception("Failed while fetching process model for process id  " + completionMessage.getProcessId());
                }

                ParsingTaskInputs inputs = new ParsingTaskInputs();
                dagElement.getInputOutputMapping().forEach(mapping -> {
                    ParsingTaskInput input = new ParsingTaskInput();
                    input.setContextVariableName(dagElement.getParentParserId() + "-" + mapping.getOutputId());
                    input.setId(mapping.getInputId());
                    inputs.addInput(input);
                });

                ParsingTaskOutputs outputs = new ParsingTaskOutputs();
                childParserInfo.getOutputFiles().forEach(parserOutput -> {
                    ParsingTaskOutput output = new ParsingTaskOutput();
                    output.setContextVariableName(dagElement.getChildParserId() + "-" + parserOutput.getId());
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
        final Consumer<String, ProcessCompletionMessage> consumer = new KafkaConsumer<>(props);

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ServerSettings.getSetting("kafka.broker.url"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, ServerSettings.getSetting("kafka.broker.consumer.group"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ProcessCompletionMessageDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumer.subscribe(Collections.singletonList(ServerSettings.getSetting("kafka.parser.topic")));

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

    private void test() {
        ProcessCompletionMessage completionMessage = new ProcessCompletionMessage();
        completionMessage.setExperimentId("Echo_on_Oct_24,_2018_10:03_AM_b67b6a7e-d41f-46c2-a756-d8cf91155e1e");
        completionMessage.setGatewayId("seagrid");
        completionMessage.setProcessId("PROCESS_be7d1946-e404-447e-a7e8-5581aaef2ef8");
        process(completionMessage);
    }

}
