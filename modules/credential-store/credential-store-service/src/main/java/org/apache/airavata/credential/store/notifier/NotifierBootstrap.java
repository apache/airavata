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
package org.apache.airavata.credential.store.notifier;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 12/27/13
 * Time: 2:22 PM
 */

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.credential.store.credential.CommunityUser;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential;
import org.apache.airavata.credential.store.notifier.impl.EmailNotificationMessage;
import org.apache.airavata.credential.store.notifier.impl.EmailNotifier;
import org.apache.airavata.credential.store.notifier.impl.EmailNotifierConfiguration;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.apache.airavata.credential.store.store.impl.CredentialReaderImpl;
import org.apache.airavata.credential.store.util.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.*;

/**
 * This class runs a timer. Periodically it checks for expiring credentials.
 * Then if there are expiring credentials this will send an email.
 */
public class NotifierBootstrap extends TimerTask {

    private static boolean enabled = false;

    private static String MESSAGE = "Credentials for community user {0} expires at {1}";
    private static String SUBJECT = "Expiring credentials for user {0}";

    private DBUtil dbUtil;

    private long period;

    protected static Logger log = LoggerFactory.getLogger(NotifierBootstrap.class);


    private CredentialStoreNotifier credentialStoreNotifier;

    public NotifierBootstrap(long period, DBUtil db, EmailNotifierConfiguration configuration) {
        this.period = period;

        // bootstrap
        if (enabled) {
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(this, 0, period);
        }

        this.dbUtil = db;

        this.credentialStoreNotifier = new EmailNotifier(configuration);
    }



    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        NotifierBootstrap.enabled = enabled;
    }

    @Override
    public void run() {

        if (!enabled)
            return;

        // retrieve OA4MP credentials
        try {
            CredentialReader credentialReader = new CredentialReaderImpl(this.dbUtil);
            List<Credential> credentials = credentialReader.getAllCredentials();

            for(Credential credential : credentials) {
                if (credential instanceof CertificateCredential) {
                    CertificateCredential certificateCredential = (CertificateCredential)credential;

                    Date date = Utility.convertStringToDate(certificateCredential.getNotAfter());
                    date.setDate(date.getDate() + 1);    // gap is 1 days

                    Date currentDate = new Date();
                    if (currentDate.after(date)) {
                        // Send an email
                        CommunityUser communityUser = certificateCredential.getCommunityUser();
                        String body =
                                String.format(MESSAGE, communityUser.getUserName(), certificateCredential.getNotAfter());
                        String subject = String.format(SUBJECT, communityUser.getUserName());
                        NotificationMessage notificationMessage
                                = new EmailNotificationMessage(subject, communityUser.getUserEmail(), body);

                        this.credentialStoreNotifier.notifyMessage(notificationMessage);

                    }
                }
            }

        } catch (ApplicationSettingsException e) {
            log.error("Error configuring email senders.", e);
        } catch (CredentialStoreException e) {
            log.error("Error sending emails about credential expiring.", e);
        } catch (ParseException e) {
            log.error("Error parsing date time when sending emails", e);
        }

    }
}
