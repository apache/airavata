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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.app.ShellApplicationDeployment;
import org.apache.airavata.commons.gfac.type.parameter.AbstractParameter;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.exception.ProviderException;
import org.apache.airavata.core.gfac.utils.GFacConstants;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.apache.airavata.core.gfac.utils.InputUtils;
import org.apache.airavata.core.gfac.utils.OutputUtils;

/**
 * {@link LocalProvider} will execute jobs (application) on local machine.
 * 
 */
public class LocalProvider extends AbstractProvider {

    private ProcessBuilder builder;
    private List<String> cmdList;

    private class ReadStreamWriteFile extends Thread {
        private BufferedReader in;
        private BufferedWriter out;

        public ReadStreamWriteFile(InputStream in, String out) throws IOException {
            this.in = new BufferedReader(new InputStreamReader(in));
            this.out = new BufferedWriter(new FileWriter(out));
        }

        public void run() {
            try {
                String line = null;
                while ((line = in.readLine()) != null) {
                    log.debug(line);
                    out.write(line);
                    out.newLine();
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
                if (out != null) {
                    try {
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void makeFileSystemDir(String dir) throws ProviderException {
        File f = new File(dir);
        if (f.isDirectory() && f.exists()) {
            return;
        } else if (!new File(dir).mkdir()) {
            throw new ProviderException("Cannot make directory");
        }
    }

    public void makeDirectory(InvocationContext invocationContext) throws ProviderException {
        ApplicationDeploymentDescription app = invocationContext.getExecutionDescription().getApp();

        log.info("working diectroy = " + app.getWorkingDir());
        log.info("temp directory = " + app.getTmpDir());

        makeFileSystemDir(app.getWorkingDir());
        makeFileSystemDir(app.getTmpDir());
        makeFileSystemDir(app.getInputDir());
        makeFileSystemDir(app.getOutputDir());
    }

    public void setupEnvironment(InvocationContext context) throws ProviderException {
        ShellApplicationDeployment app = (ShellApplicationDeployment) context.getExecutionDescription().getApp();

        // input parameter
        ArrayList<String> tmp = new ArrayList<String>();
        for (Iterator<String> iterator = context.getInput().getNames(); iterator.hasNext();) {
            String key = iterator.next();
            tmp.add(context.getInput().getStringValue(key));
        }

        cmdList = new ArrayList<String>();

        /*
         * Builder Command
         */
        cmdList.add(app.getExecutable());
        cmdList.addAll(tmp);

        // create process builder from command
        this.builder = new ProcessBuilder(cmdList);

        // get the env of the host and the application
        Map<String, String> nv = app.getEnv();
        builder.environment().putAll(nv);

        // extra env's
        builder.environment().put(GFacConstants.INPUT_DATA_DIR_VAR_NAME, app.getInputDir());
        builder.environment().put(GFacConstants.OUTPUT_DATA_DIR_VAR_NAME, app.getOutputDir());

        // working directory
        builder.directory(new File(app.getWorkingDir()));

        // log info
        log.info("Command = " + InputUtils.buildCommand(cmdList));
        log.info("Working dir = " + builder.directory());
        for (String key : builder.environment().keySet()) {
            log.info("Env[" + key + "] = " + builder.environment().get(key));
        }
    }

    public void executeApplication(InvocationContext context) throws ProviderException {
        ShellApplicationDeployment app = (ShellApplicationDeployment) context.getExecutionDescription().getApp();

        try {
            // running cmd
            Process process = builder.start();

            Thread t1 = new ReadStreamWriteFile(process.getInputStream(), app.getStdOut());
            Thread t2 = new ReadStreamWriteFile(process.getErrorStream(), app.getStdErr());

            // start output threads
            t1.setDaemon(true);
            t2.setDaemon(true);
            t1.start();
            t2.start();

            // wait for the process (application) to finish executing
            int returnValue = process.waitFor();

            // make sure other two threads are done
            t1.join();
            t2.join();

            /*
             * check return value. usually not very helpful to draw conclusions
             * based on return values so don't bother. just provide warning in
             * the log messages
             */
            if (returnValue != 0) {
                log.error("Process finished with non zero return value. Process may have failed");
            } else {
                log.info("Process finished with return value of zero.");
            }

            StringBuffer buf = new StringBuffer();
            buf.append("Executed ").append(InputUtils.buildCommand(cmdList))
                    .append(" on the localHost, working directory = ").append(app.getWorkingDir())
                    .append(" tempDirectory = ").append(app.getTmpDir()).append(" With the status ")
                    .append(String.valueOf(returnValue));

            log.info(buf.toString());

        } catch (IOException io) {
            throw new ProviderException(io.getMessage(), io);
        } catch (InterruptedException e) {
            throw new ProviderException(e.getMessage(), e);
        }
    }

    public Map<String, ?> processOutput(InvocationContext context) throws ProviderException {

        ShellApplicationDeployment app = (ShellApplicationDeployment) context.getExecutionDescription().getApp();

        try {
            String stdOutStr = GfacUtils.readFileToString(app.getStdOut());

            // set to context
            return OutputUtils.fillOutputFromStdout(context.<AbstractParameter> getOutput(), stdOutStr);
        } catch (IOException io) {
            throw new ProviderException(io.getMessage(), io);
        }
    }
}
