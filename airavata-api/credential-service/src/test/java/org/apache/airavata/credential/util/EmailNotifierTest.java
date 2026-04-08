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

import static org.junit.jupiter.api.Assertions.*;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmailNotifierTest {

    private GreenMail greenMail;

    @BeforeEach
    void setUp() {
        greenMail = new GreenMail(new ServerSetup(0, "localhost", ServerSetup.PROTOCOL_SMTP));
        greenMail.start();
        greenMail.setUser("sender@test.com", "sender@test.com", "password");
    }

    @AfterEach
    void tearDown() {
        greenMail.stop();
    }

    @Test
    void notifyMessage_sendsEmail() throws Exception {
        EmailNotifierConfiguration config = new EmailNotifierConfiguration(
                "localhost", greenMail.getSmtp().getPort(), "sender@test.com", "password", false, "sender@test.com");

        EmailNotifier notifier = new EmailNotifier(config);
        EmailNotificationMessage message =
                new EmailNotificationMessage("Test Subject", "recipient@test.com", "Test body content");

        notifier.notifyMessage(message);

        MimeMessage[] received = greenMail.getReceivedMessages();
        assertEquals(1, received.length);
        assertEquals("Test Subject", received[0].getSubject());
        assertTrue(received[0].getContent().toString().contains("Test body content"));
    }

    @Test
    void notifyMessage_multipleMessages_allDelivered() throws Exception {
        EmailNotifierConfiguration config = new EmailNotifierConfiguration(
                "localhost", greenMail.getSmtp().getPort(), "sender@test.com", "password", false, "sender@test.com");

        EmailNotifier notifier = new EmailNotifier(config);

        notifier.notifyMessage(new EmailNotificationMessage("Msg 1", "user1@test.com", "First message"));
        notifier.notifyMessage(new EmailNotificationMessage("Msg 2", "user2@test.com", "Second message"));

        MimeMessage[] received = greenMail.getReceivedMessages();
        assertEquals(2, received.length);
    }
}
