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
package org.apache.airavata.k8s.compute.impl;

import org.apache.airavata.k8s.compute.api.ComputeOperations;
import org.apache.airavata.k8s.compute.api.ExecutionResult;

import java.io.File;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class MockComputeOperation implements ComputeOperations {

    private String computeHost;

    public MockComputeOperation(String computeHost) {
        this.computeHost = computeHost;
    }

    @Override
    public ExecutionResult executeCommand(String command) throws Exception {
        System.out.println("Executing command " + command + " on host " + this.computeHost);
        ExecutionResult executionResult = new ExecutionResult();
        executionResult.setExitStatus(0);
        executionResult.setStdOut("Sample standard out");
        executionResult.setStdErr("Simple standard error");
        Thread.sleep(5000);
        System.out.println("Command successfully executed");
        return executionResult;
    }

    @Override
    public void transferDataIn(String source, String target, String protocol) throws Exception {
        System.out.println("Transferring data in from " + source + " to " + target);
        Thread.sleep(5000);
        System.out.println("Transferred data in from " + source + " to " + target);
    }

    @Override
    public void transferDataOut(String source, String target, String protocol) throws Exception {
        System.out.println("Transferring data out from " + source + " to " + target);
        File f = new File(target);
        f.getParentFile().mkdirs();
        f.createNewFile();
        System.out.println("Transferred data out from " + source + " to " + target);
    }
}
