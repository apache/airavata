package org.apache.airavata.service.exception;

public class ServiceNotFoundException extends ServiceException {
    public ServiceNotFoundException(String message) { super(message); }
    public ServiceNotFoundException(String message, Throwable cause) { super(message, cause); }
}
