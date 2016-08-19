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
package org.apache.airavata.gfac.core.authentication;

import org.ietf.jgss.GSSCredential;

import java.util.Properties;

/**
 * Authentication data. Could be MyProxy user name, password, could be GSSCredentials
 * or could be SSH keys.
 */
public abstract class GSIAuthenticationInfo implements AuthenticationInfo {

    public Properties properties = new Properties();

    public abstract GSSCredential getCredentials() throws SecurityException;

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties propertiesIn) {
        properties = propertiesIn;
    }
}
