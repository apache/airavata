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
package org.apache.airavata.monitoring.mailbox;

import javax.mail.*;
import javax.mail.search.FlagTerm;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

/**
 * GmailSMTPMailBox class represents Gmail SMTP Mail Box
 *
 * @author Siddharth Jain
 */
public class GmailSMTPMailBox implements MailBox {
    //search criterion for mails
    private enum SearchCriterion {
        UNREAD
    }

    //TODO change it to READ_WRITE in production
    private static final int MAIL_BOX_MODE = Folder.READ_ONLY;
    private static final String INBOX_FOLDER_NAME = "inbox";
    private Store store;
    private Folder inbox;
    private Map<SearchCriterion, FlagTerm> searchCriterion;

    public GmailSMTPMailBox(MailConfig mailConfig) throws MessagingException {
        // initialize message store
        store = getMessageStore(mailConfig);
        // intialize inbox
        inbox = store.getFolder(INBOX_FOLDER_NAME);
        inbox.open(MAIL_BOX_MODE);
        initSearchCriterions();
    }

    /**
     * Get Message Store
     *
     * @return Message Store
     * @throws MessagingException
     */
    private Store getMessageStore(MailConfig mailConfig) throws MessagingException {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", mailConfig.getStoreProtocol());
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore(mailConfig.getStoreProtocol());
        store.connect(mailConfig.getHost(), mailConfig.getUser(), mailConfig.getPassword());
        return store;
    }

    /**
     * Populate all the search criterions in searchCriterion map
     */
    private void initSearchCriterions() {
        searchCriterion = new EnumMap<SearchCriterion, FlagTerm>(
                SearchCriterion.class);
        Flags seen = new Flags(Flags.Flag.SEEN);
        FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
        searchCriterion.put(SearchCriterion.UNREAD, unseenFlagTerm);
    }

    @Override
    public Message[] getUnreadMessages() throws MessagingException {
        Message messages[] = inbox.search(searchCriterion
                .get(SearchCriterion.UNREAD));
        return messages;
    }

    /**
     * Close all the connections to MailBox
     *
     * @throws MessagingException
     */
    public void closeConnection() throws MessagingException {
        inbox.close(true);
        store.close();
    }
}
