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

import org.apache.airavata.credential.store.notifier.CredentialStoreNotifier;
import org.apache.airavata.credential.store.notifier.NotificationMessage;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.commons.mail.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 12/3/13
 * Time: 4:25 PM
 */

public class EmailNotifier implements CredentialStoreNotifier {

    protected static Logger log = LoggerFactory.getLogger(EmailNotifier.class);

    private EmailNotifierConfiguration emailNotifierConfiguration;

    public EmailNotifier(EmailNotifierConfiguration notifierConfiguration) {
        this.emailNotifierConfiguration = notifierConfiguration;
    }

    public void notifyMessage(NotificationMessage message) throws CredentialStoreException {
        try {
            Email email = new SimpleEmail();
            email.setHostName(this.emailNotifierConfiguration.getEmailServer());
            email.setSmtpPort(this.emailNotifierConfiguration.getEmailServerPort());
            email.setAuthenticator(new DefaultAuthenticator(this.emailNotifierConfiguration.getEmailUserName(),
                    this.emailNotifierConfiguration.getEmailPassword()));
            email.setSSLOnConnect(this.emailNotifierConfiguration.isSslConnect());
            email.setFrom(this.emailNotifierConfiguration.getFromAddress());

            EmailNotificationMessage emailMessage = (EmailNotificationMessage)message;

            email.setSubject(emailMessage.getSubject());
            email.setMsg(emailMessage.getMessage());
            email.addTo(emailMessage.getSenderEmail());
            email.send();

        } catch (EmailException e) {
            log.error("[CredentialStore]Error sending email notification message.");
            throw new CredentialStoreException("Error sending email notification message", e);
        }


    }
}
