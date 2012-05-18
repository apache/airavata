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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.exception.ProviderException;
import org.apache.airavata.core.gfac.provider.AbstractProvider;
import org.apache.airavata.core.gfac.provider.utils.InputStreamToFileWriter;
import org.apache.airavata.core.gfac.utils.GFacConstants;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.apache.airavata.core.gfac.utils.InputUtils;
import org.apache.airavata.core.gfac.utils.OutputUtils;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.NameValuePairType;
import org.apache.xmlbeans.XmlException;

/**
 * {@link LocalProvider} will execute jobs (application) on local machine.
 */
public class LocalProvider extends AbstractProvider {

    private ProcessBuilder builder;
    private List<String> cmdList;

    private void makeFileSystemDir(String dir) throws ProviderException {
        File f = new File(dir);
        if (f.isDirectory() && f.exists()) {
            return;
        } else if (!new File(dir).mkdir()) {
            throw new ProviderException("Cannot make directory");
        }
    }

    public void makeDirectory(InvocationContext invocationContext) throws ProviderException {
        ApplicationDeploymentDescriptionType app = invocationContext.getExecutionDescription().getApp().getType();

        log.info("working diectroy = " + app.getStaticWorkingDirectory());
        log.info("temp directory = " + app.getScratchWorkingDirectory());

        makeFileSystemDir(app.getStaticWorkingDirectory());
        makeFileSystemDir(app.getScratchWorkingDirectory());
        makeFileSystemDir(app.getInputDataDirectory());
        makeFileSystemDir(app.getOutputDataDirectory());
    }

    public void setupEnvironment(InvocationContext context) throws ProviderException {
        ApplicationDeploymentDescriptionType app = context.getExecutionDescription().getApp().getType();

        // input parameter
        ArrayList<String> tmp = new ArrayList<String>();
        for (Iterator<String> iterator = context.getInput().getNames(); iterator.hasNext(); ) {
            String key = iterator.next();
            tmp.add(context.getInput().getStringValue(key));
        }

        cmdList = new ArrayList<String>();

        /*
         * Builder Command
         */
        cmdList.add(app.getExecutableLocation());
        cmdList.addAll(tmp);

        // create process builder from command
        this.builder = new ProcessBuilder(cmdList);

        // get the env of the host and the application
        NameValuePairType[] env = app.getApplicationEnvironmentArray();

        if (env != null) {
            Map<String, String> nv = new HashMap<String, String>();
            for (int i = 0; i < env.length; i++) {
                String key = env[i].getName();
                String value = env[i].getValue();
                nv.put(key, value);
            }

            if ((app.getApplicationEnvironmentArray() != null) && (app.getApplicationEnvironmentArray().length != 0)
                    && nv.size() > 0) {
                builder.environment().putAll(nv);
            }
        }

        // extra env's
        builder.environment().put(GFacConstants.INPUT_DATA_DIR_VAR_NAME, app.getInputDataDirectory());
        builder.environment().put(GFacConstants.OUTPUT_DATA_DIR_VAR_NAME, app.getOutputDataDirectory());

        // working directory
        builder.directory(new File(app.getStaticWorkingDirectory()));

        // log info
        log.info("Command = " + InputUtils.buildCommand(cmdList));
        log.info("Working dir = " + builder.directory());
        for (String key : builder.environment().keySet()) {
            log.info("Env[" + key + "] = " + builder.environment().get(key));
        }
    }

    public void executeApplication(InvocationContext context) throws ProviderException {
        ApplicationDeploymentDescriptionType app = context.getExecutionDescription().getApp().getType();

        try {
            // running cmd
            Process process = builder.start();

            Thread t1 = new InputStreamToFileWriter(process.getInputStream(), app.getStandardOutput());
            Thread t2 = new InputStreamToFileWriter(process.getErrorStream(), app.getStandardError());

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
             * check return value. usually not very helpful to draw conclusions based on return values so don't bother.
             * just provide warning in the log messages
             */
            if (returnValue != 0) {
                log.error("Process finished with non zero return value. Process may have failed");
            } else {
                log.info("Process finished with return value of zero.");
            }

            StringBuffer buf = new StringBuffer();
            buf.append("Executed ").append(InputUtils.buildCommand(cmdList))
                    .append(" on the localHost, working directory = ").append(app.getStaticWorkingDirectory())
                    .append(" tempDirectory = ").append(app.getScratchWorkingDirectory()).append(" With the status ")
                    .append(String.valueOf(returnValue));

            log.info(buf.toString());

        } catch (IOException io) {
            throw new ProviderException(io.getMessage(), io);
        } catch (InterruptedException e) {
            throw new ProviderException(e.getMessage(), e);
        }
    }

    public Map<String, ?> processOutput(InvocationContext context) throws ProviderException {

        ApplicationDeploymentDescriptionType app = context.getExecutionDescription().getApp().getType();

        try {
            String stdOutStr = GfacUtils.readFileToString(app.getStandardOutput());

            // set to context
            return OutputUtils.fillOutputFromStdout(context.<ActualParameter>getOutput(), stdOutStr);
        } catch (XmlException e) {
            throw new ProviderException("Cannot read output:" + e.getMessage(), e);
        } catch (IOException io) {
            throw new ProviderException(io.getMessage(), io);
        }
    }

    @Override
    protected Map<String, ?> processInput(InvocationContext invocationContext)
            throws ProviderException {
        // TODO Auto-generated method stub
        return null;
    }
}
