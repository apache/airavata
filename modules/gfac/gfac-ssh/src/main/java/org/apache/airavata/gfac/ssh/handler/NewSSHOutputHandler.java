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

package org.apache.airavata.gfac.ssh.handler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.handler.AbstractHandler;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gfac.ssh.security.SSHSecurityContext;
import org.apache.airavata.gfac.ssh.util.GFACSSHUtils;
import org.apache.airavata.gfac.ssh.util.HandleOutputs;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.workspace.experiment.CorrectiveAction;
import org.apache.airavata.model.workspace.experiment.ErrorCategory;
import org.apache.airavata.registry.cpi.ChildDataType;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewSSHOutputHandler extends AbstractHandler{

	 private static final Logger log = LoggerFactory.getLogger(NewSSHOutputHandler.class);

	    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
	        String hostAddress = jobExecutionContext.getHostName();
	      	Cluster cluster = null;
	      	// Security Context and connection
	        try {
	            if (jobExecutionContext.getSecurityContext(hostAddress) == null) {
	                GFACSSHUtils.addSecurityContext(jobExecutionContext);
	            }
	            cluster = ((SSHSecurityContext) jobExecutionContext.getSecurityContext(hostAddress)).getPbsCluster();
	            if (cluster == null) {
	                throw new GFacProviderException("Security context is not set properly");
	            } else {
	                log.info("Successfully retrieved the Security Context");
	            }
	        } catch (Exception e) {
	            log.error(e.getMessage());
	            try {
                    StringWriter errors = new StringWriter();
                    e.printStackTrace(new PrintWriter(errors));
	                GFacUtils.saveErrorDetails(jobExecutionContext,  errors.toString(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
	            } catch (GFacException e1) {
	                log.error(e1.getLocalizedMessage());
	            }
	            throw new GFacHandlerException("Error while creating SSHSecurityContext", e, e.getLocalizedMessage());
	        }

	        super.invoke(jobExecutionContext);
	        List<OutputDataObjectType> outputArray =  HandleOutputs.handleOutputs(jobExecutionContext, cluster);
	        try {
				registry.add(ChildDataType.EXPERIMENT_OUTPUT, outputArray, jobExecutionContext.getExperimentID());
			} catch (RegistryException e) {
				throw new GFacHandlerException(e);
			}

	       
	    }

    @Override
    public void recover(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        // TODO: Auto generated method body.
    }

    @Override
	public void initProperties(Properties properties) throws GFacHandlerException {
		// TODO Auto-generated method stub
		
	}

}
