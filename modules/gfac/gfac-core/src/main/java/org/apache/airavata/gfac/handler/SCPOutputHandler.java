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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.transport.TransportException;

import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.ToolsException;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.context.security.GSISecurityContext;
import org.apache.airavata.gfac.context.security.SSHSecurityContext;
import org.apache.airavata.gfac.external.GridFtp;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.apache.airavata.gfac.utils.GFacUtils;
import org.apache.airavata.gfac.utils.OutputUtils;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.URIArrayType;
import org.apache.airavata.schemas.gfac.URIParameterType;
import org.apache.xmlbeans.XmlException;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SCPOutputHandler implements GFacHandler{
    private static final Logger log = LoggerFactory.getLogger(SCPOutputHandler.class);

    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {

        ApplicationDeploymentDescriptionType app = jobExecutionContext.getApplicationContext()
                .getApplicationDeploymentDescription().getType();
        try {
            Cluster cluster = null;
            if (jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT) != null) {
                cluster = ((GSISecurityContext) jobExecutionContext.getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT)).getPbsCluster();
            } else {
                cluster = ((SSHSecurityContext) jobExecutionContext.getSecurityContext(SSHSecurityContext.SSH_SECURITY_CONTEXT)).getPbsCluster();
            }
            if (cluster == null) {
                throw new GFacProviderException("Security context is not set properly");
            } else {
                log.info("Successfully retrieved the Security Context");
            }

            // Get the Stdouts and StdErrs
            String timeStampedServiceName = GFacUtils.createUniqueNameForService(jobExecutionContext.getServiceName());
            File localStdOutFile = File.createTempFile(timeStampedServiceName, "stdout");
            File localStdErrFile = File.createTempFile(timeStampedServiceName, "stderr");

            log.info("Downloading file : " + app.getStandardError() + " to : " + localStdErrFile.getAbsolutePath());
            cluster.scpFrom(app.getStandardOutput(), localStdOutFile.getAbsolutePath());
            log.info("Downloading file : " + app.getStandardOutput() + " to : " + localStdOutFile.getAbsolutePath());
            cluster.scpFrom(app.getStandardError(), localStdErrFile.getAbsolutePath());

            String stdOutStr = GFacUtils.readFileToString(localStdOutFile.getAbsolutePath());
            String stdErrStr = GFacUtils.readFileToString(localStdErrFile.getAbsolutePath());

            Map<String, ActualParameter> stringMap = new HashMap<String, ActualParameter>();
            Map<String, Object> output = jobExecutionContext.getOutMessageContext().getParameters();
            stringMap = OutputUtils.fillOutputFromStdout(output, stdOutStr, stdErrStr);
            if (stringMap == null || stringMap.isEmpty()) {
                throw new GFacHandlerException(
                        "Empty Output returned from the Application, Double check the application"
                                + "and ApplicationDescriptor output Parameter Names");
            }
        } catch (XmlException e) {
            throw new GFacHandlerException("Cannot read output:" + e.getMessage(), e);
        } catch (ConnectionException e) {
            throw new GFacHandlerException(e.getMessage(), e);
        } catch (TransportException e) {
            throw new GFacHandlerException(e.getMessage(), e);
        } catch (IOException e) {
            throw new GFacHandlerException(e.getMessage(), e);
        } catch (Exception e) {
            throw new GFacHandlerException("Error in retrieving results", e);
        }

    }

    public void initProperties(Map<String, String> properties) throws GFacHandlerException, GFacException {

    }
}
