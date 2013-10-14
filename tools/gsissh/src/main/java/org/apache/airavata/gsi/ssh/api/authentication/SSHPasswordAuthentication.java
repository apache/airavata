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

package org.apache.airavata.gsi.ssh.api.authentication;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 10/4/13
 * Time: 11:22 AM
 */

/**
 * Password authentication for vanilla SSH.
 */
public interface SSHPasswordAuthentication extends AuthenticationInfo {

    /**
     * Gets the password for given host name and given user name.
     * @param userName The connecting user name name.
     * @param hostName The connecting host.
     * @return Password for the given user.
     */
    String getPassword(String userName, String hostName);

}
