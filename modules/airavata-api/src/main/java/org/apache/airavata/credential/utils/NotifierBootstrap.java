/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.credential.utils;

import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.CertificateCredential;
import org.apache.airavata.credential.model.CredentialReader;
import org.apache.airavata.credential.model.CredentialStoreNotifier;
import org.apache.airavata.credential.model.EmailNotifierConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class runs a timer. Periodically it checks for expiring credentials.
 * Then if there are expiring credentials this will log a warning.
 *
 * <p>Note: Email notification has been disabled since user email is no longer stored
 * on credentials. To re-enable, integrate with the UserService to fetch email by userId.
 */
public class NotifierBootstrap extends TimerTask {

    private static boolean enabled = false;

    private static String MESSAGE = "Credentials for user {0} expires at {1}";

    private CredentialReader credentialReader;

    private long period;

    protected static Logger log = LoggerFactory.getLogger(NotifierBootstrap.class);

    private CredentialStoreNotifier credentialStoreNotifier;

    public NotifierBootstrap(long period, CredentialReader credentialReader, EmailNotifierConfiguration configuration) {
        this.period = period;

        // bootstrap
        if (enabled) {
            var timer = new Timer();
            timer.scheduleAtFixedRate(this, 0, period);
        }

        this.credentialReader = credentialReader;

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

        if (!enabled) return;

        // retrieve OA4MP credentials
        try {
            var credentials = credentialReader.getAllCredentials();

            for (var credential : credentials) {
                if (credential instanceof CertificateCredential certificateCredential) {

                    var date = Utility.convertStringToDate(certificateCredential.getNotAfter());
                    var expiryWithGap = date.toInstant().plus(1, ChronoUnit.DAYS);

                    var currentInstant = AiravataUtils.getUniqueTimestamp().toInstant();
                    if (currentInstant.isAfter(expiryWithGap)) {
                        // Log a warning about expiring credential
                        var userId = certificateCredential.getUserId();
                        var message = String.format(MESSAGE, userId, certificateCredential.getNotAfter());
                        log.warn(message);

                        // For email notifications, integrate with UserService
                        // to fetch user email by userId, then use credentialStoreNotifier
                    }
                }
            }

        } catch (CredentialStoreException e) {
            log.error("Error checking for expiring credentials.", e);
        } catch (DateTimeParseException e) {
            log.error("Error parsing date time when checking credentials", e);
        }
    }
}
