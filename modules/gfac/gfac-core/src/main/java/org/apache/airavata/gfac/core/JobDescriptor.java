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
package org.apache.airavata.gfac.core;

import org.apache.airavata.gfac.core.cluster.CommandOutput;
import org.apache.airavata.gfac.core.x2012.x12.AfterAnyList;
import org.apache.airavata.gfac.core.x2012.x12.AfterOKList;
import org.apache.airavata.gfac.core.x2012.x12.InputList;
import org.apache.airavata.gfac.core.x2012.x12.JobDescriptorDocument;
import org.apache.xmlbeans.XmlException;

import java.util.List;

/**
 * This class define a job with required parameters, based on this configuration API is generating a Pbs script and
 * submit the job to the computing resource
 */
public class JobDescriptor {

    private JobDescriptorDocument jobDescriptionDocument;


    public JobDescriptor() {
        jobDescriptionDocument = JobDescriptorDocument.Factory.newInstance();
        jobDescriptionDocument.addNewJobDescriptor();
    }

    public JobDescriptor(JobDescriptorDocument jobDescriptorDocument) {
        this.jobDescriptionDocument = jobDescriptorDocument;
    }


    public JobDescriptor(CommandOutput commandOutput) {
        jobDescriptionDocument = JobDescriptorDocument.Factory.newInstance();
        jobDescriptionDocument.addNewJobDescriptor();
    }


    public String toXML() {
        return jobDescriptionDocument.xmlText();
    }

    public JobDescriptorDocument getJobDescriptorDocument() {
        return this.jobDescriptionDocument;
    }

    /**
     * With new app catalog thrift object integration, we don't use this
     * @param xml
     * @return
     * @throws XmlException
     */
    @Deprecated
    public static JobDescriptor fromXML(String xml)
            throws XmlException {
        JobDescriptorDocument parse = JobDescriptorDocument.Factory
                .parse(xml);
        JobDescriptor jobDescriptor = new JobDescriptor(parse);
        return jobDescriptor;
    }


    //todo write bunch of setter getters to set and get jobdescription parameters
    public void setWorkingDirectory(String workingDirectory) {
        this.getJobDescriptorDocument().getJobDescriptor().setWorkingDirectory(workingDirectory);
    }

    public String getWorkingDirectory() {
        return this.getJobDescriptorDocument().getJobDescriptor().getWorkingDirectory();
    }

    public void setShellName(String shellName) {
        this.getJobDescriptorDocument().getJobDescriptor().setShellName(shellName);
    }

    public void setJobName(String name) {
        this.getJobDescriptorDocument().getJobDescriptor().setJobName(name);
    }

    public void setExecutablePath(String name) {
        this.getJobDescriptorDocument().getJobDescriptor().setExecutablePath(name);
    }

    public void setAllEnvExport(boolean name) {
        this.getJobDescriptorDocument().getJobDescriptor().setAllEnvExport(name);
    }

    public void setMailOptions(String name) {
        this.getJobDescriptorDocument().getJobDescriptor().setMailOptions(name);
    }

    public void setStandardOutFile(String name) {
        this.getJobDescriptorDocument().getJobDescriptor().setStandardOutFile(name);
    }

    public void setStandardErrorFile(String name) {
        this.getJobDescriptorDocument().getJobDescriptor().setStandardErrorFile(name);
    }

    public void setNodes(int name) {
        this.getJobDescriptorDocument().getJobDescriptor().setNodes(name);
    }

    public void setProcessesPerNode(int name) {
        this.getJobDescriptorDocument().getJobDescriptor().setProcessesPerNode(name);
    }

    public String getOutputDirectory() {
        return this.getJobDescriptorDocument().getJobDescriptor().getOutputDirectory();
    }

    public String getInputDirectory() {
        return this.getJobDescriptorDocument().getJobDescriptor().getInputDirectory();
    }
    public void setOutputDirectory(String name) {
        this.getJobDescriptorDocument().getJobDescriptor().setOutputDirectory(name);
    }

    public void setInputDirectory(String name) {
        this.getJobDescriptorDocument().getJobDescriptor().setInputDirectory(name);
    }

