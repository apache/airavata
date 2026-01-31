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

import org.apache.airavata.cli.handlers.InitHandler;
import org.apache.airavata.cli.util.ApplicationContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(
        name = "init",
        description = "Initialize all Airavata databases using Flyway migrations",
        mixinStandardHelpOptions = true)
public class InitCommand implements Runnable {

    @Autowired(required = false)
    private InitHandler initHandler;

    @Option(
            names = {"--clean"},
            description = "Clean (drop) all databases before migration")
    private boolean clean = false;

    @Override
    public void run() {
        InitHandler handler = initHandler;
        if (handler == null) {
            var ctx = ApplicationContextHolder.get();
            if (ctx == null) {
                throw new IllegalStateException("InitHandler not available and ApplicationContext not set");
            }
            handler = ctx.getBean(InitHandler.class);
        }
        handler.initializeDatabases(clean);
        // Exit immediately so Spring does not proceed to shutdown or any other command.
        System.exit(0);
    }
}
