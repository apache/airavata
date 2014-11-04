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

package org.apache.airavata.gfac.core.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.SecurityContext;
import org.apache.airavata.gfac.core.cpi.GFac;
import org.apache.airavata.gfac.core.notification.GFacNotifier;
import org.apache.airavata.gfac.core.provider.GFacProvider;
import org.apache.airavata.model.appcatalog.computeresource.DataMovementInterface;
import org.apache.airavata.model.appcatalog.computeresource.DataMovementProtocol;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.JobDetails;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.model.workspace.experiment.WorkflowNodeDetails;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.zookeeper.ZooKeeper;

public class JobExecutionContext extends AbstractContext implements Serializable{

    private GFacConfiguration gfacConfiguration;

    private ApplicationContext applicationContext;

    private MessageContext inMessageContext;

    private MessageContext outMessageContext;

    private GFacNotifier notifier;

    //FIXME : not needed for gfac
    private Experiment experiment;

    private TaskDetails taskData;

    private JobDetails jobDetails;

    // FIXME : not needed for gfac
    private WorkflowNodeDetails workflowNodeDetails;

    private GFac gfac;

    private ZooKeeper zk;

    private String credentialStoreToken;
    /**
     * User defined working directory.
     */
    private String workingDir;
    /**
     * Input data directory
     */
    private String inputDir;
    /**
     * Output data directory
     */
    private String outputDir;
    /**
     * standard output file path
     */
    private String standardOutput;
    /**
     * standard error file path
     */
    private String standardError;
    /**
     * User preferred job submission protocol.
     */
    private JobSubmissionProtocol preferredJobSubmissionProtocol;
    /**
     * User preferred data movement protocol.
     */
    private DataMovementProtocol preferredDataMovementProtocol;
    /**
     * List of job submission protocols sorted by priority order.
     */
    private List<JobSubmissionInterface> hostPrioritizedJobSubmissionInterfaces;
    /**
     * use preferred job submission protocol.
     */
    private JobSubmissionInterface preferredJobSubmissionInterface;
    /**
     * List of job submission protocols sorted by priority order.
     */
    private List<DataMovementInterface> hostPrioritizedDataMovementInterfaces;
    /**
     * use preferred job submission protocol.
     */
    private DataMovementInterface preferredDataMovementInterface;

//    private ContextHeaderDocument.ContextHeader contextHeader;

    // Keep track of the current path of the message. Before hitting provider its in-path.
    // After provider its out-path.
    private boolean inPath = true;

    // Keep list of full qualified class names of GFac handlers which should invoked before
    // the provider. This is specific to current job being executed.
    private List<String> inHandlers = new ArrayList<String>();

    // Keep list of full qualified class names of GFac handlers which should invoked after
    // the provider. This is specific to current job being executed.
    private List<String> outHandlers = new ArrayList<String>();

    // During the execution of in-flow one of the handlers(Scheduling handler) will
    // set this and GFac API will get it from the JobExecutionContext and execute the provider.
    private GFacProvider provider;

    // Service description is used by GFac to mainly specify input/output parameters for a job
    // and to expose a job as a service to the outside world. This service concept abstract out
    // a scientific application(or algorithm) as a service. Service name is there to identify to
    // which service description we should refer during the execution of the current job represented
    // by this context instance.
    private String applicationName;

    private String experimentID;

    public String getGatewayID() {
        return gatewayID;
    }

    public void setGatewayID(String gatewayID) {
        this.gatewayID = gatewayID;
    }

    private String gatewayID;
    
    private String status;

    private List<String> outputFileList;

    private Registry registry;

    /**
     *  Security context is used to handle authentication for input handlers and providers.
     *  There can be multiple security requirement for a single job so this allows you to add multiple security types
     *
     */
    private Map<String, SecurityContext> securityContext = new HashMap<String, SecurityContext>();

    public JobExecutionContext(GFacConfiguration gFacConfiguration,String applicationName){
        this.gfacConfiguration = gFacConfiguration;
        notifier = new GFacNotifier();
        setApplicationName(applicationName);
        outputFileList = new ArrayList<String>();
    }


    public String getExperimentID() {
        return experimentID;
    }

