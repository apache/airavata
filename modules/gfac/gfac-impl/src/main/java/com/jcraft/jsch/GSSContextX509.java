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
package com.jcraft.jsch;


import org.globus.common.CoGProperties;
import org.globus.gsi.gssapi.auth.HostAuthorization;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class is based on GSSContextKrb5; it substitutes the globus
 * ExtendedGSSManager and uses the SecurityUtils method to get the credential if
 * one is not passed in from memory.
 *
 */
public class GSSContextX509 implements com.jcraft.jsch.GSSContext {

    private GSSContext context = null;
    private GSSCredential credential;
    private static final Logger logger = LoggerFactory.getLogger(GSSContextX509.class);

    public void create(String user, String host) throws JSchException {
        try {
//			ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager.getInstance();

            if (credential == null) {
                try {
                    credential = getCredential();
                } catch (SecurityException t) {
                    System.out.printf("Could not get proxy: %s: %s\n", t.getClass().getSimpleName(), t.getMessage());
                    throw new JSchException(t.toString());
                }
            }

            String cname = host;

            try {
                cname = InetAddress.getByName(cname).getCanonicalHostName();
            } catch (UnknownHostException e) {
            }

            GSSName name = HostAuthorization.getInstance().getExpectedName(credential, cname);

//			context = manager.createContext(name, null, credential, GSSContext.DEFAULT_LIFETIME);
//
//			// RFC4462 3.4. GSS-API Session
//			//
//			// When calling GSS_Init_sec_context(), the client MUST set
//			// integ_req_flag to "true" to request that per-message integrity
//			// protection be supported for this context. In addition,
//			// deleg_req_flag MAY be set to "true" to request access delegation,
//			// if
//			// requested by the user.
//			//
//			// Since the user authentication process by its nature authenticates
//			// only the client, the setting of mutual_req_flag is not needed for
//			// this process. This flag SHOULD be set to "false".
//
//			// TODO: OpenSSH's sshd does accept 'false' for mutual_req_flag
//			// context.requestMutualAuth(false);
//			context.requestMutualAuth(true);
//			context.requestConf(true);
//			context.requestInteg(true); // for MIC
//			context.requestCredDeleg(true);
//			context.requestAnonymity(false);

//            context = new BCGSSContextImpl(name, (GlobusGSSCredentialImpl) credential);
//            context.requestLifetime(GSSCredential.DEFAULT_LIFETIME);
//            context.requestCredDeleg(true);
//            context.requestMutualAuth(true);
//            context.requestReplayDet(true);
//            context.requestSequenceDet(true);
//            context.requestConf(false);
//            context.requestInteg(true);
//            ((ExtendedGSSContext)context).setOption(GSSConstants.DELEGATION_TYPE, GSIConstants.DELEGATION_TYPE_FULL);

            return;
        } catch (GSSException ex) {
            throw new JSchException(ex.toString());
        }
    }

    private static GSSCredential getProxy() {
        return getProxy(null, GSSCredential.DEFAULT_LIFETIME);
    }

    /**
     * @param x509_USER_PROXY
     *            path to the proxy.
     * @param credentialLifetime
     *            in seconds.
     * @return valid credential.
     *             if proxy task throws exception (or if proxy cannot be found).
     */
    private static GSSCredential getProxy(String x509_USER_PROXY, int credentialLifetime) throws SecurityException {
        if (x509_USER_PROXY == null)
            x509_USER_PROXY = System.getProperty("x509.user.proxy");

//		if (x509_USER_PROXY == null) {
//			SystemUtils.envToProperties();
//			x509_USER_PROXY = System.getProperty("x509.user.proxy");
//		}

        if (x509_USER_PROXY == null || "".equals(x509_USER_PROXY))
            x509_USER_PROXY = CoGProperties.getDefault().getProxyFile();

        if (x509_USER_PROXY == null)
            throw new SecurityException("could not get credential; no location defined");

        ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager.getInstance();

        // file...load file into a buffer
        try {
            File f = new File(x509_USER_PROXY);
            byte[] data = new byte[(int) f.length()];
            FileInputStream in = new FileInputStream(f);
            // read in the credential data
            in.read(data);
            in.close();
            return manager.createCredential(data, ExtendedGSSCredential.IMPEXP_OPAQUE, credentialLifetime, null, // use
                    // default
                    // mechanism
                    // -
                    // GSI
                    GSSCredential.INITIATE_AND_ACCEPT);
        } catch (Throwable t) {
            throw new SecurityException("could not get credential from " + x509_USER_PROXY, t);
        }
    }

    public boolean isEstablished() {
        // this must check to see if the call returned GSS_S_COMPLETE
        if (context != null){
            return context.isEstablished();
        }
        return false;
    }

    public byte[] init(byte[] token, int s, int l) throws JSchException {
        try {
            if (context != null){
                return context.initSecContext(token, s, l);
            }else {
                throw new JSchException("Context is null..");
            }
        } catch (GSSException ex) {
            throw new JSchException(ex.toString());
        }
    }

    public byte[] getMIC(byte[] message, int s, int l) {
        try {
            MessageProp prop = new MessageProp(0, false);
            return context.getMIC(message, s, l, prop);
        } catch (GSSException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    public void dispose() {
        try {
            context.dispose();
        } catch (GSSException ex) {
        }
    }

    public void setCredential(GSSCredential credential) {
        this.credential = credential;
    }

    public GSSCredential getCredential() {
        return credential;
    }
}

