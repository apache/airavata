package org.apache.airavata.security;

/**
 * Wraps errors during authentication. This exception will be thrown if there is a system error during authentication.
 */
public class AuthenticationException extends Exception {


    public AuthenticationException() {
        super();
    }

    public AuthenticationException (String message) {
        super(message);
    }

    public AuthenticationException (String message, Exception e) {
        super(message, e);
    }


}