    /**
     * Users can pass the minute count for maxwalltime
     * @param minutes
     */
    public void setMaxWallTime(String minutes) {
        this.getJobDescriptorDocument().getJobDescriptor().setMaxWallTime(
                GFacUtils.maxWallTimeCalculator(Integer.parseInt(minutes)));

    }


    public void setMaxWallTimeForLSF(String minutes) {
        this.getJobDescriptorDocument().getJobDescriptor().setMaxWallTime(
                GFacUtils.maxWallTimeCalculatorForLSF(Integer.parseInt(minutes)));

    }
    public void setAcountString(String name) {
        this.getJobDescriptorDocument().getJobDescriptor().setAcountString(name);
    }

    public void setInputValues(List<String> inputValue) {
        InputList inputList = this.getJobDescriptorDocument().getJobDescriptor().addNewInputs();
        inputList.setInputArray(inputValue.toArray(new String[inputValue.size()]));
    }

    public void setJobID(String jobID) {
        this.getJobDescriptorDocument().getJobDescriptor().setJobID(jobID);
    }

    public void setQueueName(String queueName) {
        this.getJobDescriptorDocument().getJobDescriptor().setQueueName(queueName);
    }

    public void setStatus(String queueName) {
        this.getJobDescriptorDocument().getJobDescriptor().setStatus(queueName);
    }

    public void setAfterAnyList(String[] afterAnyList) {
        AfterAnyList afterAny = this.getJobDescriptorDocument().getJobDescriptor().addNewAfterAny();
        afterAny.setAfterAnyArray(afterAnyList);
    }

    public void setAfterOKList(String[] afterOKList) {
        AfterOKList afterAnyList = this.getJobDescriptorDocument().getJobDescriptor().addNewAfterOKList();
        afterAnyList.setAfterOKListArray(afterOKList);
    }
    public void setCTime(String cTime) {
        this.getJobDescriptorDocument().getJobDescriptor().setCTime(cTime);
    }
    public void setQTime(String qTime) {
        this.getJobDescriptorDocument().getJobDescriptor().setQTime(qTime);
    }
    public void setMTime(String mTime) {
        this.getJobDescriptorDocument().getJobDescriptor().setMTime(mTime);
    }
    public void setSTime(String sTime) {
        this.getJobDescriptorDocument().getJobDescriptor().setSTime(sTime);
    }
    public void setCompTime(String compTime) {
        this.getJobDescriptorDocument().getJobDescriptor().setCompTime(compTime);
    }
    public void setOwner(String owner) {
        this.getJobDescriptorDocument().getJobDescriptor().setOwner(owner);
    }
    public void setExecuteNode(String executeNode) {
        this.getJobDescriptorDocument().getJobDescriptor().setExecuteNode(executeNode);
    }
    public void setEllapsedTime(String ellapsedTime) {
        this.getJobDescriptorDocument().getJobDescriptor().setEllapsedTime(ellapsedTime);
    }

    public void setUsedCPUTime(String usedCPUTime) {
        this.getJobDescriptorDocument().getJobDescriptor().setUsedCPUTime(usedCPUTime);
    }
    public void setCPUCount(int usedCPUTime) {
            this.getJobDescriptorDocument().getJobDescriptor().setCpuCount(usedCPUTime);
        }
    public void setUsedMemory(String usedMemory) {
        this.getJobDescriptorDocument().getJobDescriptor().setUsedMem(usedMemory);
    }
    public void setVariableList(String variableList) {
        this.getJobDescriptorDocument().getJobDescriptor().setVariableList(variableList);
    }
    public void setSubmitArgs(String submitArgs) {
        this.getJobDescriptorDocument().getJobDescriptor().setSubmitArgs(submitArgs);
    }

    public void setPreJobCommands(String[] commands){
        if(this.getJobDescriptorDocument().getJobDescriptor().getPreJobCommands() == null){
            this.getJobDescriptorDocument().getJobDescriptor().addNewPreJobCommands();
        }
        this.getJobDescriptorDocument().getJobDescriptor().getPreJobCommands().setCommandArray(commands);
    }

     public void setPostJobCommands(String[] commands){
        if(this.getJobDescriptorDocument().getJobDescriptor().getPostJobCommands() == null){
            this.getJobDescriptorDocument().getJobDescriptor().addNewPostJobCommands();
        }
        this.getJobDescriptorDocument().getJobDescriptor().getPostJobCommands().setCommandArray(commands);
    }

