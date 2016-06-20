package org.apache.airavata.monitoring.mailbox;

import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * MailBox class represents a SMTP/IMAP e-mail box
 *
 * @author Siddharth Jain
 */
public interface MailBox {
    /**
     * Get Unread Mails
     *
     * @return Unread Mail Messages
     * @throws MessagingException
     */
    Message[] getUnreadMessages() throws MessagingException;
}
