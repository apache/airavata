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

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 10/4/13
 * Time: 8:34 AM
 */

/**
 * This is dummy class, the keyboard interactivity is not really used when acting as an API.
 * But to get things working we have this.
 */
public class SSHAPIUIKeyboardInteractive implements UIKeyboardInteractive, UserInfo {

    private String password;

    public SSHAPIUIKeyboardInteractive(String pwd) {
        this.password = pwd;
    }

    public String[] promptKeyboardInteractive(String destination, String name,
                                              String instruction, String[] prompt, boolean[] echo) {
        return null;
    }

    public String getPassphrase() {
        return password;
    }

    public String getPassword() {
        return password;
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
