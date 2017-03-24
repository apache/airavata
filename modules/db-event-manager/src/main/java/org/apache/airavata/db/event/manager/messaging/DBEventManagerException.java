package org.apache.airavata.db.event.manager.messaging;

/**
 * Created by Ajinkya on 3/14/17.
 */
public class DBEventManagerException extends Exception {

    private static final long serialVersionUID = -2849422320139467602L;

    public DBEventManagerException(Throwable e) {
        super(e);
    }

    public DBEventManagerException(String message) {
        super(message, null);
    }

    public DBEventManagerException(String message, Throwable e) {
        super(message, e);
    }

}
