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
import java.util.Iterator;

import org.apache.airavata.core.gfac.context.InvocationContext;
import org.apache.airavata.core.gfac.context.MessageContext;
import org.apache.airavata.core.gfac.context.impl.GSISecurityContext;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.exception.GfacException.FaultCode;
import org.apache.airavata.core.gfac.extension.PostExecuteChain;
import org.apache.airavata.core.gfac.external.GridFtp;
import org.apache.airavata.core.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.core.gfac.type.DataType;
import org.apache.airavata.core.gfac.type.HostDescription;
import org.apache.airavata.core.gfac.type.app.ShellApplicationDeployment;
import org.apache.airavata.core.gfac.type.host.GlobusHost;
import org.apache.airavata.core.gfac.type.parameter.AbstractParameter;
import org.apache.airavata.core.gfac.type.parameter.FileParameter;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.ietf.jgss.GSSCredential;

public class GridFtpOutputStaging extends PostExecuteChain {

    public static final String MYPROXY_SECURITY_CONTEXT = "myproxy";

    public boolean execute(InvocationContext context) throws GfacException {
        try {
            MessageContext<AbstractParameter> x = context.getMessageContext("output");

            for (Iterator<String> iterator = x.getParameterNames(); iterator.hasNext();) {
                String key = iterator.next();
                if (x.getParameterValue(key).getType() == DataType.File) {
                    FileParameter fileParameter = (FileParameter) x.getParameterValue(key);

                    /*
                     * Determine scheme
                     */
                    URI uri = URI.create(fileParameter.toStringVal());
                    if (uri.getScheme().equalsIgnoreCase(GridFtp.GSIFTP_SCHEME)) {
                        HostDescription hostDescription = context.getGfacContext().getHost();

                        /*
                         * src complete URI
                         */
                        File file = new File(uri.getPath());
                        String srcFilePath = file.getName();

                        ApplicationDeploymentDescription app = context.getGfacContext().getApp();
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

        } catch (Exception e) {
            throw new GfacException(e, FaultCode.Unknown);
        }
        return false;
    }

    private void gridFTPTransfer(InvocationContext context, URI dest, String remoteSrcFile) throws Exception {
        GridFtp ftp = new GridFtp();
        GSSCredential gssCred = ((GSISecurityContext) context.getSecurityContext(MYPROXY_SECURITY_CONTEXT))
                .getGssCredentails();
        GlobusHost host = (GlobusHost) context.getGfacContext().getHost();
        URI srcURI = GfacUtils.createGsiftpURI(host.getGridFTPEndPoint(), remoteSrcFile);
        ftp.transfer(srcURI, dest, gssCred, true);
    }

    private void updateFile(InvocationContext context, URI dest, String localFile) throws Exception {
        GridFtp ftp = new GridFtp();
        GSSCredential gssCred = ((GSISecurityContext) context.getSecurityContext(MYPROXY_SECURITY_CONTEXT))
                .getGssCredentails();
        ftp.updateFile(dest, gssCred, new File(localFile));
    }
}
