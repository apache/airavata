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

package org.apache.airavata.core.gfac.model;

import java.util.ArrayList;
import java.util.Map;

import org.ogce.schemas.gfac.documents.ApplicationDescriptionType;
import org.ogce.schemas.gfac.documents.GlobusGatekeeperType;
import org.ogce.schemas.gfac.documents.HostDescriptionType;

public class ExecutionModel {

    private String jobID;

    private String userDN;

    private String workingDir;

    private String tmpDir;

    private String stdOut;

    private String stderr;

    private String host;

    private String executable;

    private Map<String, String> env;

    private String inputDataDir;

    private String outputDataDir;

    private String stdIn;

    private String stdoutStr;

    private String stderrStr;

    private ArrayList<String> inputParameters;

    private ArrayList<String> outputParameters;

    private HostDescriptionType hostDesc;

    private ApplicationDescriptionType aplicationDesc;

    private GlobusGatekeeperType gatekeeper;

    /**
     * @return the userDN
     */
    public String getUserDN() {
        return userDN;
    }

    /**
     * @param userDN
     *            the userDN to set
     */
    public void setUserDN(String userDN) {
        this.userDN = userDN;
    }

    /**
     * @return the workingDir
     */
    public String getWorkingDir() {
        return workingDir;
    }

    /**
     * @param workingDir
     *            the workingDir to set
     */
    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * @return the tmpDir
     */
    public String getTmpDir() {
        return tmpDir;
    }

    /**
     * @param tmpDir
     *            the tmpDir to set
     */
    public void setTmpDir(String tmpDir) {
        this.tmpDir = tmpDir;
    }

    /**
     * @return the stdOut
     */
    public String getStdOut() {
        return stdOut;
    }

    /**
     * @param stdOut
     *            the stdOut to set
     */
    public void setStdOut(String stdOut) {
        this.stdOut = stdOut;
    }

    /**
     * @return the stderr
     */
    public String getStderr() {
        return stderr;
    }

    /**
     * @param stderr
     *            the stderr to set
     */
    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host
     *            the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the executable
     */
    public String getExecutable() {
        return executable;
    }

    /**
     * @param executable
     *            the executable to set
     */
    public void setExecutable(String executable) {
        this.executable = executable;
    }

    /**
     * @return the env
     */
    public Map<String, String> getEnv() {
        return env;
    }

    /**
     * @param env
     *            the env to set
     */
    public void setEnv(Map<String, String> env) {
        this.env = env;
    }

    /**
     * @return the inputDataDir
     */
    public String getInputDataDir() {
        return inputDataDir;
    }

    /**
     * @param inputDataDir
     *            the inputDataDir to set
     */
    public void setInputDataDir(String inputDataDir) {
        this.inputDataDir = inputDataDir;
    }

    /**
     * @return the outputDataDir
     */
    public String getOutputDataDir() {
        return outputDataDir;
    }

    /**
     * @param outputDataDir
     *            the outputDataDir to set
     */
    public void setOutputDataDir(String outputDataDir) {
        this.outputDataDir = outputDataDir;
    }

    /**
     * @return the stdIn
     */
    public String getStdIn() {
        return stdIn;
    }

    /**
     * @param stdIn
     *            the stdIn to set
     */
    public void setStdIn(String stdIn) {
        this.stdIn = stdIn;
    }

    /**
     * @return the stdoutStr
     */
    public String getStdoutStr() {
        return stdoutStr;
    }

    /**
     * @param stdoutStr
     *            the stdoutStr to set
     */
    public void setStdoutStr(String stdoutStr) {
        this.stdoutStr = stdoutStr;
    }

    /**
     * @return the stderrStr
     */
    public String getStderrStr() {
        return stderrStr;
    }

    /**
     * @param stderrStr
     *            the stderrStr to set
     */
    public void setStderrStr(String stderrStr) {
        this.stderrStr = stderrStr;
    }

    public void setInputParameters(ArrayList<String> inputParameters) {
        this.inputParameters = inputParameters;
    }

    public ArrayList<String> getInputParameters() {
        return inputParameters;
    }

    public void setOutputParameters(ArrayList<String> outputParameters) {
        this.outputParameters = outputParameters;
    }

    public ArrayList<String> getOutputParameters() {
        return outputParameters;
    }

    public void setHostDesc(HostDescriptionType hostDesc) {
        this.hostDesc = hostDesc;
    }

    public HostDescriptionType getHostDesc() {
        return hostDesc;
    }

    public void setGatekeeper(GlobusGatekeeperType gatekeeper) {
        this.gatekeeper = gatekeeper;
    }

    public GlobusGatekeeperType getGatekeeper() {
        return gatekeeper;
    }

    public void setAplicationDesc(ApplicationDescriptionType aplicationDesc) {
        this.aplicationDesc = aplicationDesc;
    }

    public ApplicationDescriptionType getAplicationDesc() {
        return aplicationDesc;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getJobID() {
        return jobID;
    }

}
