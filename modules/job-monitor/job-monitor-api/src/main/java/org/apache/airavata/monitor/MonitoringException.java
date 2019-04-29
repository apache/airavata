package org.apache.airavata.monitor;

public class MonitoringException extends Exception {

    public MonitoringException() {
    }

    public MonitoringException(String message) {
        super(message);
    }

    public MonitoringException(String message, Throwable cause) {
        super(message, cause);
    }

    public MonitoringException(Throwable cause) {
        super(cause);
    }

    public MonitoringException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
