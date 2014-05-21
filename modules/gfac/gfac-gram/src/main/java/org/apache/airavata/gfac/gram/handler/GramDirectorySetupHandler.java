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
package org.apache.airavata.gfac.gram.handler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.handler.AbstractHandler;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gfac.gram.security.GSISecurityContext;
import org.apache.airavata.gfac.gram.external.GridFtp;
import org.apache.airavata.gfac.gram.util.GramProviderUtils;
import org.apache.airavata.model.workspace.experiment.CorrectiveAction;
import org.apache.airavata.model.workspace.experiment.DataTransferDetails;
import org.apache.airavata.model.workspace.experiment.ErrorCategory;
import org.apache.airavata.model.workspace.experiment.TransferState;
import org.apache.airavata.model.workspace.experiment.TransferStatus;
import org.apache.airavata.registry.cpi.ChildDataType;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.apache.airavata.schemas.gfac.UnicoreHostType;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class  GramDirectorySetupHandler extends AbstractHandler {
    private static final Logger log = LoggerFactory.getLogger(GramDirectorySetupHandler.class);
   
    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        log.info("Invoking GramDirectorySetupHandler ...");
        super.invoke(jobExecutionContext);
        String[] gridFTPEndpointArray = null;

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
        


        ApplicationDescription applicationDeploymentDescription = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription();
        ApplicationDeploymentDescriptionType app = applicationDeploymentDescription.getType();
        GridFtp ftp = new GridFtp();

        try {

            GSSCredential gssCred = ((GSISecurityContext)jobExecutionContext.
                    getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT)).getGssCredentials();

            if (gridFTPEndpointArray == null || gridFTPEndpointArray.length == 0) {
            	gridFTPEndpointArray = new String[]{hostType.getHostAddress()};
            }
            boolean success = false;
            GFacHandlerException pe = null;// = new ProviderException("");
            for (String endpoint : gridFTPEndpointArray) {
                try {

                    URI tmpdirURI = GramProviderUtils.createGsiftpURI(endpoint, app.getScratchWorkingDirectory());
                    URI workingDirURI = GramProviderUtils.createGsiftpURI(endpoint, app.getStaticWorkingDirectory());
                    URI inputURI = GramProviderUtils.createGsiftpURI(endpoint, app.getInputDataDirectory());
                    URI outputURI = GramProviderUtils.createGsiftpURI(endpoint, app.getOutputDataDirectory());

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
                    DataTransferDetails detail = new DataTransferDetails();
                    TransferStatus status = new TransferStatus();
                    status.setTransferState(TransferState.DIRECTORY_SETUP);
                    detail.setTransferStatus(status);
                    detail.setTransferDescription("Working directory = " + workingDirURI);
                    registry.add(ChildDataType.DATA_TRANSFER_DETAIL,detail, jobExecutionContext.getTaskData().getTaskID());
                                  
                    break;
                } catch (URISyntaxException e) {
                    pe = new GFacHandlerException("URI is malformatted:" + e.getMessage(), e);

                } catch (Exception e) {
              	pe = new GFacHandlerException(e.getMessage(), e);
                }
            }
            if (success == false) {
            	GFacUtils.saveErrorDetails(jobExecutionContext, pe.getLocalizedMessage(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.FILE_SYSTEM_FAILURE);
        		throw pe;
            }
        } catch (SecurityException e) {
            throw new GFacHandlerException(e.getMessage(), e);
        } catch (ApplicationSettingsException e1) {
        	throw new GFacHandlerException(e1.getMessage(), e1);
		} catch (GFacException e) {
            throw new GFacHandlerException(e);
        }
    }

    public void initProperties(Properties properties) throws GFacHandlerException {

    }
}
