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

package org.apache.airavata.core.gfac.extension.pre;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.context.message.MessageContext;
import org.apache.airavata.core.gfac.context.security.impl.GSISecurityContext;
import org.apache.airavata.core.gfac.exception.ExtensionException;
import org.apache.airavata.core.gfac.exception.SecurityException;
import org.apache.airavata.core.gfac.exception.ToolsException;
import org.apache.airavata.core.gfac.extension.PreExecuteChain;
import org.apache.airavata.core.gfac.external.GridFtp;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.DataType;
import org.apache.airavata.schemas.gfac.FileParameterType;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Input plugin to transfer file from Http location to target GridFTP host
 */
public class HttpInputStaging extends PreExecuteChain {

    public static final Logger log = LoggerFactory.getLogger(HttpInputStaging.class);

    public static final String MYPROXY_SECURITY_CONTEXT = "myproxy";

    public boolean execute(InvocationContext context) throws ExtensionException {

        try {
            MessageContext<ActualParameter> inputContext = context.getInput();

            if (inputContext != null) {

                for (Iterator<String> iterator = inputContext.getNames(); iterator.hasNext();) {
                    String key = iterator.next();
                    if (inputContext.getValue(key).hasType(DataType.FILE)) {
                        FileParameterType fileParameter = (FileParameterType) inputContext.getValue(key).getType();

                        /*
                         * Determine scheme
                         */
                        URI uri = URI.create(fileParameter.getValue());
                        if (uri.getScheme().equalsIgnoreCase("http")) {
                            /*
                             * Desctination complete URI
                             */
                            File file = new File(uri.getPath());
                            String destFilePath = file.getName();

                            ApplicationDeploymentDescriptionType app = context.getExecutionDescription().getApp().getType();
                            destFilePath = app.getInputDataDirectory() + File.separator + destFilePath;

                            HostDescriptionType hostDescription = context.getExecutionDescription().getHost().getType();
                            if (hostDescription instanceof GlobusHostType) {
                                uploadToGridFTPFromHttp(context, uri, destFilePath);
                            } else {
                                downloadFile(context, uri, destFilePath);
                            }

                            /*
                             * Replace parameter
                             */
                            fileParameter.setValue(destFilePath);
                        }
                    }
                }
            } else {
                log.debug("Input Context is null");
            }
        } catch (SecurityException e) {
            throw new ExtensionException(e.getMessage(), e);
        } catch (ToolsException e) {
            throw new ExtensionException(e.getMessage(), e);
        } catch (URISyntaxException e) {
            throw new ExtensionException("URI is in the wrong format:" + e.getMessage(), e);
        } catch (IOException e) {
            throw new ExtensionException("Cannot load from HTTP", e);
        }
        return false;
    }

    private void uploadToGridFTPFromHttp(InvocationContext context, URI src, String remoteLocation)
            throws SecurityException, ToolsException, URISyntaxException, IOException {
        GridFtp ftp = new GridFtp();
        GSSCredential gssCred = ((GSISecurityContext) context.getSecurityContext(MYPROXY_SECURITY_CONTEXT))
                .getGssCredentails();

        GlobusHostType host = (GlobusHostType) context.getExecutionDescription().getHost().getType();

        for (String endpoint : host.getGridFTPEndPointArray()) {
            try {
                URI destURI = GfacUtils.createGsiftpURI(endpoint, remoteLocation);
                InputStream in = null;
                try {
                    in = src.toURL().openStream();
                    ftp.uploadFile(destURI, gssCred, in);
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (Exception e) {
                        log.warn("Cannot close URL inputstream");
                    }
                }
                return;
            } catch (ToolsException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void downloadFile(InvocationContext context, URI src, String localFile) throws IOException {
        OutputStream out = null;
        InputStream in = null;
        try {
            /*
             * Open an output stream to the destination file on our local filesystem
             */
            in = src.toURL().openStream();
            out = new BufferedOutputStream(new FileOutputStream(localFile));

            // Get the data
            byte[] buffer = new byte[1024];
            int numRead;
            while ((numRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
            }

            out.flush();

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    log.warn("Cannot close URL inputstream");
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ioe) {
                    log.warn("Cannot close URL outputstream");
                }
            }

        }
    }
}
