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

package org.apache.airavata.xbaya.security;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.gpel.client.security.GpelUserX509Credential;
import org.ietf.jgss.GSSCredential;

/**
 * This will wrap GpelUserX509Credential class. I wanted to get access to GSSCredential from GpelUserX509Credential. But
 * this was not exposed within GpelUserX509Credential. GSSCredential needs to be passed in to WorkflowProxyClient as it
 * will be used when talking to XRegsitry.
 * 
 */
public class UserX509Credential extends GpelUserX509Credential {
    private GSSCredential gssCredential;

    public UserX509Credential(GSSCredential gssCredential, X509Certificate[] x509Certificates) {
        super(gssCredential, x509Certificates);
        this.gssCredential = gssCredential;
    }

    public UserX509Credential(String s, String s1) {
        super(s, s1);
    }

    public UserX509Credential(X509Certificate[] x509Certificates, PrivateKey privateKey,
            X509Certificate[] x509Certificates1) {
        super(x509Certificates, privateKey, x509Certificates1);
    }

    public GSSCredential getGssCredential() {
        return gssCredential;
    }
}
