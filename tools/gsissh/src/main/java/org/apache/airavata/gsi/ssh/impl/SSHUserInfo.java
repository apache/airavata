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

package org.apache.airavata.gsi.ssh.impl;

import com.jcraft.jsch.UserInfo;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 9/20/13
 * Time: 2:31 PM
 */

public class SSHUserInfo implements UserInfo {

    private String password;

    public SSHUserInfo(String pwd) {
        this.password = pwd;
    }

    public String getPassphrase() {
        return this.password;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean promptPassword(String message) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean promptPassphrase(String message) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean promptYesNo(String message) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void showMessage(String message) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
