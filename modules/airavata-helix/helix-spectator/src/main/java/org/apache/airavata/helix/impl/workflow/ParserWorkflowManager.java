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
import org.apache.airavata.helix.core.util.MonitoringUtil;
import org.apache.airavata.helix.impl.task.parsing.*;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.monitor.kafka.JobStatusResultDeserializer;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
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

    public ParserWorkflowManager() throws ApplicationSettingsException {
        super(ServerSettings.getSetting("parser.workflow.manager.name"));
    }

    public static void main(String[] args) throws Exception {
        ParserWorkflowManager manager = new ParserWorkflowManager();
        manager.init();
        manager.runConsumer();
    }

    private void init() throws Exception {
        super.initComponents();
    }

    private boolean process(JobStatusResult jobStatusResult) {
        try {
            if (MonitoringUtil.hasMonitoringRegistered(getCuratorClient(), jobStatusResult.getJobId())) {

                String processId = Optional.ofNullable(MonitoringUtil.getProcessIdByJobId(getCuratorClient(), jobStatusResult.getJobId()))
                        .orElseThrow(() -> new Exception("Can not find the process for job id " + jobStatusResult.getJobId()));

                RegistryService.Client registryClient = getRegistryClientPool().getResource();
                ProcessModel processModel;
                ApplicationInterfaceDescription appDescription;
                try {
                    processModel = registryClient.getProcess(processId);
                    appDescription = registryClient.getApplicationInterface(processModel.getApplicationInterfaceId());

                } catch (Exception e) {
                    logger.error("Failed to fetch process or application description from registry associated with process id " + processId, e);
                    throw new Exception("Failed to fetch process or application description from registry associated with process id " + processId, e);

                } finally {
                    getRegistryClientPool().returnResource(registryClient);
                }

                // All the templates should be run
                // FIXME is it ApplicationInterfaceId or ApplicationName
                Set<ParsingTemplate> parsingTemplates = ParserCatalog.getParserTemplatesForApplication(appDescription.getApplicationInterfaceId());

                for (ParsingTemplate template : parsingTemplates) {
                    List<AbstractTask> allTasks = new ArrayList<>();
                    ParserInfo parentParser = null;

                    for (ParserDAGElement dagElement : template.getParserDag()) {
                        ParserInfo childParser = ParserCatalog.getParserById(dagElement.getChildParser());

                        DataParsingTask task = new DataParsingTask();
                        task.setTaskId("Data-Parsing-Task-" + childParser.getId().replaceAll("[^a-zA-Z0-9_.-]", "-"));
                        task.setJsonStrParserInfo(ParserUtil.getStrFromObj(childParser));
                        task.setJsonStrParserDagElement(ParserUtil.getStrFromObj(dagElement));
                        task.setParsingTemplateId(template.getId());
                        task.setGatewayId(ServerSettings.getSetting("gateway.id"));
                        task.setStorageResourceId(ServerSettings.getSetting("storage.resource.id"));
                        task.setDataMovementProtocol(ServerSettings.getSetting("data.movement.protocol"));
                        task.setStorageResourceCredToken(ServerSettings.getSetting("storage.cred.token"));
                        task.setStorageResourceLoginUName(ServerSettings.getSetting("resource.login.name"));
                        task.setStorageInputFilePath(parentParser != null
                                ? ParserUtil.getStorageUploadPath(template.getId(), parentParser.getId())
                                : template.getInitialInputsPath());

                        if (allTasks.size() > 0) {
                            allTasks.get(allTasks.size() - 1).setNextTask(new OutPort(task.getTaskId(), task));
                        }
                        allTasks.add(task);
                        logger.info("Successfully added the data parsing task: " + task.getTaskId() +
                                " to the task DAG of the parsing template: " + template.getId());

                        parentParser = childParser;
                    }

                    String workflowName = getWorkflowOperator().launchWorkflow(processId + "-DataParsing-" + UUID.randomUUID().toString(),
                            new ArrayList<>(allTasks), true, false);

                    try {
                        MonitoringUtil.registerWorkflow(getCuratorClient(), processId, workflowName);

                    } catch (Exception e) {
                        logger.error("Failed to save workflow " + workflowName + " of process " + processId +
                                " in zookeeper registry. " + "This will affect cancellation tasks", e);
                    }
                }
                return true;

            } else {
                logger.warn("Could not find a monitoring registered for the job id " + jobStatusResult.getJobId());
                return false;
            }

        } catch (Exception e) {
            logger.error("Failed to create the DataParsing task DAG", e);
            return false;
        }
    }

    private void runConsumer() throws ApplicationSettingsException {
        final Properties props = new Properties();
        final Consumer<String, JobStatusResult> consumer = new KafkaConsumer<>(props);

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ServerSettings.getSetting("kafka.broker.url"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, ServerSettings.getSetting("kafka.broker.consumer.group"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JobStatusResultDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumer.subscribe(Collections.singletonList(ServerSettings.getSetting("kafka.parser.topic")));

        while (true) {
            final ConsumerRecords<String, JobStatusResult> consumerRecords = consumer.poll(Long.MAX_VALUE);

            for (TopicPartition partition : consumerRecords.partitions()) {
                List<ConsumerRecord<String, JobStatusResult>> partitionRecords = consumerRecords.records(partition);
                for (ConsumerRecord<String, JobStatusResult> record : partitionRecords) {
                    boolean success = process(record.value());
                    logger.info("Status of processing job: " + record.value().getJobId() + " : " + success);
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
