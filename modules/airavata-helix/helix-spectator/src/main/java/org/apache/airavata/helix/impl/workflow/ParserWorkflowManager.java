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
import org.apache.airavata.model.process.ProcessWorkflow;
import org.apache.airavata.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Workflow Manager which will create and launch a Data Parsing DAG
 *
 * @since 1.0.0-SNAPSHOT
 */
public class ParserWorkflowManager extends WorkflowManager {

    private final static Logger logger = LoggerFactory.getLogger(ParserWorkflowManager.class);

    private ParserRequest parserRequest;

    public ParserWorkflowManager(ParserRequest parserRequest) throws ApplicationSettingsException {
        super(ServerSettings.getSetting("parser.workflow.manager.name"),
                Boolean.parseBoolean(ServerSettings.getSetting("post.workflow.manager.loadbalance.clusters")));
        this.parserRequest = parserRequest;
    }

    private void init() throws Exception {
        super.initComponents();
    }

    private boolean process() {
        if (parserRequest.getInputFileName().isEmpty() || parserRequest.getOutputFileName().isEmpty() ||
                parserRequest.getWorkingDir().isEmpty()) {

            logger.error("Parser Request has missing field(s). Input file name, output file name, and " +
                    "working directory parameters must be non-empty");
            return false;
        }

        File inputFile = new File(parserRequest.getWorkingDir().endsWith(File.separator)
                ? parserRequest.getWorkingDir() + parserRequest.getInputFileName()
                : parserRequest.getWorkingDir() + File.separator + parserRequest.getInputFileName());

        if (!inputFile.exists()) {
            logger.error("Input file: " + inputFile.getName() + " does not exists");
            return false;
        }

        try {
            final List<AbstractTask> allTasks = new ArrayList<>();
            CatalogGraph catalogGraph = new CatalogGraph(parserRequest, ServerSettings.getSetting("parser.catalog.path"));
            List<CatalogEntry> catalogEntries = catalogGraph.getSPCatalogEntries(parserRequest.outputFileType());
            String processId = "ID-" + UUID.randomUUID().toString();

            // Set the input file name into the firs CatalogEntry
            catalogEntries.get(0).setInputFileName(parserRequest.getInputFileName());
            // Set the output file name into the last CatalogEntry
            catalogEntries.get(catalogEntries.size() - 1).setOutputFileName(parserRequest.getOutputFileName());

            for (int i = 0; i < catalogEntries.size(); i++) {
                CatalogEntry entry = catalogEntries.get(i);
                if (i > 0) {
                    // Set the input file name as the previous catalog entry output file name
                    entry.setInputFileName(catalogEntries.get(i - 1).getOutputFileName());
                }

                DataParsingTask task = new DataParsingTask();
                task.setJsonStrCatalogEntry(CatalogUtil.catalogEntryToJSONString(entry));
                task.setLocalWorkingDir(parserRequest.getWorkingDir());
                task.setTaskId("ID-" + entry.getDockerImageName().replaceAll("[^a-zA-Z0-9_.-]", "-"));

                if (allTasks.size() > 0) {
                    allTasks.get(allTasks.size() - 1).setNextTask(new OutPort(task.getTaskId(), task));
                }
                allTasks.add(task);
                logger.info("Successfully added the data parsing task: " + task.getTaskId() + " to the task DAG");
            }

            String workflowName = getWorkflowOperator()
                    .launchWorkflow(processId + "-DataParsing-" + UUID.randomUUID().toString(),
                            new ArrayList<>(allTasks), true, false);

            registerWorkflowForProcess(processId, workflowName, "PARSER");

            return true;

        } catch (Exception e) {
            logger.error("Failed to create the DataParsing task DAG", e);
            return false;
        }
    }
}
