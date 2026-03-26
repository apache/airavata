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

import org.apache.airavata.cli.handlers.AccountHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;

@Component
@Command(
        name = "account",
        description = "Manage root account",
        subcommands = {HelpCommand.class, AccountCommand.Init.class})
public class AccountCommand implements Runnable {

    @Override
    public void run() {
        // Default behavior: show help
        System.out.println("Use 'account init' to create a root user account.");
    }

    @Command(
            name = "init",
            description = "Initialize root account (only one root user allowed per gateway)",
            mixinStandardHelpOptions = true)
    public static class Init implements Runnable {

        @Autowired
        private AccountHandler accountHandler;

        @Option(
                names = {"--username"},
                required = true,
                description = "Username for root account")
        private String username;

        @Option(
                names = {"--password"},
                required = true,
                description = "Password for root account")
        private String password;

        @Option(
                names = {"--gateway"},
                required = true,
                description = "Gateway ID")
        private String gatewayId;

        @Override
        public void run() {
            accountHandler.createRootAccount(gatewayId, username, password);
        }
    }
}
