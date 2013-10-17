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

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.TransportException;

import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.security.GSISecurityContext;
import org.apache.airavata.gfac.context.security.SSHSecurityContext;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SCPDirectorySetupHandler implements GFacHandler{
    private static final Logger log = LoggerFactory.getLogger(SCPDirectorySetupHandler.class);

	public void invoke(JobExecutionContext jobExecutionContext) throws GFacException {
		log.info("Setup SSH job directorties");
		makeDirectory(jobExecutionContext);

	}
	private void makeDirectory(JobExecutionContext jobExecutionContext) throws GFacException {
		Cluster cluster = null;
            if (jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT) != null) {
                cluster = ((GSISecurityContext) jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT)).getPbsCluster();
            } else {
                cluster = ((SSHSecurityContext) jobExecutionContext.getSecurityContext(SSHSecurityContext.SSH_SECURITY_CONTEXT)).getPbsCluster();
            }
            if (cluster == null) {
                throw new GFacHandlerException("Security context is not set properly");
            } else {
                log.info("Successfully retrieved the Security Context");
            }
		ApplicationDeploymentDescriptionType app = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getType();
		try {
            cluster.makeDirectory(app.getScratchWorkingDirectory());
            cluster.makeDirectory(app.getInputDataDirectory());
            cluster.makeDirectory(app.getOutputDataDirectory());
		} catch (SSHApiException e) {
            throw new GFacHandlerException("Error executing the Handler: " + SCPDirectorySetupHandler.class,e);  //To change body of catch statement use File | Settings | File Templates.
        }
	}

    public void initProperties(Map<String, String> properties) throws GFacHandlerException, GFacException {

    }
}
