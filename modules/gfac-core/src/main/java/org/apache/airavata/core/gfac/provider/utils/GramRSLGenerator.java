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

package org.apache.airavata.core.gfac.provider.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.context.message.MessageContext;
import org.apache.airavata.core.gfac.exception.ToolsException;
import org.apache.airavata.core.gfac.utils.GFacConstants;
import org.apache.airavata.schemas.gfac.GramApplicationDeploymentType;
import org.apache.airavata.schemas.gfac.NameValuePairType;
import org.apache.airavata.schemas.gfac.URIArrayType;
import org.apache.airavata.schemas.wec.ContextHeaderDocument;
import org.globus.gram.GramAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for generating GRAM RSL
 */
public class GramRSLGenerator {
    protected static final Logger log = LoggerFactory.getLogger(GramRSLGenerator.class);

    private enum JobType {
        SINGLE, MPI, MULTIPLE, CONDOR
    };

    public static GramAttributes configureRemoteJob(InvocationContext context) throws ToolsException {
        GramApplicationDeploymentType app = (GramApplicationDeploymentType) context.getExecutionDescription().getApp().getType();

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
        if(env.length != 0){
            Map<String, String> nv =new HashMap<String, String>();
            for (int i = 0; i < env.length; i++) {
                String key = env[i].getName();
                String value = env[i].getValue();
                nv.put(key, value);
            }

            for (Entry<String, String> entry : nv.entrySet()) {
                jobAttr.addEnvVariable(entry.getKey(), entry.getValue());
            }
        }
        jobAttr.addEnvVariable(GFacConstants.INPUT_DATA_DIR_VAR_NAME, app.getInputDataDirectory());
        jobAttr.addEnvVariable(GFacConstants.OUTPUT_DATA_DIR_VAR_NAME, app.getOutputDataDirectory());

        if (app.getMaxWallTime() > 0) {
            log.info("Setting max wall clock time to " + app.getMaxWallTime());

            if (app.getMaxWallTime() > 30 && app.getQueue() != null && app.getQueue().getQueueName().equals("debug")) {
                throw new ToolsException("NCSA debug Queue only support jobs < 30 minutes");
            }

            jobAttr.setMaxWallTime(app.getMaxWallTime());
            jobAttr.set("proxy_timeout", "1");
        } else {
            jobAttr.setMaxWallTime(30);
        }

        if (app.getStandardInput() != null && !"".equals(app.getStandardInput())) {
            jobAttr.setStdin(app.getStandardInput());
		} else {
			MessageContext<Object> input = context.getInput();
			for (Iterator<String> iterator = input.getNames(); iterator.hasNext();) {
				String paramName = iterator.next();
				ActualParameter actualParameter = (ActualParameter) input.getValue(paramName);
				if ("URIArray".equals(actualParameter.getType().getType().toString())) {
					String[] values = ((URIArrayType) actualParameter.getType()).getValueArray();
					for (String value : values) {
						jobAttr.addArgument(value);
					}
				} else {
					String paramValue = input.getStringValue(paramName);
					jobAttr.addArgument(paramValue);
				}
			}
		}
        // Using the workflowContext Header values if user provided them in the request and overwrite the default values in DD
        ContextHeaderDocument.ContextHeader currentContextHeader = WorkflowContextHeaderBuilder.getCurrentContextHeader();
        if (currentContextHeader != null &&
                currentContextHeader.getWorkflowSchedulingContext().getApplicationSchedulingContextArray() != null &&
                currentContextHeader.getWorkflowSchedulingContext().getApplicationSchedulingContextArray().length > 0) {
            try {
                int cpuCount = currentContextHeader.getWorkflowSchedulingContext().getApplicationSchedulingContextArray()[0].getCpuCount();
                app.setCpuCount(cpuCount);
            } catch (NullPointerException e) {
                log.info("No Value sent in WorkflowContextHeader for CPU Count, value in the Deployment Descriptor will be used");
            }
            try {
                int nodeCount = currentContextHeader.getWorkflowSchedulingContext().getApplicationSchedulingContextArray()[0].getNodeCount();
                app.setNodeCount(nodeCount);
            } catch (NullPointerException e) {
                log.info("No Value sent in WorkflowContextHeader for Node Count, value in the Deployment Descriptor will be used");
            }
        }
        if (app.getNodeCount() > 0) {
            jobAttr.set("hostCount", String.valueOf(app.getNodeCount()));
        }
        if (app.getCpuCount() > 0) {
            log.info("Setting number of procs to " + app.getCpuCount());
            jobAttr.setNumProcs(app.getCpuCount());
        }
        if (app.getMinMemory() > 0) {
            log.info("Setting minimum memory to " + app.getMinMemory());
            jobAttr.setMinMemory(app.getMinMemory());
        }
        if (app.getMaxMemory() > 0) {
            log.info("Setting maximum memory to " + app.getMaxMemory());
            jobAttr.setMaxMemory(app.getMaxMemory());
        }
        if (app.getProjectAccount() != null) {
            if (app.getProjectAccount().getProjectAccountNumber() != null) {
                log.info("Setting project to " + app.getProjectAccount().getProjectAccountNumber());
                jobAttr.setProject(app.getProjectAccount().getProjectAccountNumber());
            }
        }
        if(app.getQueue() != null){
        if (app.getQueue().getQueueName() != null) {
            System.out.println("Testing");
            log.info("Setting job queue to " + app.getQueue().getQueueName());
            jobAttr.setQueue(app.getQueue().getQueueName());
        }
        }

        String jobType = JobType.SINGLE.toString();
        if (app.getJobType() != null) {
            jobType = app.getJobType().toString();
        }

        if (jobType.equalsIgnoreCase(JobType.SINGLE.toString())) {
            log.info("Setting job type to single");
            jobAttr.setJobType(GramAttributes.JOBTYPE_SINGLE);
        } else if (jobType.equalsIgnoreCase(JobType.MPI.toString())) {
            log.info("Setting job type to mpi");
            jobAttr.setJobType(GramAttributes.JOBTYPE_MPI);
        } else if (jobType.equalsIgnoreCase(JobType.MULTIPLE.toString())) {
            log.info("Setting job type to multiple");
            jobAttr.setJobType(GramAttributes.JOBTYPE_MULTIPLE);
        } else if (jobType.equalsIgnoreCase(JobType.CONDOR.toString())) {
            jobAttr.setJobType(GramAttributes.JOBTYPE_CONDOR);
        }

        return jobAttr;
    }
}
