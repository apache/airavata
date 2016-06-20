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

    public GmailSMTPMailBox(Properties props) throws MessagingException {
        // initialize message store
        store = getMessageStore(props);
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
    private Store getMessageStore(Properties props) throws MessagingException {
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore(props.getProperty("mail.store.protocol"));
        store.connect(props.getProperty("mail.smtp.host"),
                props.getProperty("mail.userID"),
                props.getProperty("mail.password"));
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
