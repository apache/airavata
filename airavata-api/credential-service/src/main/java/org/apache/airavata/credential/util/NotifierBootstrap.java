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
package org.apache.airavata.credential.util;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 12/27/13
 * Time: 2:22 PM
 */
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.airavata.credential.repository.CredentialRepository;
import org.apache.airavata.credential.repository.CredentialStoreException;
import org.apache.airavata.model.credential.store.proto.CommunityUser;
import org.apache.airavata.model.credential.store.proto.StoredCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class runs a timer. Periodically it checks for expiring credentials.
 * Then if there are expiring credentials this will send an email.
 */
public class NotifierBootstrap extends TimerTask {

    private static boolean enabled = false;

    private static String MESSAGE = "Credentials for community user {0} expires at {1}";
    private static String SUBJECT = "Expiring credentials for user {0}";

    private CredentialRepository credentialRepository;
    private CredentialEncryptionUtil encryptionUtil;

    private long period;

    protected static Logger log = LoggerFactory.getLogger(NotifierBootstrap.class);

    private CredentialStoreNotifier credentialStoreNotifier;

    public NotifierBootstrap(
            long period,
            CredentialRepository credentialRepository,
            CredentialEncryptionUtil encryptionUtil,
            EmailNotifierConfiguration configuration) {
        this.period = period;
        this.credentialRepository = credentialRepository;
        this.encryptionUtil = encryptionUtil;

        // bootstrap
        if (enabled) {
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(this, 0, period);
        }

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
            List<StoredCredential> credentials = credentialRepository.findAll().stream()
                    .map(entity -> {
                        try {
                            StoredCredential stored =
                                    encryptionUtil.convertByteArrayToCredential(entity.getCredential());
                            long persistedTime = entity.getTimePersisted() != null
                                    ? entity.getTimePersisted().getTime()
                                    : 0;
                            return CredentialEncryptionUtil.overlayDbFields(
                                    stored,
                                    entity.getPortalUserId(),
                                    persistedTime,
                                    entity.getDescription(),
                                    entity.getTokenId());
                        } catch (CredentialStoreException e) {
                            throw new RuntimeException(
                                    "Error deserializing credential for token " + entity.getTokenId(), e);
                        }
                    })
                    .collect(Collectors.toList());

            for (StoredCredential credential : credentials) {
                if (credential.getCredentialCase() == StoredCredential.CredentialCase.CERTIFICATE_CREDENTIAL) {
                    var certificateCredential = credential.getCertificateCredential();

                    Date date = Utility.convertStringToDate(certificateCredential.getNotAfter());
                    date.setDate(date.getDate() + 1); // gap is 1 days

                    Date currentDate = new Date();
                    if (currentDate.after(date)) {
                        // Send an email
                        CommunityUser communityUser = certificateCredential.getCommunityUser();
                        String body = String.format(
                                MESSAGE, communityUser.getUsername(), certificateCredential.getNotAfter());
                        String subject = String.format(SUBJECT, communityUser.getUsername());
                        NotificationMessage notificationMessage =
                                new EmailNotificationMessage(subject, communityUser.getUserEmail(), body);

                        this.credentialStoreNotifier.notifyMessage(notificationMessage);
                    }
                }
            }

        } catch (CredentialStoreException e) {
            log.error("Error sending emails about credential expiring.", e);
        } catch (ParseException e) {
            log.error("Error parsing date time when sending emails", e);
        }
    }
}
