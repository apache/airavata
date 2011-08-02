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

import java.util.Iterator;
import java.util.Map;

import org.apache.airavata.core.gfac.context.ExecutionContext;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.exception.GfacException.FaultCode;
import org.apache.airavata.core.gfac.utils.GFacConstants;
import org.globus.gram.GramAttributes;
import org.ogce.namespaces.x2010.x08.x30.workflowContextHeader.WorkflowContextHeaderDocument.WorkflowContextHeader;
import org.ogce.namespaces.x2010.x08.x30.workflowResourceMapping.ResourceMappingDocument.ResourceMapping;
import org.ogce.schemas.gfac.documents.ApplicationDescriptionType;
import org.ogce.schemas.gfac.documents.RSLParmType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GramRSLGenerator {
    protected final static Logger log = LoggerFactory.getLogger(GramRSLGenerator.class);

    private enum JobType {
        SINGLE, MPI, MULTIPLE, CONDOR
    };

    public static GramAttributes configureRemoteJob(ExecutionContext appExecContext) throws GfacException {
        GramAttributes jobAttr = new GramAttributes();
        jobAttr.setExecutable(appExecContext.getExecutionModel().getExecutable());
        jobAttr.setDirectory(appExecContext.getExecutionModel().getWorkingDir());
        jobAttr.setStdout(appExecContext.getExecutionModel().getStdOut());
        jobAttr.setStderr(appExecContext.getExecutionModel().getStderr());

        // The env here contains the env of the host and the application. i.e
        // the env specified in
        // the host description and application description documents
        Map<String, String> nv = appExecContext.getExecutionModel().getEnv();

        for (String key : nv.keySet()) {
            jobAttr.addEnvVariable(key, nv.get(key));
        }

        jobAttr.addEnvVariable(GFacConstants.INPUT_DATA_DIR, appExecContext.getExecutionModel().getInputDataDir());
        String outputDataDir = GFacConstants.OUTPUT_DATA_DIR;
        if (!outputDataDir.isEmpty()) {
            jobAttr.addEnvVariable(outputDataDir, appExecContext.getExecutionModel().getOutputDataDir());
        }
        ApplicationDescriptionType app = appExecContext.getExecutionModel().getAplicationDesc();
        WorkflowContextHeader contextHeader = appExecContext.getWorkflowHeader();
        ResourceMapping resourceMapping = null;
        if (contextHeader != null) {
            resourceMapping = contextHeader.getResourceMappings().getResourceMappingArray(0);
        }

        log.info("Configure using App Desc = " + app.xmlText());
        if (resourceMapping != null && resourceMapping.getMaxWallTime() > 0) {
            log.info("Header Setting Max Wall Time" + resourceMapping.getMaxWallTime());
            jobAttr.setMaxWallTime(resourceMapping.getMaxWallTime());

        } else if (app.getMaxWallTime() > 0) {
            log.info("Setting max wall clock time to " + app.getMaxWallTime());

            if (app.getMaxWallTime() > 30 && app.getQueue() != null && app.getQueue().equals("debug")) {
                throw new GfacException("NCSA debug Queue only support jobs < 30 minutes", FaultCode.InvalidConfig);
            }

            jobAttr.setMaxWallTime(app.getMaxWallTime());
            jobAttr.set("proxy_timeout", "1");
        } else {
            jobAttr.setMaxWallTime(29);
        }

        if (appExecContext.getExecutionModel().getStdIn() != null) {
            jobAttr.setStdin(appExecContext.getExecutionModel().getStdIn());
        } else {
            Iterator<String> values = appExecContext.getExecutionModel().getInputParameters().iterator();
            while (values.hasNext()) {
                jobAttr.addArgument(values.next());
            }
        }

        if (resourceMapping != null && resourceMapping.getNodeCount() > 0) {
            log.info("Setting number of procs to " + resourceMapping.getNodeCount());
            jobAttr.set("hostCount", String.valueOf(resourceMapping.getNodeCount()));
        } else if (app.getHostCount() > 1) {
            jobAttr.set("hostCount", String.valueOf(app.getHostCount()));
        }
        if (resourceMapping != null && resourceMapping.getCpuCount() > 0) {
            log.info("Setting host count to " + resourceMapping.getCpuCount());
            jobAttr.setNumProcs(resourceMapping.getCpuCount());

        } else if (app.getCount() > 1) {
            log.info("Setting number of procs to " + app.getCount());
            jobAttr.setNumProcs(app.getCount());
        }

        if (app.getProject() != null && app.getProject().getProjectName() != null) {
            log.info("Setting project to " + app.getProject());
            jobAttr.setProject(app.getProject().getProjectName());
        }

        if (resourceMapping != null && resourceMapping.getQueueName() != null) {
            jobAttr.setQueue(resourceMapping.getQueueName());
        } else if (app.getQueue() != null && app.getQueue().getQueueName() != null) {
            log.info("Setting job queue to " + app.getQueue());
            jobAttr.setQueue(app.getQueue().getQueueName());
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

        // Support to add the Additional RSL parameters
        RSLParmType[] rslParams = app.getRslparmArray();
        if (rslParams.length > 0) {
            for (RSLParmType rslType : rslParams) {
                log.info("Adding rsl param of [" + rslType.getName() + "," + rslType.getStringValue() + "]");
                if (rslType.getName() != "") {
                    jobAttr.set(rslType.getName(), rslType.getStringValue());
                }
            }
        }

        // support urgency/SPRUCE case
        // only add spruce rsl parameter if this host has a spruce jobmanager
        // configured
        if (appExecContext.getWorkflowHeader() != null && appExecContext.getWorkflowHeader().getURGENCY() != null
        // && GfacUtils.getSpruceGatekeeper(appExecContext) != null
        ) {
            jobAttr.set("urgency", appExecContext.getWorkflowHeader().getURGENCY());
        }

        return jobAttr;
    }
}
