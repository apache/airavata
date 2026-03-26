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
package org.apache.airavata.cli.commands;

import org.apache.airavata.cli.handlers.TestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;

@Component
@Command(
        name = "test",
        description = "Test application submission readiness",
        subcommands = {HelpCommand.class, TestCommand.Run.class})
public class TestCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use 'test run' to validate experiment submission readiness.");
    }

    @Command(name = "run", description = "Test application submission readiness", mixinStandardHelpOptions = true)
    public static class Run implements Runnable {
        @Autowired
        private TestHandler testHandler;

        @Option(
                names = {"--application"},
                required = true,
                description = "Application interface ID")
        private String applicationId;

        @Option(
                names = {"--compute"},
                required = true,
                description = "Compute resource ID")
        private String computeId;

        @Option(
                names = {"--user"},
                required = true,
                description = "Username")
        private String userId;

        @Option(
                names = {"--gateway"},
                required = true,
                description = "Gateway ID")
        private String gatewayId;

        @Option(
                names = {"--storage"},
                description = "Storage resource ID")
        private String storageId;

        @Option(
                names = {"--group"},
                description = "Group resource profile ID")
        private String groupId;

        @Override
        public void run() {
            testHandler.testApplicationSubmission(gatewayId, userId, applicationId, computeId, storageId, groupId);
        }
    }
}
