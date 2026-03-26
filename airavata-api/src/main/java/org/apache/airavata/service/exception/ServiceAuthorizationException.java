package org.apache.airavata.service.exception;

public class ServiceAuthorizationException extends ServiceException {
    public ServiceAuthorizationException(String message) { super(message); }
    public ServiceAuthorizationException(String message, Throwable cause) { super(message, cause); }
}
