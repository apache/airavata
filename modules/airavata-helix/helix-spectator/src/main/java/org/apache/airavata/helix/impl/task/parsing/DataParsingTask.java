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
package org.apache.airavata.helix.impl.task.parsing;

import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.impl.task.TaskOnFailException;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.helix.task.api.annotation.TaskParam;
import org.apache.helix.task.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Pick the input file named {@link CatalogEntry#inputFileName}
 * from the {@link #localWorkingDir} directory and handover to
 * the {@link CatalogEntry#containerName} Docker container to
 * get the desired {@link CatalogEntry#outputFileName} output file
 *
 * @since 1.0.0-SNAPSHOT
 */
@TaskDef(name = "Data Parsing Task")
public class DataParsingTask extends AbstractTask {

    private final static Logger logger = LoggerFactory.getLogger(DataParsingTask.class);

    @TaskParam(name = "JSON String Catalog Entry")
    private String jsonStrCatalogEntry;

    @TaskParam(name = "Local Working Directory")
    private String localWorkingDir;

    @Override
    public TaskResult onRun(TaskHelper helper) {
        logger.info("Starting data parsing task " + getTaskId());

        CatalogEntry catalogEntry = CatalogUtil.jsonStringToCatalogEntry(jsonStrCatalogEntry);
        try {
            File inputFile = new File(localWorkingDir.endsWith(File.separator)
                    ? localWorkingDir + catalogEntry.getInputFileName()
                    : localWorkingDir + File.separator + catalogEntry.getInputFileName());

            if (!inputFile.exists()) {
                throw new TaskOnFailException("Input file does not exists for task " + getTaskId(), true, null);
            }

            ContainerStatus containerStatus = ContainerStatus.REMOVED;

            // Check whether the container is running if found stop the container
            Process procActive = Runtime.getRuntime().exec("docker ps -q -f name=" + catalogEntry.getContainerName());
            try (InputStreamReader isr = new InputStreamReader(procActive.getInputStream())) {
                if (isr.read() != -1) {
                    containerStatus = ContainerStatus.ACTIVE;
                    logger.info("Docker container: " + catalogEntry.getContainerName() +
                            " is active in data parsing task: " + getTaskId());

                    // Stop the container
                    Process procStop = Runtime.getRuntime().exec("docker stop " + catalogEntry.getContainerName());
                    try (InputStreamReader isrStop = new InputStreamReader(procStop.getInputStream())) {
                        if (isrStop.read() != -1) {
                            containerStatus = ContainerStatus.INACTIVE;
                            logger.info("Stopped the Docker container: " + catalogEntry.getContainerName() +
                                    " for data parsing task: " + getTaskId());
                        }
                    }
                }
            }

            // Check for an exited container if found remove it
            Process procExited = Runtime.getRuntime().exec("docker ps -aq -f status=exited -f name=" + catalogEntry.getContainerName());
            try (InputStreamReader isr = new InputStreamReader(procExited.getInputStream())) {
                if (isr.read() != -1) {

                    Process procRemoved = Runtime.getRuntime().exec("docker rm " + catalogEntry.getContainerName());
                    try (InputStreamReader isrRemoved = new InputStreamReader(procRemoved.getInputStream())) {
                        if (isrRemoved.read() != -1) {
                            containerStatus = ContainerStatus.REMOVED;
                            logger.info("Removed the exited Docker container: " + catalogEntry.getContainerName() +
                                    " for data parsing task: " + getTaskId());
                        } else {
                            containerStatus = ContainerStatus.INACTIVE;
                        }
                    }
                }
            }

            if (containerStatus == ContainerStatus.REMOVED) {
                /*
                 * Example command
                 *
                 *      "docker run --name CONTAINER-lahiruj/gaussian " +
                 *      "-it --rm=true " +
                 *      "--security-opt seccomp=/path/to/seccomp/profile.json " +
                 *      "--label com.example.foo=bar " +
                 *      "--env LD_LIBRARY_PATH=/usr/local/lib " +
                 *      "-v /Users/lahiruj/tmp/dir:/datacat/working-dir " +     // local directory is mounted in read-write mode
                 *      "lahiruj/gaussian " +                                   // docker image name
                 *      "python " +                                             // programming language
                 *      "gaussian.py " +                                        // file to be executed
                 *      "input-gaussian.json"                                   // input file
                 *      "output.json"                                           // output file path
                 *
                 */
                String dockerCommand = "docker run " +
                        "--name " + catalogEntry.getContainerName() +
                        " -t " +
                        catalogEntry.getAutomaticallyRmContainer() + " " +
                        catalogEntry.getRunInDetachedMode() + " " +
                        catalogEntry.getSecurityOpt() + " " +
                        catalogEntry.getLabel() + " " +
                        catalogEntry.getEnvVariables() + " " +
                        catalogEntry.getCpus() + " " +
                        " -v " + localWorkingDir + ":" + catalogEntry.getDockerWorkingDirPath() + " " +
                        catalogEntry.getDockerImageName() + " " +
                        catalogEntry.getExecutableBinary() + " " +
                        catalogEntry.getExecutingFile() + " " +
                        catalogEntry.getInputFileName() + " " +
                        catalogEntry.getOutputFileName();

                Process proc = Runtime.getRuntime().exec(dockerCommand);
                try (BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
                    String line;
                    StringBuilder error = new StringBuilder();

                    // read errors from the attempted command
                    while ((line = stdError.readLine()) != null) {
                        error.append(line);
                    }

                    if (error.length() > 0) {
                        logger.error("Error running Docker command " + error + " for task " + getTaskId());
                        throw new TaskOnFailException("Could not run Docker command successfully for task " + getTaskId(), true, null);
                    }
                }

                return onSuccess("Data parsing task " + getTaskId() + " successfully completed");

            } else {
                throw new TaskOnFailException("Docker container has not been successfully " +
                        (containerStatus == ContainerStatus.ACTIVE ? "stopped " : "removed ") +
                        "for data parsing task" + getTaskId(), true, null);
            }

        } catch (TaskOnFailException e) {
            if (e.getError() != null) {
                logger.error(e.getReason(), e.getError());
            } else {
                logger.error(e.getReason());
            }

            return onFail(e.getReason(), e.isCritical());

        } catch (Exception e) {
            logger.error("Unknown error while executing data parsing task " + getTaskId(), e);
            return onFail("Unknown error while executing data parsing task " + getTaskId(), false);
        }
    }

    @Override
    public void onCancel() {

    }

    public String getJsonStrCatalogEntry() {
        return jsonStrCatalogEntry;
    }

    public void setJsonStrCatalogEntry(String jsonStrCatalogEntry) {
        this.jsonStrCatalogEntry = jsonStrCatalogEntry;
    }

    public String getLocalWorkingDir() {
        return localWorkingDir;
    }

    public void setLocalWorkingDir(String localWorkingDir) {
        this.localWorkingDir = localWorkingDir;
    }

    private enum ContainerStatus {
        ACTIVE,
        INACTIVE,
        REMOVED
    }
}