    public void setModuleLoadCommands(String[] commands) {
        if (this.getJobDescriptorDocument().getJobDescriptor().getModuleLoadCommands() == null) {
            this.getJobDescriptorDocument().getJobDescriptor().addNewModuleLoadCommands();
        }
        this.getJobDescriptorDocument().getJobDescriptor().getModuleLoadCommands().setCommandArray(commands);
    }

    public void addModuleLoadCommands(String command) {
        if (this.getJobDescriptorDocument().getJobDescriptor().getModuleLoadCommands() == null) {
            this.getJobDescriptorDocument().getJobDescriptor().addNewModuleLoadCommands();
        }
        this.getJobDescriptorDocument().getJobDescriptor().getModuleLoadCommands().addCommand(command);
    }

    public void addPreJobCommand(String command){
        if(this.getJobDescriptorDocument().getJobDescriptor().getPreJobCommands() == null){
            this.getJobDescriptorDocument().getJobDescriptor().addNewPreJobCommands();
        }
        this.getJobDescriptorDocument().getJobDescriptor().getPreJobCommands().addCommand(command);
    }

     public void addPostJobCommand(String command){
        if(this.getJobDescriptorDocument().getJobDescriptor().getPostJobCommands() == null){
            this.getJobDescriptorDocument().getJobDescriptor().addNewPostJobCommands();
        }
        this.getJobDescriptorDocument().getJobDescriptor().getPostJobCommands().addCommand(command);
    }

    public void setPartition(String partition){
        this.getJobDescriptorDocument().getJobDescriptor().setPartition(partition);
    }

     public void setUserName(String userName){
        this.getJobDescriptorDocument().getJobDescriptor().setUserName(userName);
    }
     public void setNodeList(String nodeList){
        this.getJobDescriptorDocument().getJobDescriptor().setNodeList(nodeList);
    }
    public void setJobSubmitter(String jobSubmitter){
           this.getJobDescriptorDocument().getJobDescriptor().setJobSubmitterCommand(jobSubmitter);
    }
    public String getNodeList(){
        return this.getJobDescriptorDocument().getJobDescriptor().getNodeList();
    }
    public String getExecutablePath() {
        return this.getJobDescriptorDocument().getJobDescriptor().getExecutablePath();
    }

    public boolean getAllEnvExport() {
        return this.getJobDescriptorDocument().getJobDescriptor().getAllEnvExport();
    }

    public String getMailOptions() {
        return this.getJobDescriptorDocument().getJobDescriptor().getMailOptions();
    }

    public String getStandardOutFile() {
        return this.getJobDescriptorDocument().getJobDescriptor().getStandardOutFile();
    }

    public String getStandardErrorFile() {
        return this.getJobDescriptorDocument().getJobDescriptor().getStandardErrorFile();
    }

    public int getNodes(int name) {
        return this.getJobDescriptorDocument().getJobDescriptor().getNodes();
    }

    public int getCPUCount(int name) {
        return this.getJobDescriptorDocument().getJobDescriptor().getCpuCount();
    }

    public int getProcessesPerNode() {
        return this.getJobDescriptorDocument().getJobDescriptor().getProcessesPerNode();
    }

    public String getMaxWallTime() {
        return this.getJobDescriptorDocument().getJobDescriptor().getMaxWallTime();
    }

    public String getAcountString() {
        return this.getJobDescriptorDocument().getJobDescriptor().getAcountString();
    }

    public String[] getInputValues() {
        return this.getJobDescriptorDocument().getJobDescriptor().getInputs().getInputArray();
    }

    public String getJobID() {
        return this.getJobDescriptorDocument().getJobDescriptor().getJobID();
    }

    public String getQueueName() {
        return this.getJobDescriptorDocument().getJobDescriptor().getQueueName();
    }

    public String getStatus() {
        return this.getJobDescriptorDocument().getJobDescriptor().getStatus();
    }

    public String[] getAfterAnyList() {
        return this.getJobDescriptorDocument().getJobDescriptor().getAfterAny().getAfterAnyArray();
    }

