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

import org.apache.airavata.credential.store.notifier.NotificationMessage;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 12/3/13
 * Time: 5:01 PM
 */

public class EmailNotificationMessage extends NotificationMessage {

    public EmailNotificationMessage(String subject, String senderEmail, String msg) {
        super(msg);
        this.subject = subject;
        this.senderEmail = senderEmail;
    }

    private String subject;
    private String senderEmail;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }
}
