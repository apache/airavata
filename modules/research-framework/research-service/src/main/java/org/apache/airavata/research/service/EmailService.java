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
package org.apache.airavata.research.service;

import org.apache.airavata.research.service.enums.EmailType;
import org.apache.airavata.research.service.model.entity.EmailRecord;
import org.apache.airavata.research.service.model.repo.EmailRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${airavata.research-portal.admin-notification-email}")
    private String adminEmail;

    @Autowired
    private EmailRecordRepository emailRecordRepository;

    public void sendSimpleMessage(String newUserEmail, EmailType type) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(adminEmail);
        message.setSubject(type.getSubject());
        message.setText(type.getMessage(newUserEmail));
        mailSender.send(message);

        EmailRecord record = new EmailRecord();
        record.setEmailType(type);
        record.setUserId(newUserEmail);
        emailRecordRepository.save(record);
        emailRecordRepository.flush();
    }

    public boolean hasSentEmail(String toEmail, EmailType emailType) {
        return emailRecordRepository
                .findByUserIdAndEmailType(toEmail, emailType)
                .isPresent();
    }
}
