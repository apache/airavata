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

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 9/20/13
 * Time: 12:15 PM
 */

import org.apache.airavata.gsi.ssh.api.authentication.GSIAuthenticationInfo;
import org.apache.airavata.gsi.ssh.api.authentication.SSHPasswordAuthentication;
import org.ietf.jgss.*;

/**
 * An authenticator used for raw SSH sessions. Gives SSH user name, password
 * directly.
 * This is only an example implementation.
 */
public class DefaultPasswordAuthenticationInfo implements SSHPasswordAuthentication {

    private String password;

    public DefaultPasswordAuthenticationInfo(String pwd) {
        this.password = pwd;
    }

    public String getPassword(String userName, String hostName) {
        return password;
    }
}
