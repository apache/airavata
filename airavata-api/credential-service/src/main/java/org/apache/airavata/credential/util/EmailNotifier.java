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

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.airavata.credential.repository.CredentialStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Sends email notifications using Spring's JavaMailSender.
 */
public class EmailNotifier implements CredentialStoreNotifier {

    private static final Logger log = LoggerFactory.getLogger(EmailNotifier.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailNotifier(EmailNotifierConfiguration config) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getEmailServer());
        sender.setPort(config.getEmailServerPort());
        sender.setUsername(config.getEmailUserName());
        sender.setPassword(config.getEmailPassword());
        if (config.isSslConnect()) {
            sender.getJavaMailProperties().put("mail.smtp.ssl.enable", "true");
        }
        sender.getJavaMailProperties().put("mail.smtp.auth", "true");
        this.mailSender = sender;
        this.fromAddress = config.getFromAddress();
    }

    /** Package-private constructor for testing with a mock/stub JavaMailSender. */
    EmailNotifier(JavaMailSender mailSender, String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void notifyMessage(NotificationMessage message) throws CredentialStoreException {
        try {
            EmailNotificationMessage emailMessage = (EmailNotificationMessage) message;
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false);
            helper.setFrom(fromAddress);
            helper.setTo(emailMessage.getSenderEmail());
            helper.setSubject(emailMessage.getSubject());
            helper.setText(emailMessage.getMessage());
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("[CredentialStore] Error sending email notification message.");
            throw new CredentialStoreException("Error sending email notification message", e);
        }
    }
}
