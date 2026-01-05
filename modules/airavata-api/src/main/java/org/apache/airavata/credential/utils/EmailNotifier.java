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

import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.CredentialStoreNotifier;
import org.apache.airavata.credential.model.EmailNotificationMessage;
import org.apache.airavata.credential.model.EmailNotifierConfiguration;
import org.apache.airavata.credential.model.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 12/3/13
 * Time: 4:25 PM
 *
 * EmailNotifier uses Spring's JavaMailSender with Jakarta Mail API.
 * Spring Boot's mail starter provides the Jakarta Mail implementation.
 */
public class EmailNotifier implements CredentialStoreNotifier {

    protected static Logger logger = LoggerFactory.getLogger(EmailNotifier.class);

    private EmailNotifierConfiguration emailNotifierConfiguration;

    public EmailNotifier(EmailNotifierConfiguration notifierConfiguration) {
        this.emailNotifierConfiguration = notifierConfiguration;
    }

    public void notifyMessage(NotificationMessage message) throws CredentialStoreException {
        try {
            JavaMailSender mailSender = createMailSender();
            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(this.emailNotifierConfiguration.getFromAddress());

            EmailNotificationMessage emailMessage = (EmailNotificationMessage) message;

            email.setSubject(emailMessage.getSubject());
            email.setText(emailMessage.getMessage());
            email.setTo(emailMessage.getSenderEmail());
            mailSender.send(email);

        } catch (Exception e) {
            var msg = String.format("Error sending email notification message: %s", e.getMessage());
            logger.error(msg, e);
            throw new CredentialStoreException(msg, e);
        }
    }

    /**
     * Creates a JavaMailSender using Spring's JavaMailSenderImpl with Jakarta Mail API.
     * Spring Boot's mail starter (spring-boot-starter-mail) provides the Jakarta Mail
     * implementation, which is automatically used by JavaMailSenderImpl.
     */
    private JavaMailSender createMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(this.emailNotifierConfiguration.getEmailServer());
        mailSender.setPort(this.emailNotifierConfiguration.getEmailServerPort());
        mailSender.setUsername(this.emailNotifierConfiguration.getEmailUserName());
        mailSender.setPassword(this.emailNotifierConfiguration.getEmailPassword());

        java.util.Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        if (this.emailNotifierConfiguration.isSslConnect()) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.starttls.enable", "true");
        }
        props.put("mail.smtp.auth", "true");

        return mailSender;
    }
}
