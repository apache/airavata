/**
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
 */
package org.apache.airavata.credential.store.servlet;

import edu.uiuc.ncsa.myproxy.oa4mp.client.loader.ClientBootstrapper;
import edu.uiuc.ncsa.security.core.util.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;

/**
 * Bootstrapper class for credential-store.
 */
public class CredentialBootstrapper extends ClientBootstrapper {

    protected static Logger log = LoggerFactory.getLogger(CredentialBootstrapper.class);

    public ConfigurationLoader getConfigurationLoader(ServletContext servletContext) throws Exception {

        File currentDirectory = new File(".");

        log.info("Current directory is - " + currentDirectory.getAbsolutePath());

        return super.getConfigurationLoader(servletContext);

    }

}
