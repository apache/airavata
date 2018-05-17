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
package org.apache.airavata.helix.agent.ssh;

import com.jcraft.jsch.Channel;
import org.apache.airavata.agents.api.CommandOutput;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class StandardOutReader implements CommandOutput {

    private String stdOut;
    private String stdError;
    private Integer exitCode;

    @Override
    public String getStdOut() {
        return this.stdOut;
    }

    @Override
    public String getStdError() {
        return this.stdError;
    }

    @Override
    public Integer getExitCode() {
        return this.exitCode;
    }

    public void readStdOutFromStream(InputStream is) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, "UTF-8");
        this.stdOut = writer.toString();
    }

    public void readStdErrFromStream(InputStream is) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, "UTF-8");
        this.stdError = writer.toString();
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }
}