    public void setExperimentID(String experimentID) {
        this.experimentID = experimentID;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public MessageContext getInMessageContext() {
        return inMessageContext;
    }

    public void setInMessageContext(MessageContext inMessageContext) {
        this.inMessageContext = inMessageContext;
    }

    public MessageContext getOutMessageContext() {
        return outMessageContext;
    }

    public void setOutMessageContext(MessageContext outMessageContext) {
        this.outMessageContext = outMessageContext;
    }

    public GFacConfiguration getGFacConfiguration() {
        return gfacConfiguration;
    }

    public GFacNotifier getNotificationService(){
        return notifier;
    }

    public GFacProvider getProvider() {
        return provider;
    }

    public void setProvider(GFacProvider provider) {
        this.provider = provider;
    }

    public List<String> getInHandlers() {
        return inHandlers;
    }

    public void setInHandlers(List<String> inHandlers) {
        this.inHandlers = inHandlers;
    }

    public List<String> getOutHandlers() {
        return outHandlers;
    }

    public void setOutHandlers(List<String> outHandlers) {
        this.outHandlers = outHandlers;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public GFacNotifier getNotifier() {
        return notifier;
    }

    public boolean isInPath() {
        return inPath;
    }

    public TaskDetails getTaskData() {
		return taskData;
	}

	public void setTaskData(TaskDetails taskData) {
		this.taskData = taskData;
	}

	public boolean isOutPath(){
        return !inPath;
    }

    public void setInPath() {
        this.inPath = true;
    }

    public void setOutPath(){
        this.inPath = false;
    }

	public SecurityContext getSecurityContext(String name) throws GFacException{
		SecurityContext secContext = securityContext.get(name);
		return secContext;
	}

	public void addSecurityContext(String name, SecurityContext value){
		securityContext.put(name, value);
    }

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

    public JobDetails getJobDetails() {
        return jobDetails;
    }

    public void setJobDetails(JobDetails jobDetails) {
        this.jobDetails = jobDetails;
    }

    public void addOutputFile(String file) {
        outputFileList.add(file);
    }
    public List<String> getOutputFiles(){
        return outputFileList;
    }

    public Registry getRegistry() {
        return registry;
    }

    public Map<String, SecurityContext>  getAllSecurityContexts(){
        return securityContext;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public WorkflowNodeDetails getWorkflowNodeDetails() {
        return workflowNodeDetails;
    }

    public void setWorkflowNodeDetails(WorkflowNodeDetails workflowNodeDetails) {
        this.workflowNodeDetails = workflowNodeDetails;
    }

    public GFac getGfac() {
        return gfac;
    }

    public void setGfac(GFac gfac) {
        this.gfac = gfac;
    }

    public ZooKeeper getZk() {
        return zk;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;

    }

    public String getCredentialStoreToken() {
        return credentialStoreToken;
    }

    public void setCredentialStoreToken(String credentialStoreToken) {
        this.credentialStoreToken = credentialStoreToken;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public String getInputDir() {
        return inputDir;
    }

    public void setInputDir(String inputDir) {
        this.inputDir = inputDir;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getStandardOutput() {
        return standardOutput;
    }

    public void setStandardOutput(String standardOutput) {
        this.standardOutput = standardOutput;
    }

    public String getStandardError() {
        return standardError;
    }

    public void setStandardError(String standardError) {
        this.standardError = standardError;
    }

    public JobSubmissionProtocol getPreferredJobSubmissionProtocol() {
        return preferredJobSubmissionProtocol;
    }

    public void setPreferredJobSubmissionProtocol(JobSubmissionProtocol preferredJobSubmissionProtocol) {
        this.preferredJobSubmissionProtocol = preferredJobSubmissionProtocol;
    }

    public DataMovementProtocol getPreferredDataMovementProtocol() {
        return preferredDataMovementProtocol;
    }

    public void setPreferredDataMovementProtocol(DataMovementProtocol preferredDataMovementProtocol) {
        this.preferredDataMovementProtocol = preferredDataMovementProtocol;
    }

    public List<JobSubmissionInterface> getHostPrioritizedJobSubmissionInterfaces() {
        return hostPrioritizedJobSubmissionInterfaces;
    }

    public void setHostPrioritizedJobSubmissionInterfaces(List<JobSubmissionInterface> hostPrioritizedJobSubmissionInterfaces) {
        this.hostPrioritizedJobSubmissionInterfaces = hostPrioritizedJobSubmissionInterfaces;
    }

    public JobSubmissionInterface getPreferredJobSubmissionInterface() {
        return preferredJobSubmissionInterface;
    }

    public void setPreferredJobSubmissionInterface(JobSubmissionInterface preferredJobSubmissionInterface) {
        this.preferredJobSubmissionInterface = preferredJobSubmissionInterface;
    }

    public String getHostName() {
        return applicationContext.getComputeResourceDescription().getHostName();
    }

    public List<DataMovementInterface> getHostPrioritizedDataMovementInterfaces() {
        return hostPrioritizedDataMovementInterfaces;
    }

    public void setHostPrioritizedDataMovementInterfaces(List<DataMovementInterface> hostPrioritizedDataMovementInterfaces) {
        this.hostPrioritizedDataMovementInterfaces = hostPrioritizedDataMovementInterfaces;
    }

    public DataMovementInterface getPreferredDataMovementInterface() {
        return preferredDataMovementInterface;
    }

    public void setPreferredDataMovementInterface(DataMovementInterface preferredDataMovementInterface) {
        this.preferredDataMovementInterface = preferredDataMovementInterface;
    }

    public String getExecutablePath() {
        if (applicationContext == null || applicationContext.getApplicationDeploymentDescription() == null) {
            return null;
        } else {
            return applicationContext.getApplicationDeploymentDescription().getExecutablePath();
        }
    }
}
