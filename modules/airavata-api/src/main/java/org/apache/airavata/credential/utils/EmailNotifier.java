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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * EmailNotifier uses Spring Boot's auto-configured JavaMailSender.
 *
 * Configure via application.properties (Spring Mail properties):
 *   spring.mail.host=smtp.example.com
 *   spring.mail.port=587
 *   spring.mail.username=user
 *   spring.mail.password=secret
 *   spring.mail.properties.mail.smtp.auth=true
 *   spring.mail.properties.mail.smtp.starttls.enable=true
 */
@Component
@ConditionalOnBean(JavaMailSender.class)
public class EmailNotifier implements CredentialStoreNotifier {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotifier.class);

    private final JavaMailSender mailSender;
    private EmailNotifierConfiguration emailNotifierConfiguration;

    /**
     * Constructor with Spring-injected JavaMailSender.
     * The mailSender is auto-configured by Spring Boot based on spring.mail.* properties.
     */
    @Autowired
    public EmailNotifier(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Legacy constructor for backward compatibility.
     */
    public EmailNotifier(EmailNotifierConfiguration notifierConfiguration) {
        this.emailNotifierConfiguration = notifierConfiguration;
        this.mailSender = null; // Will be set separately or use factory
    }

    /**
     * Set the email notifier configuration (for legacy usage).
     */
    public void setEmailNotifierConfiguration(EmailNotifierConfiguration configuration) {
        this.emailNotifierConfiguration = configuration;
    }

    @Override
    public void notifyMessage(NotificationMessage message) throws CredentialStoreException {
        try {
            var email = new SimpleMailMessage();

            if (emailNotifierConfiguration != null) {
                email.setFrom(emailNotifierConfiguration.getFromAddress());
            }
            email.setText(message.getMessage());

            if (mailSender != null) {
                mailSender.send(email);
                logger.info("Notification message sent");
            } else {
                throw new CredentialStoreException("JavaMailSender not configured");
            }

        } catch (Exception e) {
            var msg = String.format("Error sending notification message: %s", e.getMessage());
            logger.error(msg, e);
            throw new CredentialStoreException(msg, e);
        }
    }

    @Override
    public void notifyEmail(EmailNotificationMessage emailMessage) throws CredentialStoreException {
        try {
            var email = new SimpleMailMessage();

            if (emailNotifierConfiguration != null) {
                email.setFrom(emailNotifierConfiguration.getFromAddress());
            }

            email.setSubject(emailMessage.getSubject());
            email.setText(emailMessage.getMessage());
            email.setTo(emailMessage.getSenderEmail());

            if (mailSender != null) {
                mailSender.send(email);
                logger.info("Email notification sent to: {}", emailMessage.getSenderEmail());
            } else {
                throw new CredentialStoreException("JavaMailSender not configured");
            }

        } catch (Exception e) {
            var msg = String.format("Error sending email notification message: %s", e.getMessage());
            logger.error(msg, e);
            throw new CredentialStoreException(msg, e);
        }
    }
}
