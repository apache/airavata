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
package org.apache.airavata.gfac.handler;

import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.ToolsException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.security.GSISecurityContext;
import org.apache.airavata.gfac.external.GridFtp;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.gfac.utils.GFacUtils;
import org.apache.airavata.gfac.utils.GramJobSubmissionListener;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.apache.airavata.schemas.gfac.UnicoreHostType;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.UUID;

public class GramDirectorySetupHandler implements GFacHandler {
    private static final Logger log = LoggerFactory.getLogger(GramJobSubmissionListener.class);

    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException,GFacException {
        log.info("Invoking GramDirectorySetupHandler ...");


        String[] gridFTPEndpointArray = null;

        String hostName = null;

        //TODO: why it is tightly coupled with gridftp
//        GlobusHostType host = (GlobusHostType) jobExecutionContext.getApplicationContext().getHostDescription().getType();

        //TODO: make it more reusable
        HostDescriptionType hostType = jobExecutionContext.getApplicationContext().getHostDescription().getType();



        if(hostType instanceof GlobusHostType){
        	gridFTPEndpointArray = ((GlobusHostType) hostType).getGridFTPEndPointArray();
        }
        else if (hostType instanceof UnicoreHostType){
        	gridFTPEndpointArray = ((UnicoreHostType) hostType).getGridFTPEndPointArray();
        }
        else {
        	//TODO
        }


        ApplicationDescription applicationDeploymentDescription = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription();
        ApplicationDeploymentDescriptionType app = applicationDeploymentDescription.getType();
        GridFtp ftp = new GridFtp();

        try {
            GSSCredential gssCred = ((GSISecurityContext)jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT)).getGssCredentails();

            if (gridFTPEndpointArray == null || gridFTPEndpointArray.length == 0) {
            	gridFTPEndpointArray = new String[]{hostType.getHostAddress()};
            }
            boolean success = false;
            GFacHandlerException pe = null;// = new ProviderException("");
            for (String endpoint : gridFTPEndpointArray) {
                try {

                    URI tmpdirURI = GFacUtils.createGsiftpURI(endpoint, app.getScratchWorkingDirectory());
                    URI workingDirURI = GFacUtils.createGsiftpURI(endpoint, app.getStaticWorkingDirectory());
                    URI inputURI = GFacUtils.createGsiftpURI(endpoint, app.getInputDataDirectory());
                    URI outputURI = GFacUtils.createGsiftpURI(endpoint, app.getOutputDataDirectory());

                    log.info("Host FTP = " + gridFTPEndpointArray[0]);
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
                    pe = new GFacHandlerException("URI is malformatted:" + e.getMessage(), e, jobExecutionContext);

                } catch (ToolsException e) {
                    pe = new GFacHandlerException(e.getMessage(), e, jobExecutionContext);
                }
            }
            if (success == false) {
                throw pe;
            }
        } catch (SecurityException e) {
            throw new GFacHandlerException(e.getMessage(), e, jobExecutionContext);
        }
    }
}
