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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.app.ShellApplicationDeployment;
import org.apache.airavata.commons.gfac.type.parameter.AbstractParameter;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.context.message.MessageContext;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.exception.GfacException.FaultCode;
import org.apache.airavata.core.gfac.notification.NotificationService;
import org.apache.airavata.core.gfac.utils.GFacConstants;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.apache.airavata.core.gfac.utils.OutputUtils;

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
        ApplicationDeploymentDescription app = invocationContext.getExecutionDescription().getApp();

        log.info("working diectroy = " + app.getWorkingDir());
        log.info("temp directory = " + app.getTmpDir());
        new File(app.getWorkingDir()).mkdir();
        new File(app.getTmpDir()).mkdir();
        new File(app.getInputDir()).mkdir();
        new File(app.getOutputDir()).mkdir();
    }

    public void execute(InvocationContext context) throws GfacException {
        ShellApplicationDeployment app = (ShellApplicationDeployment)context.getExecutionDescription().getApp();
        
        // input parameter
        ArrayList<String> tmp = new ArrayList<String>();
        for (Iterator<String> iterator = context.getInput().getNames(); iterator.hasNext();) {
            String key = iterator.next();
            tmp.add(context.getInput().getStringValue(key));
        }
        
        List<String> cmdList = new ArrayList<String>();

        try {
            /*
             * Builder Command
             */
            cmdList.add(app.getExecutable());
            cmdList.addAll(tmp);

            // create process builder from command
            ProcessBuilder builder = new ProcessBuilder(cmdList);

            // get the env of the host and the application
            Map<String, String> nv = app.getEnv();
            builder.environment().putAll(nv);

            // extra env's
            builder.environment().put(GFacConstants.INPUT_DATA_DIR_VAR_NAME, app.getInputDir());
            builder.environment().put(GFacConstants.OUTPUT_DATA_DIR_VAR_NAME, app.getOutputDir());

            // working directory
            builder.directory(new File(app.getWorkingDir()));

            // log info
            log.info("Command = " + buildCommand(cmdList));
            log.info("Working dir = " + builder.directory());
            for (String key : builder.environment().keySet()) {
                log.info("Env[" + key + "] = " + builder.environment().get(key));
            }

            NotificationService notifier = context.getExecutionContext().getNotificationService();
            notifier.startExecution(this, context);
            
            // running cmd
            Process process = builder.start();

            final BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            final BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            final BufferedWriter stdoutWtiter = new BufferedWriter(new FileWriter(app.getStdOut()));
            final BufferedWriter stdErrWtiter = new BufferedWriter(new FileWriter(app.getStdErr()));

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
            
            notifier.finishExecution(this, context);

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
            buf.append("Executed ")
            		.append(buildCommand(cmdList))
            		.append(" on the localHost, working directory = ")
                    .append(app.getWorkingDir())
                    .append(" tempDirectory = ")
                    .append(app.getTmpDir())
                    .append(" With the status ")
                    .append(String.valueOf(returnValue));

            log.info(buf.toString());

            String stdOutStr = GfacUtils.readFile(app.getStdOut());
            String stdErrStr = GfacUtils.readFile(app.getStdErr());

            // set to context
            OutputUtils.fillOutputFromStdout(context.<AbstractParameter>getOutput(), stdOutStr, stdErrStr);

        } catch (IOException e) {
        	log.error("error", e);
            throw new GfacException(e, FaultCode.LocalError);
        } catch (InterruptedException e) {
        	log.error("error", e);
            throw new GfacException(e, FaultCode.LocalError);
        } catch (Exception e){
        	log.error("error", e);
            throw new GfacException(e, FaultCode.LocalError);
        }

    }

    public void dispose(InvocationContext invocationContext) throws GfacException {

    }

    public void abort(InvocationContext invocationContext) throws GfacException {

    }

}
