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

import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class LocalDirectorySetupHandler implements GFacHandler{
    private static final Logger log = LoggerFactory.getLogger(LocalDirectorySetupHandler.class);

    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        log.info("Invoking GramDirectorySetupHandler ...");
        HostDescriptionType type = jobExecutionContext.getApplicationContext().getHostDescription().getType();
        ApplicationDescription applicationDeploymentDescription = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription();
        ApplicationDeploymentDescriptionType app = applicationDeploymentDescription.getType();
        log.debug("working diectroy = " + app.getStaticWorkingDirectory());
        log.debug("temp directory = " + app.getScratchWorkingDirectory());

        makeFileSystemDir(app.getStaticWorkingDirectory(),jobExecutionContext);
        makeFileSystemDir(app.getScratchWorkingDirectory(),jobExecutionContext);
        makeFileSystemDir(app.getInputDataDirectory(),jobExecutionContext);
        makeFileSystemDir(app.getOutputDataDirectory(),jobExecutionContext);
    }
    private void makeFileSystemDir(String dir, JobExecutionContext jobExecutionContext) throws GFacHandlerException {
           File f = new File(dir);
           if (f.isDirectory() && f.exists()) {
               return;
           } else if (!new File(dir).mkdir()) {
               throw new GFacHandlerException("Cannot make directory "+dir);
           }
    }

    public void initProperties(Map<String, String> properties) throws GFacHandlerException, GFacException {

    }
}
