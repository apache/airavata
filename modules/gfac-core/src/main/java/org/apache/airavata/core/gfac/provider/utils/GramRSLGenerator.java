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

import org.apache.airavata.commons.gfac.type.app.GramApplicationDeployment;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.exception.ToolsException;
import org.apache.airavata.core.gfac.utils.GFacConstants;
import org.apache.airavata.schemas.gfac.ShellApplicationDeploymentType;
import org.globus.gram.GramAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utilities for generating GRAM RSL
 */
public class GramRSLGenerator {
    protected static final Logger log = LoggerFactory.getLogger(GramRSLGenerator.class);

    private enum JobType {
        SINGLE, MPI, MULTIPLE, CONDOR
    };

    public static GramAttributes configureRemoteJob(InvocationContext context) throws ToolsException {
        GramApplicationDeployment app = (GramApplicationDeployment) context.getExecutionDescription().getApp();

        GramAttributes jobAttr = new GramAttributes();
        jobAttr.setExecutable(app.getExecutable());
        jobAttr.setDirectory(app.getWorkingDir());
        jobAttr.setStdout(app.getStdOut());
        jobAttr.setStderr(app.getStdErr());

        /*
         * The env here contains the env of the host and the application. i.e the env specified in the host description
         * and application description documents
         */
        ShellApplicationDeploymentType.Env.Entry[] env = app.getEnv().getEntryArray();

        Map<String, String> nv = null;
        for (int i = 0; i < env.length; i++) {
            String key = app.getEnv().getEntryArray(i).getKey();
            String value = app.getEnv().getEntryArray(i).getValue();
            nv.put(key, value);
        }

        for (Entry<String, String> entry : nv.entrySet()) {
            jobAttr.addEnvVariable(entry.getKey(), entry.getValue());
        }

        jobAttr.addEnvVariable(GFacConstants.INPUT_DATA_DIR_VAR_NAME, app.getInputDir());
        jobAttr.addEnvVariable(GFacConstants.INPUT_DATA_DIR_VAR_NAME, app.getOutputDir());

        if (app.getWallTime() > 0) {
            log.info("Setting max wall clock time to " + app.getWallTime());

            if (app.getWallTime() > 30 && app.getQueueName() != null && app.getQueueName().equals("debug")) {
                throw new ToolsException("NCSA debug Queue only support jobs < 30 minutes");
            }

            jobAttr.setMaxWallTime(app.getWallTime());
            jobAttr.set("proxy_timeout", "1");
        } else {
            jobAttr.setMaxWallTime(29);
        }

        if (app.getStdIn() != null) {
            jobAttr.setStdin(app.getStdIn());
        } else {
            // input parameter
            for (Iterator<String> iterator = context.getInput().getNames(); iterator.hasNext();) {
                String key = iterator.next();
                jobAttr.addArgument(context.getInput().getStringValue(key));
            }
        }

        if (app.getNodeCount() > 1) {
            jobAttr.set("hostCount", String.valueOf(app.getNodeCount()));
        }
        if (app.getCpuCount() > 1) {
            log.info("Setting number of procs to " + app.getCpuCount());
            jobAttr.setNumProcs(app.getCpuCount());
        }
        if (app.getProjectName() != null) {
            log.info("Setting project to " + app.getProjectName());
            jobAttr.setProject(app.getProjectName());
        }
        if (app.getQueueName() != null) {
            log.info("Setting job queue to " + app.getQueueName());
            jobAttr.setQueue(app.getQueueName());
        }

        String jobType = JobType.SINGLE.toString();
        if (app.getJobType() != null) {
            jobType = app.getJobType();
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

        // TODO rsl parameter & urgency/SPRUCE

        return jobAttr;
    }
}
