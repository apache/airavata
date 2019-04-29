package org.apache.airavata.helix.cluster.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformMonitorError {

    private final static Logger logger = LoggerFactory.getLogger(PlatformMonitorError.class);

    private String reason;
    private String errorCode;
    private String category;
    private Throwable error;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }
}