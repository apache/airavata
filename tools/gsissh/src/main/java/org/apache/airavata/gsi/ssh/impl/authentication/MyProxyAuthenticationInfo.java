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

package org.apache.airavata.gsi.ssh.impl.authentication;

import org.apache.airavata.gsi.ssh.api.authentication.GSIAuthenticationInfo;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 8/14/13
 * Time: 5:22 PM
 */

public class MyProxyAuthenticationInfo extends GSIAuthenticationInfo {

    public static final String X509_CERT_DIR = "X509_CERT_DIR";
    private String userName;
    private String password;
    private String myProxyUrl;
    private int myProxyPort;
    private int lifeTime;

    public MyProxyAuthenticationInfo(String userName, String password, String myProxyUrl, int myProxyPort,
                                     int life, String certificatePath) {
        this.userName = userName;
        this.password = password;
        this.myProxyUrl = myProxyUrl;
        this.myProxyPort = myProxyPort;
        this.lifeTime = life;
        properties.setProperty(X509_CERT_DIR, certificatePath);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMyProxyUrl() {
        return myProxyUrl;
    }

    public void setMyProxyUrl(String myProxyUrl) {
        this.myProxyUrl = myProxyUrl;
    }

    public int getMyProxyPort() {
        return myProxyPort;
    }

    public void setMyProxyPort(int myProxyPort) {
        this.myProxyPort = myProxyPort;
    }

    public int getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(int lifeTime) {
        this.lifeTime = lifeTime;
    }

    public GSSCredential getCredentials() throws SecurityException {
        return getMyProxyCredentials();
    }

    private GSSCredential getMyProxyCredentials() throws SecurityException {
        MyProxy myproxy = new MyProxy(this.myProxyUrl, this.myProxyPort);
        try {
            return myproxy.get(this.getUserName(), this.password, this.lifeTime);
        } catch (MyProxyException e) {
            throw new SecurityException("Error getting proxy credentials", e);
        }
    }


}
