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
import org.apache.airavata.gfac.context.security.SSHSecurityContext;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SCPDirectorySetupHandler implements GFacHandler{
    private static final Logger log = LoggerFactory.getLogger(SCPDirectorySetupHandler.class);

	public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException,GFacException {
		log.info("Setup SSH job directorties");
		makeDirectory(jobExecutionContext);

	}
	private void makeDirectory(JobExecutionContext context) throws GFacHandlerException,GFacException {
		SSHSecurityContext securityContext = (SSHSecurityContext)context.getSecurityContext(SSHSecurityContext.SSH_SECURITY_CONTEXT);
		ApplicationDeploymentDescriptionType app = context.getApplicationContext().getApplicationDeploymentDescription().getType();
		Session session = null;
		try {
			session = securityContext.getSession(context.getApplicationContext().getHostDescription().getType().getHostAddress());

			StringBuilder commandString = new StringBuilder();

			commandString.append("mkdir -p ");
			commandString.append(app.getScratchWorkingDirectory());
			commandString.append(" ; ");
			commandString.append("mkdir -p ");
			commandString.append(app.getStaticWorkingDirectory());
			commandString.append(" ; ");
			commandString.append("mkdir -p ");
			commandString.append(app.getInputDataDirectory());
			commandString.append(" ; ");
			commandString.append("mkdir -p ");
			commandString.append(app.getOutputDataDirectory());

			Command cmd = session.exec(commandString.toString());
			cmd.join(Constants.COMMAND_EXECUTION_TIMEOUT, TimeUnit.SECONDS);
		} catch (ConnectionException e) {
			throw new GFacHandlerException(e.getMessage(), e, context);
		} catch (TransportException e) {
			throw new GFacHandlerException(e.getMessage(), e, context);
		} catch (IOException e) {
			throw new GFacHandlerException(e.getMessage(), e, context);
		} finally {
			securityContext.closeSession(session);
		}
	}

    public void init(Map<String, String> properties) throws GFacHandlerException, GFacException {

    }
}
