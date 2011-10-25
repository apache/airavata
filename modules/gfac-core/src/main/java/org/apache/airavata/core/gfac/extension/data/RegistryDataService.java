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

package org.apache.airavata.core.gfac.extension.data;

import java.io.File;
import java.util.Date;
import java.util.UUID;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.exception.ExtensionException;
import org.apache.airavata.core.gfac.extension.DataServiceChain;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.ShellApplicationDeploymentType;

/**
 * This plugin fills out all information that is missing in {@link ApplicationDeploymentDescription} based on Unix
 * system. For instance, it will use "/tmp" as default temporary directory.
 * 
 */
public class RegistryDataService extends DataServiceChain {

    public boolean execute(InvocationContext context) throws ExtensionException {

        ServiceDescription serviceDesc = context.getExecutionDescription().getService();
        HostDescription hostDesc = context.getExecutionDescription().getHost();
        ApplicationDeploymentDescriptionType appDesc = context.getExecutionDescription().getApp().getType();
        if (serviceDesc != null && hostDesc != null && appDesc != null) {
            /*
             * if there is no setting in deployment description, use from host
             */
            if (appDesc.getTmpDir() == null) {
                appDesc.setTmpDir("/tmp");
            }

            /*
             * Working dir
             */
            if (appDesc.getWorkingDir() == null) {
                String date = new Date().toString();
                date = date.replaceAll(" ", "_");
                date = date.replaceAll(":", "_");

                String tmpDir = appDesc.getTmpDir() + File.separator + appDesc.getName() + "_" + date + "_"
                        + UUID.randomUUID();

                appDesc.setWorkingDir(tmpDir);
            }

            /*
             * Input and Output Directory
             */
            if (appDesc.getInputDir() == null) {
                appDesc.setInputDir(appDesc.getWorkingDir() + File.separator + "inputData");
            }
            if (appDesc.getOutputDir() == null) {
                appDesc.setOutputDir(appDesc.getWorkingDir() + File.separator + "outputData");
            }

            /*
             * Stdout and Stderr for Shell
             */
            if (appDesc.getClass().isAssignableFrom(ShellApplicationDeploymentType.class)) {
            	ShellApplicationDeploymentType shell = (ShellApplicationDeploymentType) appDesc;
                if (shell.getStdOut() == null) {
                    shell.setStdOut(appDesc.getWorkingDir() + File.separator + appDesc.getName() + ".stdout");
                }
                if (shell.getStdErr() == null) {
                    shell.setStdErr(appDesc.getWorkingDir() + File.separator + appDesc.getName() + ".stderr");
                }
            }

        } else {
            throw new ExtensionException("Service Map for " + context.getServiceName()
                    + " does not found on resource Catalog " + context.getExecutionContext().getRegistryService());
        }

        return false;
    }
}
