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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.airavata.core.gfac.context.ExecutionContext;
import org.apache.airavata.core.gfac.context.InvocationContext;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.exception.GfacException.FaultCode;
import org.apache.airavata.core.gfac.exception.JobSubmissionFault;
import org.apache.airavata.core.gfac.notification.NotificationService;
import org.apache.airavata.core.gfac.utils.GFacConstants;
import org.apache.airavata.core.gfac.utils.GFacOptions.CurrentProviders;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.apache.airavata.core.gfac.utils.OutputUtils;

import edu.indiana.extreme.lead.workflow_tracking.common.DurationObj;

public class LocalProvider extends AbstractProvider {

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
        ExecutionContext context = invocationContext.getExecutionContext();

        log.info("working diectroy = " + context.getExecutionModel().getWorkingDir());
        log.info("temp directory = " + context.getExecutionModel().getTmpDir());
        new File(context.getExecutionModel().getWorkingDir()).mkdir();
        new File(context.getExecutionModel().getTmpDir()).mkdir();
        new File(context.getExecutionModel().getInputDataDir()).mkdir();
        new File(context.getExecutionModel().getOutputDataDir()).mkdir();
    }

    public void execute(InvocationContext invocationContext) throws GfacException {
        ExecutionContext context = invocationContext.getExecutionContext();

        List<String> cmdList = new ArrayList<String>();

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
            ProcessBuilder builder = new ProcessBuilder(cmdList);

            // get the env of the host and the application
            Map<String, String> nv = context.getExecutionModel().getEnv();
            builder.environment().putAll(nv);

            // extra env's
            builder.environment().put(GFacConstants.INPUT_DATA_DIR, context.getExecutionModel().getInputDataDir());
            builder.environment().put(GFacConstants.OUTPUT_DATA_DIR, context.getExecutionModel().getOutputDataDir());

            // working directory
            builder.directory(new File(context.getExecutionModel().getWorkingDir()));

            // log info
            log.info("Command = " + buildCommand(cmdList));
            log.info("Working dir = " + builder.directory());
            for (String key : builder.environment().keySet()) {
                log.info("Env[" + key + "] = " + builder.environment().get(key));
            }

            // notify start
            DurationObj compObj = notifier.computationStarted();

            // running cmd
            Process process = builder.start();

            final BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            final BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            final BufferedWriter stdoutWtiter = new BufferedWriter(new FileWriter(context.getExecutionModel()
                    .getStdOut()));
            final BufferedWriter stdErrWtiter = new BufferedWriter(new FileWriter(context.getExecutionModel()
                    .getStderr()));

            Thread t1 = new Thread(new Runnable() {

                public void run() {
                    try {
                        String line = null;
                        while ((line = in.readLine()) != null) {
                            log.debug(line);
                            stdoutWtiter.write(line);
                            stdoutWtiter.newLine();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (stdoutWtiter != null) {
                            try {
                                stdoutWtiter.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            });

            Thread t2 = new Thread(new Runnable() {

                public void run() {
                    try {
                        String line = null;
                        while ((line = err.readLine()) != null) {
                            log.debug(line);
                            stdErrWtiter.write(line);
                            stdErrWtiter.newLine();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (err != null) {
                            try {
                                err.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (stdErrWtiter != null) {
                            try {
                                stdErrWtiter.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }

            });

            // start output threads
            t1.setDaemon(true);
            t2.setDaemon(true);
            t1.start();
            t2.start();

            // wait for the process (application) to finish executing
            int returnValue = process.waitFor();

            // notify end
            notifier.computationFinished(compObj);

            // make sure other two threads are done
            t1.join();
            t2.join();

            /*
             * check return value. usually not very helpful to draw conclusions based on return values so don't bother.
             * just provide warning in the log messages
             */
            if (returnValue != 0) {
                log.error("Process finished with non zero return value. Process may have failed");
            } else {
                log.info("Process finished with return value of zero.");
            }

            StringBuffer buf = new StringBuffer();
            buf.append("Executed ").append(buildCommand(cmdList)).append(" on the localHost, working directory =")
                    .append(context.getExecutionModel().getWorkingDir()).append("tempDirectory =")
                    .append(context.getExecutionModel().getTmpDir()).append("With the status")
                    .append(String.valueOf(returnValue));
            context.getNotificationService().info(buf.toString());

            log.info(buf.toString());

            context.getExecutionModel().setStdoutStr(GfacUtils.readFile(context.getExecutionModel().getStdOut()));
            context.getExecutionModel().setStderrStr(GfacUtils.readFile(context.getExecutionModel().getStderr()));

            // set to context
            OutputUtils.fillOutputFromStdout(invocationContext.getMessageContext("output"), context.getExecutionModel()
                    .getStdoutStr(), context.getExecutionModel().getStderrStr());

        } catch (IOException e) {
            throw new JobSubmissionFault(e, "", "", buildCommand(cmdList), CurrentProviders.Local);
        } catch (InterruptedException e) {
            throw new GfacException(e, FaultCode.LocalError);
        }

    }

    public void dispose(InvocationContext invocationContext) throws GfacException {

    }

    public void abort(InvocationContext invocationContext) throws GfacException {

    }

}
