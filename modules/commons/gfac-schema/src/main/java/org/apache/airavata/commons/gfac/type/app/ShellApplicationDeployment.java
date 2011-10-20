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
 *
 */

package org.apache.airavata.commons.gfac.type.app;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.schemas.gfac.ShellApplicationDeploymentType;

import java.util.Map;

public class ShellApplicationDeployment extends ApplicationDeploymentDescription {

    private ShellApplicationDeploymentType shellApplicationDeploymentType;
    // TODO
    private Map<String, String> env;

    public ShellApplicationDeployment() {
        shellApplicationDeploymentType = ShellApplicationDeploymentType.Factory.newInstance();
    }

    public ShellApplicationDeployment(ShellApplicationDeploymentType sadt) {
        shellApplicationDeploymentType = sadt;
    }

    public String getExecutable() {
        return shellApplicationDeploymentType.getExecutable();
    }

    public void setExecutable(String executable) {
        this.shellApplicationDeploymentType.setExecutable(executable);
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public void setEnv(Map<String, String> env) {
        this.env = env;
    }

    public String getStdOut() {
        return shellApplicationDeploymentType.getStdOut();
    }

    public void setStdOut(String stdOut) {
        this.shellApplicationDeploymentType.setStdOut(stdOut);
    }

    public String getStdErr() {
        return shellApplicationDeploymentType.getStdErr();
    }

    public void setStdErr(String stderr) {
        this.shellApplicationDeploymentType.setStdErr(stderr);
    }

    public String getStdIn() {
        return shellApplicationDeploymentType.getStdIn();
    }

    public void setStdIn(String stdIn) {
        this.shellApplicationDeploymentType.setStdIn(stdIn);
    }
}
