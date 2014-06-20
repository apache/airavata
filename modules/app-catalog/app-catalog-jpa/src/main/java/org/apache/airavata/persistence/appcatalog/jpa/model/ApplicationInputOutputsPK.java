package org.apache.airavata.persistence.appcatalog.jpa.model;

public class ApplicationInputOutputsPK {
    private String applicationID;
    private String inputOutputID;

    public ApplicationInputOutputsPK(String applicationID, String inputOutputID) {
        this.applicationID = applicationID;
        this.inputOutputID = inputOutputID;
    }

    public ApplicationInputOutputsPK() {
    }
    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public String getInputOutputID() {
        return inputOutputID;
    }

    public void setInputOutputID(String inputOutputID) {
        this.inputOutputID = inputOutputID;
    }
}
