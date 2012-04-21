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

package org.apache.airavata.core.gfac.provider.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.context.security.impl.SSHSecurityContextImpl;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.exception.ProviderException;
import org.apache.airavata.core.gfac.provider.AbstractProvider;
import org.apache.airavata.core.gfac.utils.GFacConstants;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.apache.airavata.core.gfac.utils.InputUtils;
import org.apache.airavata.core.gfac.utils.OutputUtils;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.NameValuePairType;
import org.apache.xmlbeans.XmlException;

/**
 * Execute application using remote SSH
 */
public class SSHProvider extends AbstractProvider {

    private static final String SPACE = " ";
    private static final String SSH_SECURITY_CONTEXT = "ssh";
    private static final int COMMAND_EXECUTION_TIMEOUT = 5;
    private SSHSecurityContextImpl sshContext;
    private String command;
    private SSHClient ssh;

    public SSHProvider() {
        ssh = new SSHClient();
    }

    private Session getSession(InvocationContext context) throws IOException {
        try {

            /*
             * if it is connected, create a session Note: one client can have multiple session (on one channel)
             */
            if (ssh.isConnected())
                return ssh.startSession();

            if (sshContext == null) {
                sshContext = ((SSHSecurityContextImpl) context.getSecurityContext(SSH_SECURITY_CONTEXT));
            }

            KeyProvider pkey = ssh.loadKeys(sshContext.getPrivateKeyLoc(), sshContext.getKeyPass());

            ssh.loadKnownHosts();
            ssh.authPublickey(sshContext.getUsername(), pkey);

            ssh.connect(context.getExecutionDescription().getHost().getType().getHostAddress());
            return ssh.startSession();

        } catch (NullPointerException ne) {
            throw new SecurityException("Cannot load security context for SSH", ne);
        }
    }

    private void closeSession(Session session) {
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                log.warn("Cannot Close SSH Session");
            }
        }
    }

    public void makeDirectory(InvocationContext context) throws ProviderException {
        ApplicationDeploymentDescriptionType app = context.getExecutionDescription().getApp().getType();

        Session session = null;
        try {
            session = getSession(context);

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
            cmd.join(COMMAND_EXECUTION_TIMEOUT, TimeUnit.SECONDS);
        } catch (ConnectionException e) {
            throw new ProviderException(e.getMessage(), e);
        } catch (TransportException e) {
            throw new ProviderException(e.getMessage(), e);
        } catch (IOException e) {
            throw new ProviderException(e.getMessage(), e);
        } finally {
            closeSession(session);
        }
    }

    public void setupEnvironment(InvocationContext context) throws ProviderException {
        ApplicationDeploymentDescriptionType app = context.getExecutionDescription().getApp().getType();

        // input parameter
        ArrayList<String> tmp = new ArrayList<String>();
        for (Iterator<String> iterator = context.getInput().getNames(); iterator.hasNext();) {
            String key = iterator.next();
            tmp.add(context.getInput().getStringValue(key));
        }

        List<String> cmdList = new ArrayList<String>();

        /*
         * Builder Command
         */
        cmdList.add(app.getExecutableLocation());
        cmdList.addAll(tmp);

        // create process builder from command
        command = InputUtils.buildCommand(cmdList);

        // redirect StdOut and StdErr
        // TODO: Make 1> and 2> into static constants.
        // TODO: This only works for the BASH shell. CSH and TCSH will be
        // different.
        command += SPACE + "1>" + SPACE + app.getStandardOutput();
        command += SPACE + "2>" + SPACE + app.getStandardError();
    }

    public void executeApplication(InvocationContext context) throws ProviderException {
        ApplicationDeploymentDescriptionType app = context.getExecutionDescription().getApp().getType();

        Session session = null;
        try {
            session = getSession(context);

            /*
             * Going to working Directory
             */
            session.exec("cd " + app.getStaticWorkingDirectory());

            // get the env of the host and the application
            NameValuePairType[] env = app.getApplicationEnvironmentArray();

            Map<String, String> nv = new HashMap<String, String>();
            if (env != null) {
                for (int i = 0; i < env.length; i++) {
                    String key = env[i].getName();
                    String value = env[i].getValue();
                    nv.put(key, value);
                }
            }
            // extra env's
            nv.put(GFacConstants.INPUT_DATA_DIR_VAR_NAME, app.getInputDataDirectory());
            nv.put(GFacConstants.OUTPUT_DATA_DIR_VAR_NAME, app.getOutputDataDirectory());

            /*
             * Set environment
             */
            log.info("Command = " + command);
            for (Entry<String, String> entry : nv.entrySet()) {
                log.info("Env[" + entry.getKey() + "] = " + entry.getValue());
                session.setEnvVar(entry.getKey(), entry.getValue());
            }

            /*
             * Execute
             */
            Command cmd = session.exec(command);
            log.info("stdout=" + GfacUtils.readFromStream(session.getInputStream()));
            cmd.join(COMMAND_EXECUTION_TIMEOUT, TimeUnit.SECONDS);

            /*
             * check return value. usually not very helpful to draw conclusions based on return values so don't bother.
             * just provide warning in the log messages
             */
            if (cmd.getExitStatus() != 0) {
                log.error("Process finished with non zero return value. Process may have failed");
            } else {
                log.info("Process finished with return value of zero.");
            }

        } catch (ConnectionException e) {
            throw new  ProviderException(e.getMessage(), e);
        } catch (TransportException e) {
            throw new ProviderException(e.getMessage(), e);
        } catch (IOException e) {
            throw new ProviderException(e.getMessage(), e);
        } finally {
            closeSession(session);
        }
    }

    public Map<String, ?> processOutput(InvocationContext context) throws ProviderException {
        ApplicationDeploymentDescriptionType app = context.getExecutionDescription().getApp().getType();
        try {

            // Get the Stdouts and StdErrs
            String timeStampedServiceName = GfacUtils.createUniqueNameForService(context.getServiceName());
            File localStdOutFile = File.createTempFile(timeStampedServiceName, "stdout");
            File localStdErrFile = File.createTempFile(timeStampedServiceName, "stderr");

            SCPFileTransfer fileTransfer = ssh.newSCPFileTransfer();
            fileTransfer.download(app.getStandardOutput(), localStdOutFile.getAbsolutePath());
            fileTransfer.download(app.getStandardError(), localStdErrFile.getAbsolutePath());

            String stdOutStr = GfacUtils.readFileToString(localStdOutFile.getAbsolutePath());
            String stdErrStr = GfacUtils.readFileToString(localStdErrFile.getAbsolutePath());

            return OutputUtils.fillOutputFromStdout(context.<ActualParameter> getOutput(), stdOutStr);

        } catch (XmlException e) {
            throw new ProviderException("Cannot read output:" + e.getMessage(), e);
        } catch (ConnectionException e) {
            throw new ProviderException(e.getMessage(), e);
        } catch (TransportException e) {
            throw new ProviderException(e.getMessage(), e);
        } catch (IOException e) {
            throw new ProviderException(e.getMessage(), e);
        }
    }

    public void dispose(InvocationContext invocationContext) throws GfacException {
        super.dispose(invocationContext);
        try {
            if (ssh != null && ssh.isConnected()) {
                ssh.disconnect();
            }
        } catch (Exception e) {
            log.warn("Cannot Close SSH Connection");
        }
    }

	@Override
	protected Map<String, ?> processInput(InvocationContext invocationContext)
			throws ProviderException {
		// TODO Auto-generated method stub
		return null;
	}
}
