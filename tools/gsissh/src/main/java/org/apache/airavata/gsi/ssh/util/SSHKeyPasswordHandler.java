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

package org.apache.airavata.gsi.ssh.util;

import com.jcraft.jsch.UserInfo;
import org.apache.airavata.gsi.ssh.api.authentication.SSHKeyAuthentication;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 10/4/13
 * Time: 2:22 PM
 */

/**
 * This class is used to get the pass phrase to decrypt public/private keys.
 */
public class SSHKeyPasswordHandler implements UserInfo {

    private SSHKeyAuthentication keyAuthenticationHandler;

    public SSHKeyPasswordHandler(SSHKeyAuthentication handler) {
        this.keyAuthenticationHandler = handler;
    }

    public String getPassphrase() {
        return keyAuthenticationHandler.getPassPhrase();
    }

    public String getPassword() {
        throw new NotImplementedException();
    }

    public boolean promptPassword(String message) {
        return false;
    }

    public boolean promptPassphrase(String message) {
        return true;
    }

    public boolean promptYesNo(String message) {
        return false;
    }

    public void showMessage(String message) {
        keyAuthenticationHandler.bannerMessage(message);
    }
}
