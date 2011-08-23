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

package org.apache.airavata.core.gfac.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;

import org.apache.airavata.core.gfac.context.InvocationContext;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.notification.NotificationService;
import org.apache.airavata.core.gfac.type.HostDescription;
import org.apache.airavata.core.gfac.type.app.ShellApplicationDeployment;
import org.apache.airavata.core.gfac.utils.GFacConstants;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.apache.airavata.core.gfac.utils.OutputUtils;

import edu.indiana.extreme.lead.workflow_tracking.common.DurationObj;

public class SSHProvider extends AbstractProvider {

    private static final String SPACE = " ";

    private String buildCommand(List<String> cmdList) {
        StringBuffer buff = new StringBuffer();
        for (String string : cmdList) {
            buff.append(string);
            buff.append(SPACE);
        }
        return buff.toString();
    }

    public void initialize(InvocationContext context) throws GfacException {
        HostDescription host = context.getGfacContext().getHost();
        ShellApplicationDeployment app = (ShellApplicationDeployment)context.getGfacContext().getApp();

        SSHClient ssh = new SSHClient();
        try {
            ssh.loadKnownHosts();
            ssh.connect(host.getName());

            // TODO how to authenticate with system
            ssh.authPublickey(System.getProperty("user.name"));
            final Session session = ssh.startSession();
            try {
                StringBuilder command = new StringBuilder();
                command.append("mkdir -p ");
                command.append(app.getTmpDir());
                command.append(" | ");
                command.append("mkdir -p ");
                command.append(app.getWorkingDir());
                command.append(" | ");
                command.append("mkdir -p ");
                command.append(app.getInputDir());
                command.append(" | ");
                command.append("mkdir -p ");
                command.append(app.getOutputDir());
                Command cmd = session.exec(command.toString());
                cmd.join(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw e;
            } finally {
                try {
                    session.close();
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            throw new GfacException(e.getMessage(), e);
        } finally {
            try {
                ssh.disconnect();
            } catch (Exception e) {
            }
        }
    }
    
    public void execute(InvocationContext context) throws GfacException {
        HostDescription host = context.getGfacContext().getHost();
        ShellApplicationDeployment app = (ShellApplicationDeployment)context.getGfacContext().getApp();

        // input parameter
        ArrayList<String> tmp = new ArrayList<String>();
        for (Iterator<String> iterator = context.getMessageContext("input").getParameterNames(); iterator.hasNext();) {
            String key = iterator.next();
            tmp.add(context.getMessageContext("input").getStringParameterValue(key));
        }
        
        List<String> cmdList = new ArrayList<String>();

        SSHClient ssh = new SSHClient();
        try {

            /*
             * Notifier
             */
            NotificationService notifier = context.getExecutionContext().getNotificationService();

            /*
             * Builder Command
             */
            cmdList.add(app.getExecutable());
            cmdList.addAll(tmp);

            // create process builder from command
            String command = buildCommand(cmdList);

            // redirect StdOut and StdErr
            command += SPACE + "1>" + SPACE + app.getStdOut();
            command += SPACE + "2>" + SPACE + app.getStdErr();

            // get the env of the host and the application
            Map<String, String> nv = app.getEnv();

            // extra env's
            nv.put(GFacConstants.INPUT_DATA_DIR_VAR_NAME, app.getInputDir());
            nv.put(GFacConstants.OUTPUT_DATA_DIR_VAR_NAME, app.getOutputDir());

            // log info
            log.info("Command = " + buildCommand(cmdList));
            for (String key : nv.keySet()) {
                log.info("Env[" + key + "] = " + nv.get(key));
            }

            // notify start
            DurationObj compObj = notifier.computationStarted();

            /*
             * Create ssh connection
             */
            ssh.loadKnownHosts();
            ssh.connect(host.getName());

            // TODO how to authenticate with system
            ssh.authPublickey(System.getProperty("user.name"));

            final Session session = ssh.startSession();
            try {
                /*
                 * Build working Directory
                 */
                log.info("WorkingDir = " + app.getWorkingDir());
                session.exec("mkdir -p " + app.getWorkingDir());
                session.exec("cd " + app.getWorkingDir());

                /*
                 * Set environment
                 */
                for (String key : nv.keySet()) {
                    session.setEnvVar(key, nv.get(key));
                }

                /*
                 * Execute
                 */
                Command cmd = session.exec(command);
                log.info("stdout=" + GfacUtils.readFromStream(session.getInputStream()));
                cmd.join(5, TimeUnit.SECONDS);

                // notify end
                notifier.computationFinished(compObj);

                /*
                 * check return value. usually not very helpful to draw conclusions based on return values so don't
                 * bother. just provide warning in the log messages
                 */
                if (cmd.getExitStatus() != 0) {
                    log.error("Process finished with non zero return value. Process may have failed");
                } else {
                    log.info("Process finished with return value of zero.");
                }

                File logDir = new File("./service_logs");
                if (!logDir.exists()) {
                    logDir.mkdir();
                }

                // Get the Stdouts and StdErrs
                QName x = QName.valueOf(context.getServiceName());
                String timeStampedServiceName = GfacUtils.createServiceDirName(x);
                File localStdOutFile = new File(logDir, timeStampedServiceName + ".stdout");
                File localStdErrFile = new File(logDir, timeStampedServiceName + ".stderr");

                SCPFileTransfer fileTransfer = ssh.newSCPFileTransfer();
                fileTransfer.download(app.getStdOut(), localStdOutFile.getAbsolutePath());
                fileTransfer.download(app.getStdErr(), localStdErrFile.getAbsolutePath());

                String stdOutStr = GfacUtils.readFile(localStdOutFile.getAbsolutePath());
                String stdErrStr = GfacUtils.readFile(localStdErrFile.getAbsolutePath());

                // set to context
                OutputUtils.fillOutputFromStdout(context.getMessageContext("output"), stdOutStr, stdErrStr);

            } catch (Exception e) {
                throw e;
            } finally {
                try {
                    session.close();
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            throw new GfacException(e.getMessage(), e);
        } finally {
            try {
                ssh.disconnect();
            } catch (Exception e) {
            }
        }
    }

    public void dispose(InvocationContext invocationContext) throws GfacException {
        // TODO Auto-generated method stub

    }

    public void abort(InvocationContext invocationContext) throws GfacException {
        // TODO Auto-generated method stub

    }

}
