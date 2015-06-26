package org.apache.airavata.api.server.security;

public class SecurityException extends Exception {
    public SecurityException(String message) {
        super(message);
    }

    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecurityException() {
        super();
    }
}