    public String[] getAfterOKList() {
        return this.getJobDescriptorDocument().getJobDescriptor().getAfterOKList().getAfterOKListArray();
    }
    public String getCTime() {
        return this.getJobDescriptorDocument().getJobDescriptor().getCTime();
    }
    public String getQTime() {
        return this.getJobDescriptorDocument().getJobDescriptor().getQTime();
    }
    public String getMTime() {
        return this.getJobDescriptorDocument().getJobDescriptor().getMTime();
    }
    public String getSTime() {
        return this.getJobDescriptorDocument().getJobDescriptor().getSTime();
    }
    public String getCompTime() {
        return this.getJobDescriptorDocument().getJobDescriptor().getCompTime();
    }
    public String getOwner() {
        return this.getJobDescriptorDocument().getJobDescriptor().getOwner();
    }
    public String getExecuteNode() {
         return this.getJobDescriptorDocument().getJobDescriptor().getExecuteNode();
    }
    public String getEllapsedTime() {
        return this.getJobDescriptorDocument().getJobDescriptor().getEllapsedTime();
    }

    public String getUsedCPUTime() {
        return this.getJobDescriptorDocument().getJobDescriptor().getUsedCPUTime();
    }

    public String getUsedMemory() {
        return this.getJobDescriptorDocument().getJobDescriptor().getUsedMem();
    }
    public void getShellName() {
        this.getJobDescriptorDocument().getJobDescriptor().getShellName();
    }

    public String getJobName() {
        return this.getJobDescriptorDocument().getJobDescriptor().getJobName();
    }

    public String getJobId() {
        return this.getJobDescriptorDocument().getJobDescriptor().getJobID();
    }


    public String getVariableList() {
        return this.getJobDescriptorDocument().getJobDescriptor().getJobID();
    }
    public String getSubmitArgs() {
        return this.getJobDescriptorDocument().getJobDescriptor().getJobID();
    }

    public String[] getPostJobCommands(){
        if(this.getJobDescriptorDocument().getJobDescriptor().getPostJobCommands() != null) {
            return this.getJobDescriptorDocument().getJobDescriptor().getPostJobCommands().getCommandArray();
        }
        return null;
    }

    public String[] getModuleCommands() {
        if (this.getJobDescriptorDocument().getJobDescriptor().getModuleLoadCommands() != null) {
            return this.getJobDescriptorDocument().getJobDescriptor().getModuleLoadCommands().getCommandArray();
        }
        return null;
    }

    public String[] getPreJobCommands(){
        if(this.getJobDescriptorDocument().getJobDescriptor().getPreJobCommands() != null) {
            return this.getJobDescriptorDocument().getJobDescriptor().getPreJobCommands().getCommandArray();
        }
        return null;
    }

    public String getJobSubmitterCommand(){
          return this.getJobDescriptorDocument().getJobDescriptor().getJobSubmitterCommand();
    }

    public String getPartition(){
        return this.getJobDescriptorDocument().getJobDescriptor().getPartition();
    }

    public String getUserName(){
        return this.getJobDescriptorDocument().getJobDescriptor().getUserName();
    }

    public void setCallBackIp(String ip){
        this.jobDescriptionDocument.getJobDescriptor().setCallBackIp(ip);
    }

    public void setCallBackPort(String ip){
        this.jobDescriptionDocument.getJobDescriptor().setCallBackPort(ip);
    }


    public String getCallBackIp(){
        return this.jobDescriptionDocument.getJobDescriptor().getCallBackIp();
    }
    public String getCallBackPort(){
        return this.jobDescriptionDocument.getJobDescriptor().getCallBackPort();
    }

    public void setMailType(String emailType) {
        this.getJobDescriptorDocument().getJobDescriptor().setMailType(emailType);
    }

    public String getMailType() {
        return this.getJobDescriptorDocument().getJobDescriptor().getMailType();
    }
    public void setMailAddress(String emailAddress) {
        this.getJobDescriptorDocument().getJobDescriptor().setMailAddress(emailAddress);
    }

    public String getMailAddress() {
        return this.getJobDescriptorDocument().getJobDescriptor().getMailAddress();
    }

    public String getChassisName() {
        return this.getJobDescriptorDocument().getJobDescriptor().getChassisName();
    }

    public void setChassisName(String chassisName){
        this.getJobDescriptorDocument().getJobDescriptor().setChassisName(chassisName);
    }
    

}

