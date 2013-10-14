package org.apache.airavata.gsi.ssh.api.authentication;/*
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

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 10/4/13
 * Time: 2:39 PM
 */

/**
 * Abstracts out common methods for SSH key authentication.
 */
public interface SSHKeyAuthentication extends AuthenticationInfo {

    /**
     * This is needed only if private key and public keys are encrypted.
     * If they are not encrypted we can just return null.
     * @return User should return pass phrase if keys are encrypted. If not null.
     */
    String getPassPhrase();

    /**
     * Callback with the banner message. API user can get hold of banner message
     * by implementing this method.
     * @param message The banner message.
     */
    void bannerMessage(String message);
}
