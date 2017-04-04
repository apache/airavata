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
package org.apache.airavata.credential.store.notifier.impl;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ApplicationSettings;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 12/3/13
 * Time: 5:06 PM
 */

public class EmailNotifierConfiguration {
    private String emailServer;
    private int emailServerPort;
    private String emailUserName;
    private String emailPassword;
    private boolean sslConnect;
    private String fromAddress;

    public EmailNotifierConfiguration(String emailServer, int emailServerPort, String emailUserName,
                                      String emailPassword, boolean sslConnect, String fromAddress) {
        this.emailServer = emailServer;
        this.emailServerPort = emailServerPort;
        this.emailUserName = emailUserName;
        this.emailPassword = emailPassword;
        this.sslConnect = sslConnect;
        this.fromAddress = fromAddress;
    }

    public String getEmailServer() {
        return emailServer;
    }

    public int getEmailServerPort() {
        return emailServerPort;
    }

    public String getEmailUserName() {
        return emailUserName;
    }

    public String getEmailPassword() {
        return emailPassword;
    }

    public boolean isSslConnect() {
        return sslConnect;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public static EmailNotifierConfiguration getEmailNotifierConfigurations() throws ApplicationSettingsException {
        return new EmailNotifierConfiguration(ApplicationSettings.getCredentialStoreEmailServer(),
                Integer.parseInt(ApplicationSettings.getCredentialStoreEmailServerPort()),
                ApplicationSettings.getCredentialStoreEmailUser(),
                ApplicationSettings.getCredentialStoreEmailPassword(),
                Boolean.parseBoolean(ApplicationSettings.getCredentialStoreEmailSSLConnect()),
                ApplicationSettings.getCredentialStoreEmailFromEmail());
    }

}
