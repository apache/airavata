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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;

import org.apache.airavata.core.gfac.context.ExecutionContext;
import org.apache.airavata.core.gfac.context.InvocationContext;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.model.ExecutionModel;
import org.apache.airavata.core.gfac.notification.NotificationService;
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

    public void initialize(InvocationContext invocationContext) throws GfacException {
        ExecutionContext appExecContext = invocationContext.getExecutionContext();
        ExecutionModel model = appExecContext.getExecutionModel();

        SSHClient ssh = new SSHClient();
        try {
            ssh.loadKnownHosts();
            ssh.connect(model.getHost());

            // TODO how to authenticate with system
            ssh.authPublickey(System.getProperty("user.name"));
            final Session session = ssh.startSession();
            try {
                StringBuilder command = new StringBuilder();
                command.append("mkdir -p ");
                command.append(model.getTmpDir());
                command.append(" | ");
                command.append("mkdir -p ");
                command.append(model.getWorkingDir());
                command.append(" | ");
                command.append("mkdir -p ");
                command.append(model.getInputDataDir());
                command.append(" | ");
                command.append("mkdir -p ");
                command.append(model.getOutputDataDir());
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
    
    public void execute(InvocationContext invocationContext) throws GfacException {
        ExecutionContext context = invocationContext.getExecutionContext();
        ExecutionModel model = context.getExecutionModel();

        List<String> cmdList = new ArrayList<String>();

        SSHClient ssh = new SSHClient();
        try {

            /*
             * Notifier
             */
            NotificationService notifier = context.getNotificationService();

            /*
             * Builder Command
             */
            cmdList.add(context.getExecutionModel().getExecutable());
            cmdList.addAll(context.getExecutionModel().getInputParameters());

            // create process builder from command
            String command = buildCommand(cmdList);

            // redirect StdOut and StdErr
            command += SPACE + "1>" + SPACE + model.getStdOut();
            command += SPACE + "2>" + SPACE + model.getStderr();

            // get the env of the host and the application
            Map<String, String> nv = context.getExecutionModel().getEnv();

            // extra env's
            nv.put(GFacConstants.INPUT_DATA_DIR, context.getExecutionModel().getInputDataDir());
            nv.put(GFacConstants.OUTPUT_DATA_DIR, context.getExecutionModel().getOutputDataDir());

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
            ssh.connect(model.getHost());

            // TODO how to authenticate with system
            ssh.authPublickey(System.getProperty("user.name"));

            final Session session = ssh.startSession();
            try {
                /*
                 * Build working Directory
                 */
                log.info("WorkingDir = " + model.getWorkingDir());
                session.exec("mkdir -p " + model.getWorkingDir());
                session.exec("cd " + model.getWorkingDir());

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
                QName x = QName.valueOf(invocationContext.getServiceName());
                String timeStampedServiceName = GfacUtils.createServiceDirName(x);
                File localStdOutFile = new File(logDir, timeStampedServiceName + ".stdout");
                File localStdErrFile = new File(logDir, timeStampedServiceName + ".stderr");

                SCPFileTransfer fileTransfer = ssh.newSCPFileTransfer();
                fileTransfer.download(model.getStdOut(), localStdOutFile.getAbsolutePath());
                fileTransfer.download(model.getStderr(), localStdErrFile.getAbsolutePath());

                context.getExecutionModel().setStdoutStr(GfacUtils.readFile(localStdOutFile.getAbsolutePath()));
                context.getExecutionModel().setStderrStr(GfacUtils.readFile(localStdErrFile.getAbsolutePath()));

                // set to context
                OutputUtils.fillOutputFromStdout(invocationContext.getMessageContext("output"), context
                        .getExecutionModel().getStdoutStr(), context.getExecutionModel().getStderrStr());

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
