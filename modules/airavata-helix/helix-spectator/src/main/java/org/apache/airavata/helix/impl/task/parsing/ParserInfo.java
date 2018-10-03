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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains parser information which are required to parse given input files into
 * given output files and to initiate a Docker container
 *
 * @since 1.0.0-SNAPSHOT
 */
public class ParserInfo {

    private String id;
    private String containerName;
    private String dockerImageName;
    // Where the output files will be saved inside container
    private String dockerWorkingDirPath;
    // python, java, etc.
    private String executableBinary;
    // The file which should be executed to parse the content
    private String executingFile;
    private List<String> inputFiles;
    private List<String> mandatoryOutputFiles;
    private List<String> optionalOutputFiles;

    /*
     * Following variables are declared according to the parameters
     * defined at https://docs.docker.com/engine/reference/commandline/run web page
     */
    // Run the container in detached mode
    private String runInDetachedMode;
    // Automatically remove the container once the output is generated
    private String automaticallyRmContainer;
    // override the default profile with the specified one, eg. "--security-opt seccomp=/path/to/seccomp/profile.json"
    private String securityOpt;
    // eg. --env VAR1=value1 --env VAR2=value2
    private String envVariables;
    // Number of CPUs going to be allocated
    private String cpus;
    // Set metadata on container, eg."--label com.example.foo=bar"
    private String label;

    private ParserInfo(Builder builder) {
        id = builder.id;
        containerName = "CONTAINER-" + builder.dockerImageName.replaceAll("[^a-zA-Z0-9_.-]", "-");
        dockerImageName = builder.dockerImageName;
        dockerWorkingDirPath = builder.dockerWorkingDirPath;
        executableBinary = builder.executableBinary;
        executingFile = builder.executingFile;
        inputFiles = builder.inputFiles;
        mandatoryOutputFiles = builder.mandatoryOutputFiles;
        optionalOutputFiles = builder.optionalOutputFiles;

        runInDetachedMode = builder.runInDetachedMode;
        automaticallyRmContainer = builder.automaticallyRmContainer;
        securityOpt = builder.securityOpt;
        envVariables = builder.envVariables;
        cpus = builder.cpus;
        label = builder.label;
    }

    public String getId() {
        return id;
    }

    public String getContainerName() {
        return containerName;
    }

    public String getDockerImageName() {
        return dockerImageName;
    }

    public String getDockerWorkingDirPath() {
        return dockerWorkingDirPath;
    }

    public String getExecutableBinary() {
        return executableBinary;
    }

    public String getExecutingFile() {
        return executingFile;
    }

    public List<String> getInputFiles() {
        return inputFiles;
    }

    public List<String> getMandatoryOutputFiles() {
        return mandatoryOutputFiles;
    }

    public List<String> getOptionalOutputFiles() {
        return optionalOutputFiles;
    }

    public String getRunInDetachedMode() {
        return runInDetachedMode;
    }

    public String getAutomaticallyRmContainer() {
        return automaticallyRmContainer;
    }

    public String getSecurityOpt() {
        return securityOpt;
    }

    public String getEnvVariables() {
        return envVariables;
    }

    public String getCpus() {
        return cpus;
    }

    public String getLabel() {
        return label;
    }

    public static class Builder {
        // Required parameters
        private String id;
        private String dockerImageName;
        private String dockerWorkingDirPath;
        private String executableBinary;
        private String executingFile;
        private List<String> inputFiles;
        private List<String> mandatoryOutputFiles;

        // Optional parameters. Initialized to default values
        private List<String> optionalOutputFiles = null;
        private String runInDetachedMode = "";
        private String automaticallyRmContainer = "--rm=true";
        private String securityOpt = "";
        private String envVariables = "";
        private String cpus = "";
        private String label = "";

        public Builder(String id, String dockerImageName, String dockerWorkingDirPath, String executableBinary,
                       String executingFile, List<String> inputFiles, List<String> mandatoryOutputFiles) {
            this.id = id;
            this.dockerImageName = dockerImageName;
            this.dockerWorkingDirPath = dockerWorkingDirPath;
            this.executableBinary = executableBinary;
            this.executingFile = executingFile;
            this.inputFiles = inputFiles;
            this.mandatoryOutputFiles = mandatoryOutputFiles;
        }

        public Builder optionalOutputFiles(List<String> files) {
            if (files != null && files.size() > 0) {
                optionalOutputFiles = files;
            }
            return this;
        }

        public Builder runInDetachedMode(String val) {
            if (Boolean.valueOf(val)) {
                runInDetachedMode = "-d";
            }
            return this;
        }

        public Builder automaticallyRmContainer(String val) {
            if (!val.isEmpty() && !Boolean.valueOf(val)) {
                automaticallyRmContainer = "";
            }
            return this;
        }

        public Builder securityOpt(String val) {
            if (!val.isEmpty()) {
                securityOpt = "--security-opt " + val;
            }
            return this;
        }

        public Builder envVariables(List<String> val) {
            if (val != null && val.size() > 0) {
                envVariables = val.stream().map(s -> " --env " + s).collect(Collectors.joining()).trim();
            }
            return this;
        }

        public Builder cpus(String val) {
            if (!val.isEmpty()) {
                cpus = "--cpus=" + val;
            }
            return this;
        }

        public Builder label(String val) {
            if (!val.isEmpty()) {
                label = "--label " + val;
            }
            return this;
        }

        public ParserInfo build() {
            return new ParserInfo(this);
        }
    }
}
