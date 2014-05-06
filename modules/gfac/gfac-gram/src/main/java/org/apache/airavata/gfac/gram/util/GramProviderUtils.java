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
package org.apache.airavata.gfac.gram.util;

import org.apache.airavata.gfac.ToolsException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.globus.gram.GramAttributes;
import org.globus.gram.GramJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class GramProviderUtils {
    private static final Logger log = LoggerFactory.getLogger(GramProviderUtils.class);

    public static GramJob setupEnvironment(JobExecutionContext jobExecutionContext, boolean enableTwoPhase) throws GFacProviderException {
        log.debug("Searching for Gate Keeper");
        try {
            GramAttributes jobAttr = GramRSLGenerator.configureRemoteJob(jobExecutionContext);
            String rsl = jobAttr.toRSL();

            if (enableTwoPhase) {
                rsl = rsl + "(twoPhase=yes)";
            }

            log.debug("RSL = " + rsl);
            GramJob job = new GramJob(rsl);
            return job;
        } catch (ToolsException te) {
            throw new GFacProviderException(te.getMessage(), te);
        }
    }

     public static JobState getApplicationJobStatus(int gramStatus) {
        switch (gramStatus) {
            case GramJob.STATUS_UNSUBMITTED:
                return JobState.HELD;
            case GramJob.STATUS_ACTIVE:
                return JobState.ACTIVE;
            case GramJob.STATUS_DONE:
                return JobState.COMPLETE;
            case GramJob.STATUS_FAILED:
                return JobState.FAILED;
            case GramJob.STATUS_PENDING:
                return JobState.QUEUED;
            case GramJob.STATUS_STAGE_IN:
                return JobState.QUEUED;
            case GramJob.STATUS_STAGE_OUT:
                return JobState.COMPLETE;
            case GramJob.STATUS_SUSPENDED:
                return JobState.SUSPENDED;
            default:
                return JobState.UNKNOWN;
        }
    }

    public static URI createGsiftpURI(String host, String localPath) throws URISyntaxException {
        StringBuffer buf = new StringBuffer();
        if (!host.startsWith("gsiftp://"))
            buf.append("gsiftp://");
        buf.append(host);
        if (!host.endsWith("/"))
            buf.append("/");
        buf.append(localPath);
        return new URI(buf.toString());
    }

     public static URI createGsiftpURI(GridFTPContactInfo host, String localPath) throws URISyntaxException {
        StringBuffer buf = new StringBuffer();

        if (!host.hostName.startsWith("gsiftp://"))
            buf.append("gsiftp://");
        buf.append(host).append(":").append(host.port);
        if (!host.hostName.endsWith("/"))
            buf.append("/");
        buf.append(localPath);
        return new URI(buf.toString());
    }

    public static String createGsiftpURIAsString(String host, String localPath) throws URISyntaxException {
        StringBuffer buf = new StringBuffer();
        if (!host.startsWith("gsiftp://"))
            buf.append("gsiftp://");
        buf.append(host);
        if (!host.endsWith("/"))
            buf.append("/");
        buf.append(localPath);
        return buf.toString();
    }

}
