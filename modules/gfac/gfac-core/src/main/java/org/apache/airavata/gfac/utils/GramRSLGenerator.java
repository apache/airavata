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
package org.apache.airavata.gfac.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.ToolsException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.model.workspace.experiment.ComputationalResourceScheduling;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.schemas.gfac.FileArrayType;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.apache.airavata.schemas.gfac.NameValuePairType;
import org.apache.airavata.schemas.gfac.QueueType;
import org.apache.airavata.schemas.gfac.StringArrayType;
import org.apache.airavata.schemas.gfac.URIArrayType;
import org.globus.gram.GramAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GramRSLGenerator {
    protected static final Logger log = LoggerFactory.getLogger(GramRSLGenerator.class);

    private enum JobType {
        SERIAL, SINGLE, MPI, MULTIPLE, CONDOR
    }

    ;

    public static GramAttributes  configureRemoteJob(JobExecutionContext context) throws ToolsException {
        HpcApplicationDeploymentType app = (HpcApplicationDeploymentType) context.getApplicationContext().getApplicationDeploymentDescription().getType();
        GramAttributes jobAttr = new GramAttributes();
        jobAttr.setExecutable(app.getExecutableLocation());
        jobAttr.setDirectory(app.getStaticWorkingDirectory());
        jobAttr.setStdout(app.getStandardOutput());
        jobAttr.setStderr(app.getStandardError());
        /*
         * The env here contains the env of the host and the application. i.e the env specified in the host description
         * and application description documents
         */
        NameValuePairType[] env = app.getApplicationEnvironmentArray();
        if (env.length != 0) {
            Map<String, String> nv = new HashMap<String, String>();
            for (int i = 0; i < env.length; i++) {
                String key = env[i].getName();
                String value = env[i].getValue();
                nv.put(key, value);
            }

            for (Map.Entry<String, String> entry : nv.entrySet()) {
                jobAttr.addEnvVariable(entry.getKey(), entry.getValue());
            }
        }
        jobAttr.addEnvVariable(Constants.INPUT_DATA_DIR_VAR_NAME, app.getInputDataDirectory());
        jobAttr.addEnvVariable(Constants.OUTPUT_DATA_DIR_VAR_NAME, app.getOutputDataDirectory());

    

        if (app.getStandardInput() != null && !"".equals(app.getStandardInput())) {
            jobAttr.setStdin(app.getStandardInput());
        } else {
            MessageContext input = context.getInMessageContext();;
            Map<String,Object> inputs = input.getParameters();
            Set<String> keys = inputs.keySet();
            for (String paramName : keys ) {
             	ActualParameter actualParameter = (ActualParameter) inputs.get(paramName);
                if ("URIArray".equals(actualParameter.getType().getType().toString()) || "StringArray".equals(actualParameter.getType().getType().toString())
                        || "FileArray".equals(actualParameter.getType().getType().toString())) {
                    String[] values = null;
                    if (actualParameter.getType() instanceof URIArrayType) {
                        values = ((URIArrayType) actualParameter.getType()).getValueArray();
                    } else if (actualParameter.getType() instanceof StringArrayType) {
                        values = ((StringArrayType) actualParameter.getType()).getValueArray();
                    } else if (actualParameter.getType() instanceof FileArrayType) {
                        values = ((FileArrayType) actualParameter.getType()).getValueArray();
                    }
                    String value = StringUtil.createDelimiteredString(values, " ");
                    jobAttr.addArgument(value);
                } else {
                    String paramValue = MappingFactory.toString(actualParameter);
                    jobAttr.addArgument(paramValue);
                }
            }
        }
        // Using the workflowContext Header values if user provided them in the request and overwrite the default values in DD
        //todo finish the scheduling based on workflow execution context
        TaskDetails taskData = context.getTaskData();
        if(taskData != null && taskData.isSetTaskScheduling()){
        	 ComputationalResourceScheduling computionnalResource = taskData.getTaskScheduling();
                try {
                    int cpuCount = computionnalResource.getTotalCPUCount();
                    if(cpuCount>0){
                        app.setCpuCount(cpuCount);
                    }
                } catch (NullPointerException e) {
                    log.debug("No Value sent in WorkflowContextHeader for CPU Count, value in the Deployment Descriptor will be used");
                    new GFacProviderException("No Value sent in WorkflowContextHeader for Node Count, value in the Deployment Descriptor will be used",e);
                }
                try {
                    int nodeCount = computionnalResource.getNodeCount();
                    if(nodeCount>0){
                        app.setNodeCount(nodeCount);
                    }
                } catch (NullPointerException e) {
                    log.debug("No Value sent in WorkflowContextHeader for Node Count, value in the Deployment Descriptor will be used");
                     new GFacProviderException("No Value sent in WorkflowContextHeader for Node Count, value in the Deployment Descriptor will be used",e);
                }
                try {
                    String queueName = computionnalResource.getQueueName();
                    if (queueName != null) {
                        if(app.getQueue() == null){
                            QueueType queueType = app.addNewQueue();
                            queueType.setQueueName(queueName);
                        }else{
                            app.getQueue().setQueueName(queueName);
                        }
                    }
                } catch (NullPointerException e) {
                    log.debug("No Value sent in WorkflowContextHeader for Node Count, value in the Deployment Descriptor will be used");
                     new GFacProviderException("No Value sent in WorkflowContextHeader for Node Count, value in the Deployment Descriptor will be used",e);
                }
                try {
                    int maxwallTime = computionnalResource.getWallTimeLimit();
                    if(maxwallTime>0){
                        app.setMaxWallTime(maxwallTime);
                    }
                } catch (NullPointerException e) {
                    log.debug("No Value sent in WorkflowContextHeader for Node Count, value in the Deployment Descriptor will be used");
                     new GFacProviderException("No Value sent in WorkflowContextHeader for Node Count, value in the Deployment Descriptor will be used",e);
                }
        }
        if (app.getNodeCount() > 0) {
            jobAttr.set("hostCount", String.valueOf(app.getNodeCount()));
            log.debug("Setting number of Nodes to " + app.getCpuCount());
        }
        if (app.getCpuCount() > 0) {
            log.debug("Setting number of procs to " + app.getCpuCount());
            jobAttr.setNumProcs(app.getCpuCount());
        }
        if (app.getMinMemory() > 0) {
            log.debug("Setting minimum memory to " + app.getMinMemory());
            jobAttr.setMinMemory(app.getMinMemory());
        }
        if (app.getMaxMemory() > 0) {
            log.debug("Setting maximum memory to " + app.getMaxMemory());
            jobAttr.setMaxMemory(app.getMaxMemory());
        }
        if (app.getProjectAccount() != null) {
            if (app.getProjectAccount().getProjectAccountNumber() != null) {
                log.debug("Setting project to " + app.getProjectAccount().getProjectAccountNumber());
                jobAttr.setProject(app.getProjectAccount().getProjectAccountNumber());
            }
        }
        if (app.getQueue() != null) {
            if (app.getQueue().getQueueName() != null) {
                log.debug("Setting job queue to " + app.getQueue().getQueueName());
                jobAttr.setQueue(app.getQueue().getQueueName());
            }
        }
        if (app.getMaxWallTime() > 0) {
            log.debug("Setting max wall clock time to " + app.getMaxWallTime());

            jobAttr.setMaxWallTime(app.getMaxWallTime());
            jobAttr.set("proxy_timeout", "1");
        } else {
            jobAttr.setMaxWallTime(30);
        }
        String jobType = JobType.SINGLE.toString();
        if (app.getJobType() != null) {
            jobType = app.getJobType().toString();
        }
        if (jobType.equalsIgnoreCase(JobType.SINGLE.toString())) {
            log.debug("Setting job type to single");
            jobAttr.setJobType(GramAttributes.JOBTYPE_SINGLE);
        } if (jobType.equalsIgnoreCase(JobType.SERIAL.toString())) {
            log.debug("Setting job type to single");
            jobAttr.setJobType(GramAttributes.JOBTYPE_SINGLE);
        } else if (jobType.equalsIgnoreCase(JobType.MPI.toString())) {
            log.debug("Setting job type to mpi");
            jobAttr.setJobType(GramAttributes.JOBTYPE_MPI);
        } else if (jobType.equalsIgnoreCase(JobType.MULTIPLE.toString())) {
            log.debug("Setting job type to multiple");
            jobAttr.setJobType(GramAttributes.JOBTYPE_MULTIPLE);
        } else if (jobType.equalsIgnoreCase(JobType.CONDOR.toString())) {
            jobAttr.setJobType(GramAttributes.JOBTYPE_CONDOR);
        }

        return jobAttr;
    }
}
