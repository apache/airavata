/**
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
package org.apache.airavata.gfac.impl;

import com.jcraft.jsch.Channel;
import org.apache.airavata.gfac.core.cluster.CommandOutput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by syodage on 11/9/15.
 */
public class LocalCommandOutput implements CommandOutput {
    private Process process;

    @Override
    public void onOutput(Channel channel) {

    }

    public void readOutputs(Process process) {
        this.process = process;
    }

    public String getStandardOut() throws IOException {
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            sb.append(s);
        }
        return sb.toString();
    }

    public String getStandardErrorString() throws IOException {
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder sb = new StringBuilder();
        String s = null;
        while ((s = stdError.readLine()) != null) {
            sb.append(s);
        }
        return sb.toString();
    }

    @Override
    public OutputStream getStandardError() {
        return null;
    }

    @Override
    public void exitCode(int code) {

    }

    @Override
    public int getExitCode() {
        while (process.isAlive()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return process.exitValue();
    }
}
