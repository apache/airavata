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

import junit.framework.TestCase;
import org.apache.airavata.credential.store.notifier.NotificationMessage;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 12/27/13
 * Time: 1:54 PM
 */

public class EmailNotifierTest extends TestCase {
    public void setUp() throws Exception {
        super.setUp();

    }

    // Test is disabled. Need to fill in parameters to send mails
    public void xtestNotifyMessage() throws Exception {

        EmailNotifierConfiguration emailNotifierConfiguration = new EmailNotifierConfiguration("smtp.googlemail.com",
                465, "yyy", "xxx", true, "yyy@gmail.com");

        EmailNotifier notifier = new EmailNotifier(emailNotifierConfiguration);
        EmailNotificationMessage emailNotificationMessage = new EmailNotificationMessage("Test",
                "ggg@gmail.com", "Testing credential store");
        notifier.notifyMessage(emailNotificationMessage);

    }

    // Just to ignore test failures.
    public void testIgnore() {

    }
}
