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

package org.apache.airavata.core.gfac.extension.post;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Iterator;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.DataType;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.app.ShellApplicationDeployment;
import org.apache.airavata.commons.gfac.type.host.GlobusHost;
import org.apache.airavata.commons.gfac.type.parameter.AbstractParameter;
import org.apache.airavata.commons.gfac.type.parameter.FileParameter;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.context.message.MessageContext;
import org.apache.airavata.core.gfac.context.security.impl.GSISecurityContext;
import org.apache.airavata.core.gfac.exception.ExtensionException;
import org.apache.airavata.core.gfac.exception.SecurityException;
import org.apache.airavata.core.gfac.exception.ToolsException;
import org.apache.airavata.core.gfac.extension.PostExecuteChain;
import org.apache.airavata.core.gfac.external.GridFtp;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Output plugin to transfer file from GridFTP host to location given in output parameter
 */
public class GridFtpOutputStaging extends PostExecuteChain {
    
    public static final Logger log = LoggerFactory.getLogger(GridFtpOutputStaging.class);

    public static final String MYPROXY_SECURITY_CONTEXT = "myproxy";        

    public boolean execute(InvocationContext context) throws ExtensionException {
        try {
            MessageContext<AbstractParameter> outputContext = context.getOutput();

            if (outputContext != null) {

                for (Iterator<String> iterator = outputContext.getNames(); iterator.hasNext();) {
                    String key = iterator.next();
                    if (outputContext.getValue(key).getType() == DataType.File) {
                        FileParameter fileParameter = (FileParameter) outputContext.getValue(key);

                        /*
                         * Determine scheme
                         */
                        URI uri = URI.create(fileParameter.toStringVal());
                        if (uri.getScheme().equalsIgnoreCase(GridFtp.GSIFTP_SCHEME)) {
                            HostDescription hostDescription = context.getExecutionDescription().getHost();

                            /*
                             * src complete URI
                             */
                            File file = new File(uri.getPath());
                            String srcFilePath = file.getName();

                            ApplicationDeploymentDescription app = context.getExecutionDescription().getApp();
                            if (app instanceof ShellApplicationDeployment) {
                                srcFilePath = app.getOutputDir() + File.separator + srcFilePath;
                            }

                            if (hostDescription instanceof GlobusHost) {
                                gridFTPTransfer(context, uri, srcFilePath);
                            } else if (GfacUtils.isLocalHost(hostDescription.getName())) {
                                updateFile(context, uri, srcFilePath);
                            }
                        }
                    }
                }
            }else{
                log.debug("Output Context is null");
            }
        } catch (UnknownHostException e) {
            throw new ExtensionException("Cannot find IP Address for current host", e);
        } catch (URISyntaxException e) {
            throw new ExtensionException("URI is in the wrong format:" + e.getMessage(), e);
        } catch (ToolsException e) {
            throw new ExtensionException(e.getMessage(), e);
        } catch (SecurityException e) {
            throw new ExtensionException(e.getMessage(), e);
        }
        return false;
    }

    private void gridFTPTransfer(InvocationContext context, URI dest, String remoteSrcFile) throws SecurityException,
            ToolsException, URISyntaxException {
        GridFtp ftp = new GridFtp();
        GSSCredential gssCred = ((GSISecurityContext) context.getSecurityContext(MYPROXY_SECURITY_CONTEXT))
                .getGssCredentails();
        GlobusHost host = (GlobusHost) context.getExecutionDescription().getHost();
        URI srcURI = GfacUtils.createGsiftpURI(host.getGridFTPEndPoint(), remoteSrcFile);
        ftp.transfer(srcURI, dest, gssCred, true);
    }

    private void updateFile(InvocationContext context, URI dest, String localFile) throws SecurityException,
            ToolsException {
        GridFtp ftp = new GridFtp();
        GSSCredential gssCred = ((GSISecurityContext) context.getSecurityContext(MYPROXY_SECURITY_CONTEXT))
                .getGssCredentails();
        ftp.updateFile(dest, gssCred, new File(localFile));
    }
}
