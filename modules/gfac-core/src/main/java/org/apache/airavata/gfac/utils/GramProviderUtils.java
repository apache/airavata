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

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.gfac.ToolsException;
import org.apache.airavata.gfac.context.GSISecurityContext;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.external.GridFtp;
import org.apache.airavata.gfac.notification.events.ExecutionFailEvent;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.schemas.gfac.*;
import org.globus.gram.GramAttributes;
import org.globus.gram.GramJob;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class GramProviderUtils {
    private static final Logger log = LoggerFactory.getLogger(GramJobSubmissionListener.class);

    public static void makeDirectory(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        GlobusHostType host = (GlobusHostType) jobExecutionContext.getApplicationContext().getHostDescription().getType();
        ApplicationDeploymentDescriptionType app = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getType();
        GridFtp ftp = new GridFtp();

        try {
            GSISecurityContext gssContext = new GSISecurityContext(jobExecutionContext.getGFacConfiguration());
            GSSCredential gssCred = gssContext.getGssCredentails();
            String[] hostgridFTP = host.getGridFTPEndPointArray();
            if (hostgridFTP == null || hostgridFTP.length == 0) {
                hostgridFTP = new String[]{host.getHostAddress()};
            }
            boolean success = false;
            GFacProviderException pe = null;// = new ProviderException("");
            for (String endpoint : host.getGridFTPEndPointArray()) {
                try {

                    URI tmpdirURI = GFacUtils.createGsiftpURI(endpoint, app.getScratchWorkingDirectory());
                    URI workingDirURI = GFacUtils.createGsiftpURI(endpoint, app.getStaticWorkingDirectory());
                    URI inputURI = GFacUtils.createGsiftpURI(endpoint, app.getInputDataDirectory());
                    URI outputURI = GFacUtils.createGsiftpURI(endpoint, app.getOutputDataDirectory());

                    log.info("Host FTP = " + hostgridFTP[0]);
                    log.info("temp directory = " + tmpdirURI);
                    log.info("Working directory = " + workingDirURI);
                    log.info("Input directory = " + inputURI);
                    log.info("Output directory = " + outputURI);

                    ftp.makeDir(tmpdirURI, gssCred);
                    ftp.makeDir(workingDirURI, gssCred);
                    ftp.makeDir(inputURI, gssCred);
                    ftp.makeDir(outputURI, gssCred);

                    success = true;
                    break;
                } catch (URISyntaxException e) {
                    pe = new GFacProviderException("URI is malformatted:" + e.getMessage(), e, jobExecutionContext);

                } catch (ToolsException e) {
                    pe = new GFacProviderException(e.getMessage(), e, jobExecutionContext);
                }
            }
            if (success == false) {
                throw pe;
            }
        } catch (SecurityException e) {
            throw new GFacProviderException(e.getMessage(), e, jobExecutionContext);
        }
    }

    public static GramJob setupEnvironment(JobExecutionContext jobExecutionContext) throws GFacProviderException {
        log.debug("Searching for Gate Keeper");
        try {
            GramAttributes jobAttr = GramRSLGenerator.configureRemoteJob(jobExecutionContext);
            String rsl = jobAttr.toRSL();

            log.debug("RSL = " + rsl);
            GramJob job = new GramJob(rsl);
            return job;
        } catch (ToolsException te) {
            throw new GFacProviderException(te.getMessage(), te, jobExecutionContext);
        }
    }



}
